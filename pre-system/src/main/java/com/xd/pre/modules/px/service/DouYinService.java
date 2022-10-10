package com.xd.pre.modules.px.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xd.pre.common.constant.PreConstant;
import com.xd.pre.common.utils.R;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.common.utils.px.dto.UrlEntity;
import com.xd.pre.modules.sys.domain.*;
import com.xd.pre.modules.sys.mapper.DouyinChongzhiAccountMapper;
import com.xd.pre.modules.sys.mapper.DouyinCkMapper;
import com.xd.pre.modules.sys.mapper.JdMchOrderMapper;
import com.xd.pre.modules.sys.mapper.JdOrderPtMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DouYinService {

    @Resource
    private DouyinChongzhiAccountMapper douyinChongzhiAccountMapper;
    @Resource
    private DouyinCkMapper douyinCkMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Resource
    private JdOrderPtMapper jdOrderPtMapper;
    @Resource
    private JdMchOrderMapper jdMchOrderMapper;

    public R match(JdMchOrder jdMchOrder, JdAppStoreConfig jdAppStoreConfig, JdLog jdLog) {
        TimeInterval timer = DateUtil.timer();
        log.info("开始匹配抖音充值msg:{}", jdMchOrder);
        log.info("获取抖音ck锁定");
        Set<String> keys = redisTemplate.keys(PreConstant.抖音CK锁定 + "*");
        LambdaQueryWrapper<DouyinCk> ckLambdaQueryWrapper = Wrappers.<DouyinCk>lambdaQuery();
        if (CollUtil.isNotEmpty(keys)) {
            ckLambdaQueryWrapper.notIn(DouyinCk::getCk, keys.stream().map(it -> it.split(":")[1]).collect(Collectors.toList()));
        }
        List<DouyinCk> douyinCks = douyinCkMapper.selectList(ckLambdaQueryWrapper);
        if (CollUtil.isEmpty(douyinCks)) {
            log.error("当前没有可以用的抖音CK");
            return null;
        }
        int randomCkIndex = PreUtils.randomCommon(0, douyinCks.size() - 1, 1)[0];
        DouyinCk douyinCk = douyinCks.get(randomCkIndex);
        Boolean ifAbsentCK = redisTemplate.opsForValue().setIfAbsent(PreConstant.抖音CK锁定 + douyinCk.getCk(), JSON.toJSONString(douyinCk), 30, TimeUnit.SECONDS);
        if (!ifAbsentCK) {
            log.error("当亲ck已经被其他线程锁定");
            return null;
        }
        log.info("获取充值的账号,并且排除锁定的账号");
        LambdaQueryWrapper<DouyinChongzhiAccount> douyinChongzhiAccountLambdaQueryWrapper = Wrappers.<DouyinChongzhiAccount>lambdaQuery();
        Set<String> accounts = redisTemplate.keys(PreConstant.抖音充值账号ID锁定 + "*");
        if (CollUtil.isNotEmpty(accounts)) {
            douyinChongzhiAccountLambdaQueryWrapper.notIn(DouyinChongzhiAccount::getId,
                    accounts.stream().map(it -> it.split(":")[1]).collect(Collectors.toList()));
        }
        douyinChongzhiAccountLambdaQueryWrapper.eq(DouyinChongzhiAccount::getMoney, jdAppStoreConfig.getSkuPrice());
        douyinChongzhiAccountLambdaQueryWrapper.eq(DouyinChongzhiAccount::getIsEnable, PreConstant.ONE);
        douyinChongzhiAccountLambdaQueryWrapper.eq(DouyinChongzhiAccount::getIsSuccess, PreConstant.ZERO);
        douyinChongzhiAccountLambdaQueryWrapper.orderByAsc(DouyinChongzhiAccount::getId);
        Integer count = douyinChongzhiAccountMapper.selectCount(douyinChongzhiAccountLambdaQueryWrapper);
        if (count <= 0) {
            log.error("当前没有充值的抖音账号");
            return null;
        }
        int i = PreUtils.randomCommon(0, count, 1)[0];
        if (count > 15) {
            if (count > 15) {
                int[] ints = PreUtils.randomCommon(0, count, 13);
                List<Integer> chongzhiaccount = new ArrayList<>();
                for (int anInt : ints) {
                    chongzhiaccount.add(anInt);
                }
                chongzhiaccount = chongzhiaccount.stream().sorted().collect(Collectors.toList());
                i = chongzhiaccount.get(PreConstant.ZERO);
            }
        }
        log.info("获取到最小的msg:{}", i);
        Page<DouyinChongzhiAccount> douyinChongzhiAccountPage = new Page<>(i, 1);
        douyinChongzhiAccountPage = douyinChongzhiAccountMapper.selectPage(douyinChongzhiAccountPage, douyinChongzhiAccountLambdaQueryWrapper);
        if (CollUtil.isNotEmpty(douyinChongzhiAccountPage.getRecords())) {
            DouyinChongzhiAccount douyinChongzhiAccount = douyinChongzhiAccountPage.getRecords().get(PreConstant.ZERO);
            Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent(PreConstant.抖音充值账号ID锁定 + douyinChongzhiAccount.getId(), JSON.toJSONString(douyinChongzhiAccount), 8, TimeUnit.MINUTES);
            if (!ifAbsent) {
                log.error("当前充值抖音账号已经被其他的锁定msg:{}", douyinChongzhiAccount.getId());
                return null;
            }
        }
        DouyinChongzhiAccount douyinChongzhiAccount = douyinChongzhiAccountPage.getRecords().get(PreConstant.ZERO);
        JdProxyIpPort zhiLianIp = getZhiLianIp();
        log.info("当前获取的Ipmsg:{}", zhiLianIp);
//        JdProxyIpPort zhiLianIp = JdProxyIpPort.builder().ip(ip).port(port).build();
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(zhiLianIp.getIp(), Integer.valueOf(zhiLianIp.getPort())));
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.proxy(proxy);
        OkHttpClient client = builder.connectTimeout(3, TimeUnit.SECONDS).readTimeout(3, TimeUnit.SECONDS).build();
        String customized_price = jdAppStoreConfig.getSkuPrice().intValue() * 100 + "";
        String createOrderStr = createOrder(customized_price, douyinChongzhiAccount.getAccount(), douyinCk.getCk(), client);
        if (StrUtil.isBlank(createOrderStr) || !createOrderStr.contains("order_id") || !createOrderStr.contains("https://tp-pay.snssdk.com/cashdesk")) {
            log.error("没有获取到支付链接。请查看日志");
            redisTemplate.delete(PreConstant.抖音充值账号ID锁定 + douyinChongzhiAccount.getId());
            redisTemplate.delete(PreConstant.抖音充值账号ID锁定 + douyinCk.getCk());
            return null;
        }
        log.info("当前获取到支付链接了.获取支付链接");
        JSONObject payUrlJson = JSON.parseObject(createOrderStr);
        String payUrlJsonDataStr = payUrlJson.getString("data");
        JSONObject payUrlJsonDataJson = JSON.parseObject(payUrlJsonDataStr);
        String params = payUrlJsonDataJson.getString("params");
        String trade_no = PreUtils.parseUrl(params).getParams().get("trade_no");
        log.info("支付抖音订单号订单号msg:{}", trade_no);
//        if (buyao(jdLog, douyinCk, douyinChongzhiAccount, client, params, trade_no)) return null;
//        log.info("耗时getMwebUrl:{}", timer.interval());
        log.info("当前支付链接为msg:{}", params);
        JdOrderPt.JdOrderPtBuilder jdOrderPtBuilder = JdOrderPt.builder();
        JdOrderPt jdOrderPtDb = jdOrderPtBuilder.orderId(trade_no)
                .ptPin(PreUtils.get_pt_pin(null))
                .expireTime(DateUtil.offsetMinute(new Date(), jdAppStoreConfig.getPayIdExpireTime()))
                .createTime(new Date()).skuPrice(jdAppStoreConfig.getSkuPrice()).skuName(jdAppStoreConfig.getSkuName())
                .skuId(jdAppStoreConfig.getSkuId()).weixinUrl(params)
                .isWxSuccess(PreConstant.ONE).isMatch(PreConstant.ONE).isMatch(PreConstant.ONE).currentCk(null)
                .hrefUrl(params).weixinUrl(params)
                .orgAppCk(params).build();
        this.jdOrderPtMapper.insert(jdOrderPtDb);
        log.info("订单锁定成功msg时间:{},orderId:{}", timer.interval(), jdMchOrder.getTradeNo());
        Boolean isLockMath = redisTemplate.opsForValue().setIfAbsent("匹配锁定成功:" + jdMchOrder.getTradeNo(), JSON.toJSONString(jdMchOrder), jdAppStoreConfig.getExpireTime(), TimeUnit.MINUTES);
        if (!isLockMath) {
            log.error("当前已经匹配了。请查看详情,");
            return null;
        }
        long l = (System.currentTimeMillis() - jdMchOrder.getCreateTime().getTime()) / 1000;
        jdMchOrder.setOriginalTradeNo(jdOrderPtDb.getOrderId());
        jdMchOrder.setMatchTime(l);
        jdMchOrder.setOriginalTradeId(jdOrderPtDb.getId());
        jdMchOrderMapper.updateById(jdMchOrder);
        return R.ok(jdMchOrder);
    }

    private boolean buyao(JdLog jdLog, DouyinCk douyinCk, DouyinChongzhiAccount douyinChongzhiAccount, OkHttpClient client, String params, String trade_no) {
        String orderStatus = getOrderStatus(params, client);
        if (StrUtil.isBlank(orderStatus)) {
            log.error("没有获取到process,请查看日志当前订单号:{}", trade_no);
            redisTemplate.delete(PreConstant.抖音充值账号ID锁定 + douyinChongzhiAccount.getId());
            redisTemplate.delete(PreConstant.抖音充值账号ID锁定 + douyinCk.getCk());
            return true;
        }
        String process = JSON.parseObject(orderStatus).getString("process");
        if (StrUtil.isBlank(process) || !process.contains("cache")) {
            log.error("当前订单,{}没有获取到process", trade_no);
        }
        String mwebUrl = getMwebUrl(process, trade_no, client, jdLog.getIp());
        Map<String, String> headerMap = PreUtils.buildIpMap(jdLog.getIp());
       /* String hrefUrl = payCombine.weixinUrl(mwebUrl, headerMap);
        log.info("当前支付链接为Msg:{},系统订单号为orderId:{}", hrefUrl);
        if (StrUtil.isBlank(hrefUrl)) {
            log.error("当前获取支付链接为空,请查看日志");
            redisTemplate.delete(PreConstant.抖音充值账号ID锁定 + douyinChongzhiAccount.getId());
            redisTemplate.delete(PreConstant.抖音充值账号ID锁定 + douyinCk.getCk());
            return true;
        }*/
        return false;
    }

    private String getMwebUrl(String process, String trade_no, OkHttpClient client, String ip) {
        try {
            Map<String, String> headerMap = PreUtils.buildIpMap(ip);
            RequestBody requestBody = new FormBody.Builder()
                    .add("scene", "h5")
                    .add("risk_info", "{\"device_platform\":\"android\"}")
                    .add("biz_content", String.format("{\"trade_no\":\"%s\",\"ptcode\":\"wx\",\"ptcode_info\":{\"bank_card_id\":\"\",\"business_scene\":\"\"}}", trade_no))
                    .add("process", process)
                    .build();
            Request.Builder requstBuild =
                    new Request.Builder()
                            .url("https://tp-pay.snssdk.com/gateway-cashier2/tp/cashier/trade_confirm")
                            .post(requestBody);
            for (String key : headerMap.keySet()) {
                requstBuild.header(key, headerMap.get(key));
            }
            Request request = requstBuild.addHeader("user-agent", "Mozilla/5.0 (Linux; Android 8.0.0; SM-G955U Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Mobile Safari/537.36 Edg/102.0.5005.124")
                    .build();
            Response response = client.newCall(request).execute();
            String payUrlStr = response.body().string();
            response.close();
            log.info("支付链接payUrlStr:{}", payUrlStr);
            if (StrUtil.isBlank(payUrlStr) || !payUrlStr.contains("https://wx.tenpay.com/")) {
                log.error("当前订单没有微信链接msg:{}", trade_no);
            }
            String payData = JSON.parseObject(JSON.parseObject(JSON.parseObject(payUrlStr).getString("data")).getString("pay_params")).getString("data");
            String mweb_url = JSON.parseObject(payData).getString("mweb_url");
            log.info("当前跳转链接为msg:{}", mweb_url);
            return mweb_url;
        } catch (Exception e) {
            log.error("获取跳转链接失败mweb_url:{}", e.getMessage());
        }
        return null;
    }

    public String getOrderStatus(String params, OkHttpClient client) {
        try {
            UrlEntity urlEntity = PreUtils.parseUrl(params);
            String findStatusParams = String.format("{\"params\": %s}", JSON.toJSONString(urlEntity.getParams()));
            RequestBody requestBody = new FormBody.Builder()
                    .add("scene", "h5")
                    .add("risk_info", "{\"device_platform\":\"android\"}")
                    .add("biz_content", findStatusParams)
                    .build();
            Request request = new Request.Builder()
                    .url("https://tp-pay.snssdk.com/gateway-cashier2/tp/cashier/trade_create")
                    .post(requestBody)
                    .addHeader("user-agent", "Mozilla/5.0 (Linux; Android 8.0.0; SM-G955U Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Mobile Safari/537.36 Edg/102.0.5005.124")
                    .build();
            Response response = client.newCall(request).execute();
            String resultOrderStatus = response.body().string();
            response.close();
            log.info("支付结果为msg：{}", resultOrderStatus);
            return resultOrderStatus;
        } catch (Exception e) {
            log.info("获取支付结果或者process 报错msg:{}", e.getMessage());
        }
        return null;
    }

    private String createOrder(String customized_price, String short_id, String ck, OkHttpClient client) {
        try {
            String fp = PreUtils.getRandomNum(52);
            String url = String.format("https://www.douyin.com/webcast/wallet_api/diamond_buy_external_safe/?diamond_id=888888&source=10&way=0&aid=1128&platform=android" +
                    "&fp=%s&customized_price=%s&short_id=%s", fp, customized_price, short_id);
            RequestBody requestBody = new FormBody.Builder().build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("cookie", String.format("sessionid_ss=%s;", ck))
                    .addHeader("user-agent", "Mozilla/5.0 (Linux; Android 8.0.0; SM-G955U Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Mobile Safari/537.36 Edg/102.0.5005.124")
                    .build();
            Response response = client.newCall(request).execute();
            String body = response.body().string();
            response.close();
            log.info("body:{}", body);
            return body;
        } catch (Exception e) {
            log.error("请求订单报错msg:{}", e.getMessage());
        }
        return null;
    }


    private JdProxyIpPort getZhiLianIp() {
        Set<String> ips = redisTemplate.keys(PreConstant.直连IPSOCKS5 + "*");
        Set<String> ipLocks = redisTemplate.keys(PreConstant.直连IP锁定SOCKS5 + "*");
        List<String> ipNotLocks = null;
        if (CollUtil.isEmpty(ipLocks)) {
            ipNotLocks = ips.stream().map(it -> it.split(":")[1]).collect(Collectors.toList());
        } else {
            List<String> all = ips.stream().map(it -> it.split(":")[1]).collect(Collectors.toList());
            List<String> allLock = ipLocks.stream().map(it -> it.split(":")[1]).collect(Collectors.toList());
            ipNotLocks = all.stream().filter(it -> !allLock.contains(it)).collect(Collectors.toList());
        }
        if (CollUtil.isEmpty(ipNotLocks) || ipNotLocks.size() < 20) {
            //        TODO 查询直登账号，并且待支付和已经支付的不超过5单。这个账号
//         返回对应的订单号。然后支付
            //        //下单
            log.info("获取独享ip");
            String result2 = HttpRequest.post("http://webapi.http.zhimacangku.com/getip?num=20&type=2&pro=&city=0&yys=0&port=2&time=2&ts=1&ys=0&cs=0&lb=1&sb=0&pb=4&mr=1&regions=")
                    .timeout(20000)//超时，毫秒
                    .execute().body();
            JSONObject ipStr = JSON.parseObject(result2);
            if (StrUtil.isNotBlank(result2) && ipStr.getInteger("code") == PreConstant.ZERO) {
                List<JSONObject> datas = JSON.parseArray(ipStr.getString("data"), JSONObject.class);
                for (JSONObject dataMap : datas) {
                    String ip = dataMap.getString("ip");
                    Integer port = dataMap.getInteger("port");
                    String expire_time = dataMap.getString("expire_time");
                    DateTime ex = DateUtil.parseDateTime(expire_time);
                    long l = DateUtil.betweenMs(new Date(), ex) / 1000;
                    JdProxyIpPort oneIp = JdProxyIpPort.builder().ip(ip).expirationTime(ex).port(port + "").build();
                    redisTemplate.opsForValue().set(PreConstant.直连IPSOCKS5 + ip, JSON.toJSONString(oneIp), l - 300, TimeUnit.SECONDS);
                }
                if (CollUtil.isNotEmpty(datas)) {
                    String ip = datas.get(0).getString("ip");
                    String s = redisTemplate.opsForValue().get(PreConstant.直连IPSOCKS5 + ip);
                    JdProxyIpPort jdProxyIpPort = JSON.parseObject(s, JdProxyIpPort.class);
                    redisTemplate.opsForValue().set(PreConstant.直连IP锁定SOCKS5 + ip, JSON.toJSONString(jdProxyIpPort), 1, TimeUnit.MINUTES);
                    return jdProxyIpPort;
                }

            }
        } else {
            int i = PreUtils.randomCommon(0, ipNotLocks.size() - 1, 1)[0];
            String ip = ipNotLocks.get(i);
            String s = redisTemplate.opsForValue().get(PreConstant.直连IPSOCKS5 + ip);
            JdProxyIpPort jdProxyIpPort = JSON.parseObject(s, JdProxyIpPort.class);
            redisTemplate.opsForValue().set(PreConstant.直连IP锁定SOCKS5 + ip, JSON.toJSONString(jdProxyIpPort), 1, TimeUnit.MINUTES);
            return jdProxyIpPort;
        }
        return null;
    }

}
