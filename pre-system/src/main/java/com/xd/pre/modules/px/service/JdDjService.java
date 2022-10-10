package com.xd.pre.modules.px.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xd.pre.common.constant.PreConstant;
import com.xd.pre.common.sign.JdSgin;
import com.xd.pre.common.utils.R;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.common.utils.px.dto.SignVoAndDto;
import com.xd.pre.common.utils.px.dto.UrlEntity;
import com.xd.pre.modules.px.appstorePc.PcAppStoreService;
import com.xd.pre.modules.px.jddj.Max5Dto;
import com.xd.pre.modules.px.jddj.cancel.CancelCombine;
import com.xd.pre.modules.px.jddj.cookie.JdDjCookie;
import com.xd.pre.modules.px.jddj.initCashier.InitCashierCombine;
import com.xd.pre.modules.px.jddj.orderDelete.OrderDeleteCombine;
import com.xd.pre.modules.px.jddj.orderdetail.OrderDetailCombine;
import com.xd.pre.modules.px.jddj.pay.PayCombine;
import com.xd.pre.modules.px.jddj.paytoken.PayTokenPrefixCombine;
import com.xd.pre.modules.px.jddj.submit.SubmitCombine;
import com.xd.pre.modules.px.vo.reqvo.TokenKeyVo;
import com.xd.pre.modules.px.vo.resvo.TokenKeyResVo;
import com.xd.pre.modules.sys.domain.*;
import com.xd.pre.modules.sys.mapper.JdAppStoreConfigMapper;
import com.xd.pre.modules.sys.mapper.JdCkZhidengMapper;
import com.xd.pre.modules.sys.mapper.JdMchOrderMapper;
import com.xd.pre.modules.sys.mapper.JdOrderPtMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JdDjService {


    @Resource
    private JdCkZhidengMapper jdCkZhidengMapper;
    @Autowired
    private TokenKeyService tokenKeyService;
    @Autowired
    private ProxyProductService proxyProductService;
    @Resource
    private JdOrderPtMapper jdOrderPtMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Resource
    private JdMchOrderMapper jdMchOrderMapper;
    @Autowired
    private NewWeiXinPayUrl newWeiXinPayUrl;


    @Autowired
    private JdDjCookie jdDjCookieSpring;

    @Autowired
    private PayTokenPrefixCombine payTokenPrefixCombineSpring;

    @Autowired
    private InitCashierCombine initCashierCombineSpring;

    @Autowired
    private PayCombine payCombineSpring;

    @Autowired
    private OrderDetailCombine orderDetailCombineSpring;

    @Autowired
    private SubmitCombine submitCombineSpring;

    @Autowired
    private CancelCombine cancelCombineSpring;

    @Autowired
    private OrderDeleteCombine orderDeleteCombineSpring;

    @Resource
    private JdAppStoreConfigMapper jdAppStoreConfigMapper;

    @Autowired
    private PcAppStoreService pcAppStoreService;


    public R match(JdMchOrder jdMchOrder, JdAppStoreConfig jdAppStoreConfig, JdLog jdLog) {
        Map<String, String> headerMap = PreUtils.buildIpMap(jdLog.getIp());
        try {
            TimeInterval timer = DateUtil.timer();
            log.info("查询今天的ck今天的账号");
            DateTime beginOfDay = DateUtil.beginOfDay(new Date());
            DateTime endOfDay = DateUtil.endOfDay(new Date());
            //查询今天可以用的ck
            LambdaQueryWrapper<JdCkZhideng> wrapper = Wrappers.<JdCkZhideng>lambdaQuery().eq(JdCkZhideng::getIsEnable, PreConstant.ONE);
            log.debug("组装超过5单就不查询了，也就是说iswxmax5");
            List<Max5Dto> max5Dtos = jdCkZhidengMapper.selectMax5Order(PreConstant.FIVE, beginOfDay, endOfDay);
            if (CollUtil.isNotEmpty(max5Dtos)) {
                List<String> pinx = max5Dtos.stream().map(it -> it.getPtPin()).collect(Collectors.toList());
                wrapper.notIn(JdCkZhideng::getPtPin, pinx);
            }
            if (jdAppStoreConfig.getConfig().equals("143") || jdAppStoreConfig.getConfig().equals("142")) {
                log.info("京东跳转方式");
//                wrapper.gt(JdCkZhideng::getMckTime, beginOfDay).lt(JdCkZhideng::getMckTime, endOfDay)
                wrapper.isNotNull(JdCkZhideng::getAppck);
            }
            wrapper.orderByAsc(JdCkZhideng::getId);
            Integer count = jdCkZhidengMapper.selectCount(wrapper);
            if (count < 1) {
                log.info("当前ck太小了。不匹配");
                return null;
            }
            int i = PreUtils.randomCommon(0, count, 1)[0];
            if (count > 15) {
                int[] ints = PreUtils.randomCommon(0, count, 13);
                List<Integer> accounts = new ArrayList<>();
                for (int anInt : ints) {
                    accounts.add(anInt);
                }
                accounts = accounts.stream().sorted().collect(Collectors.toList());
                i = accounts.get(PreConstant.ZERO);
            }
            Page<JdCkZhideng> jdCkZhidengPage = new Page<>(i, PreConstant.ONE);
            jdCkZhidengPage = jdCkZhidengMapper.selectPage(jdCkZhidengPage, wrapper);

            JdCkZhideng jdCkZhideng = jdCkZhidengPage.getRecords().get(PreConstant.ZERO);


            //TODO
            // jdCkZhideng = jdCkZhidengMapper.selectById(1);
            log.info("jdCkZhideng:" + timer.interval());
//            jdCkZhideng = this.jdCkZhidengMapper.selectById(1);
/*            JdProxyIpPort zhiLianIp = getZhiLianIp();
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(zhiLianIp.getIp(), Integer.valueOf(zhiLianIp.getPort())));
//            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            if (ObjectUtil.isNotNull(proxy)) {
                log.debug("代理设置:{}", proxy.toString());
                builder.proxy(proxy);
            }
            OkHttpClient client = builder.connectTimeout(2, TimeUnit.SECONDS).readTimeout(2, TimeUnit.SECONDS).followRedirects(false).build();*/
            OkHttpClient client = pcAppStoreService.buildClient();
            InetSocketAddress address = (InetSocketAddress) client.proxy().address();
            String hostName = address.getHostName();
            int port = address.getPort();
            JdProxyIpPort zhiLianIp = JdProxyIpPort.builder().ip(hostName).port(port + "").build();
            if (StrUtil.isNotBlank(jdCkZhideng.getMck())) {
                String loginStr = getLoginStr(jdCkZhideng.getMck());
                if (loginStr.contains("not login")) {
                    jdCkZhideng = buildJdMck(zhiLianIp, jdCkZhideng);
                }
            } else {
                jdCkZhideng = buildJdMck(zhiLianIp, jdCkZhideng);
            }
            if (ObjectUtil.isNull(jdCkZhideng)) {
                String isLoginStr = getLoginStr(jdCkZhideng.getAppck());
                if (isLoginStr.contains("not login")) {
                    jdCkZhideng.setIsEnable(PreConstant.ZERO);
                    this.jdCkZhidengMapper.updateById(jdCkZhideng);
                }
                return null;
            }

            log.info("client:" + timer.interval());
            Integer amount = jdAppStoreConfig.getSkuPrice().intValue() * 100;
            JdDjCookie jdDjCookie = jdDjCookieSpring.jdDjCookieBuild(jdCkZhideng.getMck(), client, redisTemplate, jdCkZhidengMapper, headerMap);
//            jdDjCookie.setAppCk(jdCkZhideng.getAppck());
            log.info("JdDjCookie:" + timer.interval());
            SubmitCombine submitCombine = SubmitCombine.getDjencrypt(jdDjCookie, amount);
            submitCombine = submitCombineSpring.submitOrderRequst(submitCombine, client, jdCkZhidengMapper, headerMap);
            for (int i1 = 0; i1 < 3; i1++) {
                if (ObjectUtil.isNotNull(submitCombine) && ObjectUtil.isNotNull(submitCombine.getIsAccountCode()) && submitCombine.getIsAccountCode() != PreConstant.ZERO) {
                    break;
                }
                if (ObjectUtil.isNotNull(submitCombine) && submitCombine.getIsAccountCode() == PreConstant.ZERO) {
                    jdDjCookie = jdDjCookieSpring.jdDjCookieBuild(jdCkZhideng.getMck(), client, redisTemplate, jdCkZhidengMapper, headerMap);
                    log.info("JdDjCookie:" + timer.interval());
                    submitCombine = SubmitCombine.getDjencrypt(jdDjCookie, amount);
                    submitCombine = submitCombineSpring.submitOrderRequst(submitCombine, client, jdCkZhidengMapper, headerMap);
                }
            }
            String key = submitCombine.getOrderId() + "_" + System.currentTimeMillis();
            if (ObjectUtil.isNotNull(submitCombine) && submitCombine.getIsAccountCode() != PreConstant.ZERO) {
                redisTemplate.opsForValue().set("京东到家订单创建:" + key, JSON.toJSONString(submitCombine), 180, TimeUnit.SECONDS);
            }
            log.info("submitOrderRequst:" + timer.interval());
            PayTokenPrefixCombine payTokenPrefixCombine = PayTokenPrefixCombine.getDjencrypt(jdDjCookie, submitCombine.getOrderId(), jdAppStoreConfig);
            payTokenPrefixCombine = payTokenPrefixCombine.payTokenPrefixCombineRequst(payTokenPrefixCombine, client, headerMap);
            if (jdAppStoreConfig.getConfig().equals("143") || jdAppStoreConfig.getConfig().equals("142")) {
                String payTokenUrl = payTokenPrefixCombine.getPayTokenUrl();
                for (int j = 0; j < 4; j++) {
                    UrlEntity urlEntity = PreUtils.parseUrl(payTokenUrl);
                    urlEntity.getParams().put("paySource", jdAppStoreConfig.getConfig());
                    urlEntity.getParams().put("h5hash", "cashier");
                    payTokenUrl = urlEntity.getBaseUrl() + "?" + urlEntity.getParamStr();
                    OkHttpClient client1 = pcAppStoreService.buildClient();
                    String geturl = geturl(client1, payTokenUrl, jdCkZhideng.getAppck(), jdAppStoreConfig);
                    if (StrUtil.isNotBlank(geturl)) {
                        payTokenPrefixCombine.setTokenUrl(geturl);
                        break;
                    }
                }
                if (StrUtil.isBlank(payTokenPrefixCombine.getTokenUrl())) {
                    log.error("签证没有过");
                    return null;
                }
                log.info("执行返回token的方式");
                JdOrderPt.JdOrderPtBuilder jdOrderPtBuilder = JdOrderPt.builder();
                JdOrderPt jdOrderPtDb = jdOrderPtBuilder.orderId(payTokenPrefixCombine.getOrderId())
                        .ptPin(PreUtils.get_pt_pin(jdDjCookie.getMck()))
                        .expireTime(DateUtil.offsetMinute(new Date(), jdAppStoreConfig.getPayIdExpireTime()))
                        .createTime(new Date()).skuPrice(jdAppStoreConfig.getSkuPrice()).skuName(jdAppStoreConfig.getSkuName())
                        .skuId(jdAppStoreConfig.getSkuId()).weixinUrl(payTokenPrefixCombine.getTokenUrl())
                        .isWxSuccess(PreConstant.ONE).isMatch(PreConstant.ONE).isMatch(PreConstant.ONE).currentCk(jdCkZhideng.getMck())
                        .hrefUrl(payTokenPrefixCombine.getTokenUrl()).weixinUrl(payTokenPrefixCombine.getTokenUrl())
                        .orgAppCk(jdDjCookie.getMck()).build();
                this.jdOrderPtMapper.insert(jdOrderPtDb);
                log.info("订单锁定成功msg:{},orderId:{}", timer.interval(), jdMchOrder.getTradeNo());
                Boolean isLockMath = redisTemplate.opsForValue().setIfAbsent("匹配锁定成功:" + jdMchOrder.getTradeNo(), JSON.toJSONString(jdMchOrder), jdAppStoreConfig.getExpireTime(), TimeUnit.MINUTES);
                if (!isLockMath) {
                    log.error("当前已经匹配了。请查看详情");
                    return null;
                }
                long l = (System.currentTimeMillis() - jdMchOrder.getCreateTime().getTime()) / 1000;
                jdMchOrder.setMatchTime(l);
                jdMchOrder.setOriginalTradeNo(jdOrderPtDb.getOrderId());
                jdMchOrder.setOriginalTradeId(jdOrderPtDb.getId());
                jdMchOrderMapper.updateById(jdMchOrder);
                redisTemplate.delete("京东到家订单创建:" + key);
                return R.ok(jdMchOrder);
            } else {
                // geturl(headerMap, client, payToken);
                log.info("PayTokenPrefixCombine:" + timer.interval());
/*            InitCashierCombine initCashierCombine = initCashierCombineSpring.getDjencrypt(payTokenPrefixCombine);
            initCashierCombine = InitCashierCombine.InitCashierCombineRequst(initCashierCombine, client);*/
                System.out.println("InitCashierCombine:" + timer.interval());
                PayCombine payCombine = PayCombine.getDjencrypt(payTokenPrefixCombine);
                payCombine = payCombineSpring.PayCombineRequst(payCombine, client, jdLog.getIp());
                log.info("支付链接为msg:{}", payCombine.getPayData());
/*            String payUrl = String.format("appid=%s&noncestr=%s&package=WAP&prepayid=%s&timestamp=%s&sign=%s&partnerid=%s&signType=MD5",
                    payCombine.getPayData().getAppid(), payCombine.getPayData().getNoncestr(), payCombine.getPayData().getPrepayid(),
                    payCombine.getPayData().getTimestamp(), payCombine.getPayData().getSign(), payCombine.getPayData().getPartnerid());
            log.info("weixin://wap/pay?" + payUrl);*/
                log.info("PayCombine:" + timer.interval());
                JdOrderPt.JdOrderPtBuilder jdOrderPtBuilder = JdOrderPt.builder();
                JdOrderPt jdOrderPtDb = jdOrderPtBuilder.orderId(payCombine.getOrderId())
                        .ptPin(PreUtils.get_pt_pin(jdDjCookie.getMck()))
                        .expireTime(DateUtil.offsetMinute(new Date(), jdAppStoreConfig.getPayIdExpireTime()))
                        .createTime(new Date()).skuPrice(jdAppStoreConfig.getSkuPrice()).skuName(jdAppStoreConfig.getSkuName())
                        .skuId(jdAppStoreConfig.getSkuId()).weixinUrl(payCombine.getPayData().getHrefUrl())
                        .isWxSuccess(PreConstant.ONE).isMatch(PreConstant.ONE).isMatch(PreConstant.ONE).currentCk(payCombine.getJdDjCookie().getMck())
                        .hrefUrl(payCombine.getPayData().getHrefUrl()).weixinUrl(payCombine.getPayData().getHrefUrl())
                        .orgAppCk(jdDjCookie.getMck()).build();
                this.jdOrderPtMapper.insert(jdOrderPtDb);
                log.info("订单锁定成功msg:{},orderId:{}", timer.interval(), jdMchOrder.getTradeNo());
                Boolean isLockMath = redisTemplate.opsForValue().setIfAbsent("匹配锁定成功:" + jdMchOrder.getTradeNo(), JSON.toJSONString(jdMchOrder), jdAppStoreConfig.getExpireTime(), TimeUnit.MINUTES);
                if (!isLockMath) {
                    log.error("当前已经匹配了。请查看详情");
                    return null;
                }
                long l = (System.currentTimeMillis() - jdMchOrder.getCreateTime().getTime()) / 1000;
                jdMchOrder.setMatchTime(l);
                jdMchOrder.setOriginalTradeNo(jdOrderPtDb.getOrderId());
                jdMchOrder.setOriginalTradeId(jdOrderPtDb.getId());
                jdMchOrderMapper.updateById(jdMchOrder);
/*            CancelCombine cancelCombine = CancelCombine.getDjencrypt(payCombine);
            cancelCombine = CancelCombine.cancelCombineRequst(cancelCombine, client);
            log.info("CancelCombine:" + timer.interval());
            OrderDeleteCombine deleteCombine = OrderDeleteCombine.getDjencrypt(payCombine);
            deleteCombine = OrderDeleteCombine.orderDeleteCombineRequst(deleteCombine, client);
            log.info("OrderDeleteCombine:" + timer.interval());*/
                redisTemplate.delete("京东到家订单创建:" + key);
                return R.ok(jdMchOrder);
            }
        } catch (Exception e) {
            log.error("当前账号出现报错msg:{}", e.getMessage());
        }
        return null;

    }

    private String getLoginStr(String appck) throws IOException {
        log.info("判断是否正常的账号msg:{}", appck);
        OkHttpClient client1 = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.m.jd.com/?functionId=queryJDUserInfo&appid=jd-cphdeveloper-m")
                .get()
                .addHeader("cookie", appck)
                .addHeader("origin", "https://wqs.jd.com")
                .build();
        Response response = client1.newCall(request).execute();
        String isLoginStr = response.body().string();
        response.close();
        return isLoginStr;
    }

    private JdCkZhideng buildJdMck(JdProxyIpPort oneIp, JdCkZhideng jdCkZhideng) {
        try {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
            TokenKeyVo build = TokenKeyVo.builder().cookie(jdCkZhideng.getAppck().trim()).build();
            TokenKeyResVo tokenKeyVO = tokenKeyService.getTokenKey(build, oneIp, "");
            if (ObjectUtil.isNull(tokenKeyVO)) {
                log.error("appck获取直登账号失败");
                return null;
            }
            String mck = newWeiXinPayUrl.getMck(proxy, tokenKeyVO.getTokenKey());
            if (StrUtil.isBlank(mck) || !mck.contains("app_openAA")) {
                log.error("当前ckmsg:{},获取mck失败", jdCkZhideng.getAppck());
                return null;
            }
            jdCkZhideng.setMck(mck);
            jdCkZhideng.setMckTime(new Date());
            this.jdCkZhidengMapper.updateById(jdCkZhideng);
            return jdCkZhideng;
        } catch (Exception e) {
            log.error("设置当前mck失败,mckMsg:{}", jdCkZhideng.getPtPin());
        }
        return null;
    }

    private String geturl(OkHttpClient client, String payTokenUrl, String appck, JdAppStoreConfig jdAppStoreConfig) {
        try {
            String decode = URLEncoder.encode(payTokenUrl);
            // String appck = "pin=jd_542a0da49a690;wskey=AAJivcVbAECXQyhcTW7IN04wVcP269_e5PgOaGNlRMQgHc3ksJw-ScebeVxeKycxjdZB_CzznS1EZmQUfeCCOVSDGTuxgA9Y;";
            if (jdAppStoreConfig.getConfig().equals("142")) {
                String payTokenUrlT = String.format("https://daojia.jd.com/client?functionId=login/passport&platCode=H5&appName=paidaojia&appVersion=7.2.0&body={\"returnLink\":\"%s\"}",
                        URLEncoder.encode(payTokenUrl));
                decode = URLEncoder.encode(payTokenUrlT);
            } else if (jdAppStoreConfig.getConfig().equals("143")) {

            }
            String bodyData = String.format("{\"action\":\"to\",\"to\":\"%s\"}", decode);
            SignVoAndDto signVoAndDto = new SignVoAndDto("genToken", bodyData);
            signVoAndDto = JdSgin.newSign(signVoAndDto);
            String url = String.format("https://api.m.jd.com/client.action?functionId=genToken&clientVersion=9.4.4&client=android&uuid=%s&st=%s&sign=%s&sv=120", signVoAndDto.getUuid(),
                    signVoAndDto.getSt(), signVoAndDto.getSign());
            RequestBody requestBody = new FormBody.Builder()
                    .add("body", bodyData)
                    .build();
            Request.Builder builder1 = new Request.Builder().url(url)
                    .post(requestBody)
                    .addHeader("Cookie", appck);
            log.debug("设置请求头");
            Response response = client.newCall(builder1.build()).execute();
            String resStr = response.body().string();
            String tokenKey = JSON.parseObject(resStr).getString("tokenKey");
            String t = "https://un.m.jd.com/cgi-bin/app/appjmp?tokenKey=" + tokenKey;
            log.info("跳转页面数据msg:{}", t);
            log.info("==================");
            return t;
        } catch (Exception e) {
            log.error("获取token报错了");
        }
        return null;
    }


    private static void setHeader(Map<String, String> headerMap, Request.Builder builder) {
        if (CollUtil.isNotEmpty(headerMap)) {
            for (String key : headerMap.keySet()) {
                builder.header(key, headerMap.get(key));
            }
        }
    }


    public JdProxyIpPort getZhiLianIp() {
        Set<String> ips = redisTemplate.keys(PreConstant.直连IP + "*");
        Set<String> ipLocks = redisTemplate.keys(PreConstant.直连IP锁定 + "*");
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
            String zhimadaili = redisTemplate.opsForValue().get("芝麻代理:20");
            if (StrUtil.isBlank(zhimadaili)) {
                redisTemplate.opsForValue().set("芝麻代理:20", "http://webapi.http.zhimacangku.com/getip?num=20&type=2&pro=&city=0&yys=0&port=1&time=2&ts=1&ys=0&cs=1&lb=1&sb=0&pb=4&mr=1&regions=");
                zhimadaili = "http://webapi.http.zhimacangku.com/getip?num=20&type=2&pro=&city=0&yys=0&port=1&time=2&ts=1&ys=0&cs=1&lb=1&sb=0&pb=4&mr=1&regions=";
            }
            String result2 = HttpRequest.post(zhimadaili)
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
                    redisTemplate.opsForValue().set(PreConstant.直连IP + ip, JSON.toJSONString(oneIp), l - 300, TimeUnit.SECONDS);
                }
                if (CollUtil.isNotEmpty(datas)) {
                    String ip = datas.get(0).getString("ip");
                    String s = redisTemplate.opsForValue().get(PreConstant.直连IP + ip);
                    JdProxyIpPort jdProxyIpPort = JSON.parseObject(s, JdProxyIpPort.class);
                    redisTemplate.opsForValue().set(PreConstant.直连IP锁定 + ip, JSON.toJSONString(jdProxyIpPort), 1, TimeUnit.MINUTES);
                    return jdProxyIpPort;
                }

            }
        } else {
            int i = PreUtils.randomCommon(0, ipNotLocks.size() - 1, 1)[0];
            String ip = ipNotLocks.get(i);
            String s = redisTemplate.opsForValue().get(PreConstant.直连IP + ip);
            JdProxyIpPort jdProxyIpPort = JSON.parseObject(s, JdProxyIpPort.class);
            redisTemplate.opsForValue().set(PreConstant.直连IP锁定 + ip, JSON.toJSONString(jdProxyIpPort), 1, TimeUnit.MINUTES);
            return jdProxyIpPort;
        }
        return null;
    }

    @Scheduled(cron = "0/20 * * * * ?")
    @Async("asyncPool")
    public void deleteOrderWxFail() {
        Set<String> keys = redisTemplate.keys("京东到家订单创建:*");
        if (CollUtil.isNotEmpty(keys)) {
            log.info("删除订单msg:{}", keys);
            List<String> keysOrder = keys.stream().filter(it -> {
                String s = it.split(":")[1];
                String[] s1 = s.split("_");
                String time = s1[1];
                Date timeLong = new Date(Long.valueOf(time));
                long between = DateUtil.between(timeLong, new Date(), DateUnit.SECOND);
                if (between >= 70) {
                    return true;
                }
                return false;
            }).collect(Collectors.toList());
            for (String key : keysOrder) {
                SubmitCombine submitCombine = JSON.parseObject(redisTemplate.opsForValue().get(key), SubmitCombine.class);

                log.info("删除没有入库的订单msg:{}", submitCombine.getJdDjCookie());
                OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
                OkHttpClient client = builder.followRedirects(false).build();
                JdDjCookie jdDjCookie = jdDjCookieSpring.jdDjCookieBuild(submitCombine.getJdDjCookie().getMck(), client, redisTemplate, jdCkZhidengMapper, null);
                submitCombine.setJdDjCookie(jdDjCookie);
                try {
                    String orderId = key.split(":")[1].split("_")[0];
                    JdOrderPt jdOrderPt = jdOrderPtMapper.selectOne(Wrappers.<JdOrderPt>lambdaQuery().eq(JdOrderPt::getOrderId, orderId));
                    if (ObjectUtil.isNotNull(jdOrderPt)) {
                        redisTemplate.delete(key);
                        return;
                    }
                    OrderDeleteCombine orderDeleteCombine = getOrderDeleteCombine(submitCombine.getOrderId(), client, submitCombine.getJdDjCookie());
                    if (ObjectUtil.isNotNull(orderDeleteCombine)) {
                        redisTemplate.delete(key);
                    }
                } catch (Exception e) {
                    log.error("删除订单报错msg:{}", e.getMessage());
                }
            }
        }
    }


    @Scheduled(cron = "0/20 * * * * ?")
    @Async("asyncPool")
    public void deleteOrder() {
        List<JdAppStoreConfig> jdAppStoreConfigs = jdAppStoreConfigMapper.selectList(Wrappers.<JdAppStoreConfig>lambdaQuery().eq(JdAppStoreConfig::getGroupNum, PreConstant.THREE));
        if (CollUtil.isEmpty(jdAppStoreConfigs)) {
            return;
        }
        List<String> skuIds = jdAppStoreConfigs.stream().map(it -> it.getSkuId()).collect(Collectors.toList());
        //待微信支付。并且没有成功支付时间。并且小于当前时间-10，并且大于当前时间+60分钟。自动删除
        DateTime bef = DateUtil.offsetMinute(new Date(), -8);
        DateTime af = DateUtil.offsetMinute(new Date(), -60);
        LambdaQueryWrapper<JdOrderPt> aNull = Wrappers.<JdOrderPt>lambdaQuery().
                le(JdOrderPt::getCreateTime, bef).gt(JdOrderPt::getCreateTime, af).in(JdOrderPt::getSkuId, skuIds)
                .eq(JdOrderPt::getIsWxSuccess, PreConstant.ONE)
                .isNull(JdOrderPt::getPaySuccessTime);
        List<JdOrderPt> jdOrderPts = jdOrderPtMapper.selectList(aNull);
        if (CollUtil.isNotEmpty(jdOrderPts)) {
            for (JdOrderPt jdOrderPt : jdOrderPts) {
                JdMchOrder build = JdMchOrder.builder().originalTradeId(jdOrderPt.getId()).build();
                this.deleteOrderByOrderId(build);
            }
        }

    }

    //  @Scheduled(cron = "0/20 * * * * ?")
    @Async("asyncPool")
    public void productOffCode() {
        log.info("生成核销编码");
        Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent("锁定生成核销编码", "核销编码", 1, TimeUnit.MINUTES);
        if (!ifAbsent) {
            log.info("已经有其他系统生成核销编码");
            return;
        }
        DateTime dateTime = DateUtil.offsetDay(new Date(), -10);
        List<String> pinOffCodes = jdCkZhidengMapper.selectNotProductOffCode(dateTime);
        if (CollUtil.isEmpty(pinOffCodes)) {
            log.info("没有要生成的核销编码");
            return;
        }
        for (String pinOffCode : pinOffCodes) {
            JdCkZhideng zhideng = this.jdCkZhidengMapper.selectOne(Wrappers.<JdCkZhideng>lambdaQuery().eq(JdCkZhideng::getPtPin, pinOffCode));
            if (ObjectUtil.isNull(zhideng) || ObjectUtil.isNotNull(zhideng.getWriteOffCode())) {
                continue;
            }
            Integer offCode = jdCkZhidengMapper.selectOffCode();
            zhideng.setWriteOffCode(offCode);
            this.jdCkZhidengMapper.updateById(zhideng);
        }
    }

    //    @Scheduled(cron = "0/20 * * * * ?")
    @Async("asyncPool")
    public void buildJdDjCk() {
        JdProxyIpPort oneIp = this.proxyProductService.getOneIp(PreConstant.ZERO, PreConstant.ZERO, false);
        LambdaQueryWrapper<JdCkZhideng> aNull = Wrappers.<JdCkZhideng>lambdaQuery()
                .isNull(JdCkZhideng::getJddjCkTime)
                .eq(JdCkZhideng::getIsEnable, PreConstant.ONE);
        List<JdCkZhideng> jdCkZhidengs = jdCkZhidengMapper.selectList(aNull);
        log.info("开始重置空的京东到家的ck");
        if (CollUtil.isNotEmpty(jdCkZhidengs)) {
            for (JdCkZhideng jdCkZhidengDb : jdCkZhidengs) {
                resetZhidengCkByLastTime(oneIp, jdCkZhidengDb);
            }
        }
        log.info("结束重置为空的京东到家的ck");
        //小于重置时间 并且是可以使用的
        LambdaQueryWrapper<JdCkZhideng> wrapper = Wrappers.<JdCkZhideng>lambdaQuery()
                .le(JdCkZhideng::getJddjCkTime, DateUtil.offsetHour(new Date(), -10))
                .eq(JdCkZhideng::getIsEnable, PreConstant.ONE);
        List<JdCkZhideng> leTimeList = this.jdCkZhidengMapper.selectList(wrapper);
        if (CollUtil.isEmpty(leTimeList)) {
            return;
        }
        for (JdCkZhideng jdCkZhidengDb : leTimeList) {
            resetZhidengCkByLastTime(oneIp, jdCkZhidengDb);
        }
    }

    /**
     * 获取mck设置
     *
     * @param oneIp
     * @param jdCkZhidengDb
     */
    private void resetZhidengCkByLastTime(JdProxyIpPort oneIp, JdCkZhideng jdCkZhidengDb) {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
      /*    TokenKeyVo build = TokenKeyVo.builder().cookie(jdCkZhidengDb.getAppck().trim()).build();
            TokenKeyResVo tokenKeyVO = tokenKeyService.getTokenKey(build, oneIp, "");
            TokenKeyResVo tokenKeyVO = null;
            if (ObjectUtil.isNull(tokenKeyVO)) {
            log.error("当前获取token失败");
            return;
        }*/
        String mck = jdCkZhidengDb.getMck();
        jdCkZhidengDb.setMck(mck);
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        if (ObjectUtil.isNotNull(proxy)) {
            log.debug("代理设置:{}", proxy.toString());
            builder.proxy(proxy);
        }
        OkHttpClient client = builder.followRedirects(false).build();
        JdDjCookie jdDjCookie = jdDjCookieSpring.jdDjCookieBuild(mck, client, redisTemplate, jdCkZhidengMapper, null);
        if (ObjectUtil.isNull(jdDjCookie)) {
            return;
        }
        jdCkZhidengDb.setJddjCkJson(JSON.toJSONString(jdDjCookie));
        jdCkZhidengDb.setJddjCkTime(new Date());
        this.jdCkZhidengMapper.updateById(jdCkZhidengDb);
    }


    public void selectOrderStataus(JdOrderPt jdOrderPt, JdMchOrder jdMchOrder) {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        OkHttpClient client = builder.followRedirects(false).build();
        JdDjCookie jdDjCookie = jdDjCookieSpring.jdDjCookieBuild(jdOrderPt.getCurrentCk(), client, redisTemplate, jdCkZhidengMapper, null);
        OrderDetailCombine orderDetailCombineDe = OrderDetailCombine.getDjencrypt(jdDjCookie, jdOrderPt.getOrderId());
        OrderDetailCombine orderDetailCombine = this.orderDetailCombineSpring.orderDetailCombineRequst(orderDetailCombineDe, client, jdCkZhidengMapper);
        for (int i = 0; i < 5; i++) {
            if (ObjectUtil.isNotNull(orderDetailCombine)) {
                break;
            }
            if (ObjectUtil.isNull(orderDetailCombine)) {
                jdDjCookie = jdDjCookieSpring.jdDjCookieBuild(jdOrderPt.getCurrentCk(), client, redisTemplate, jdCkZhidengMapper, null);
                orderDetailCombineDe = OrderDetailCombine.getDjencrypt(jdDjCookie, jdOrderPt.getOrderId());
                orderDetailCombine = this.orderDetailCombineSpring.orderDetailCombineRequst(orderDetailCombineDe, client, jdCkZhidengMapper);
            }
        }
        if (ObjectUtil.isNotNull(orderDetailCombine) && ObjectUtil.isNotNull(orderDetailCombine.getOrderStatus()) &&
                orderDetailCombine.getOrderStatus() == PreConstant.ONE) {
            jdOrderPt.setHtml(orderDetailCombine.getHtml());
            this.jdOrderPtMapper.updateById(jdOrderPt);
            return;
        }
        if (ObjectUtil.isNotNull(orderDetailCombine) && ObjectUtil.isNotNull(orderDetailCombine.getOrderStatus()) &&
                orderDetailCombine.getOrderStatus() == PreConstant.THREE) {
            jdOrderPt.setHtml(orderDetailCombine.getHtml());
            this.jdOrderPtMapper.updateById(jdOrderPt);
            JdMchOrder jdClientOrderDb = jdMchOrderMapper.selectById(jdMchOrder.getId());
            jdClientOrderDb.setStatus(PreConstant.THREE);
            jdMchOrderMapper.updateById(jdClientOrderDb);
            return;
        }
        if (ObjectUtil.isNotNull(orderDetailCombine) && ObjectUtil.isNotNull(orderDetailCombine.getOrderStatus()) && orderDetailCombine.getOrderStatus() == PreConstant.TWO) {
            //TODO 设置满5单，或者制卡失败的情况
            log.info("当前支付完成msg:{}", jdOrderPt);
            JdMchOrder jdClientOrderDb = jdMchOrderMapper.selectById(jdMchOrder.getId());
            jdClientOrderDb.setStatus(PreConstant.TWO);
            jdMchOrderMapper.updateById(jdClientOrderDb);
            jdOrderPt.setPaySuccessTime(new Date());
            JdCkZhideng jdCkZhideng = this.jdCkZhidengMapper.selectOne(Wrappers.<JdCkZhideng>lambdaQuery().eq(JdCkZhideng::getPtPin, PreUtils.get_pt_pin(jdOrderPt.getCurrentCk())));
            jdOrderPt.setCarMy(jdCkZhideng.getPassword());
            jdOrderPt.setCardNumber(jdCkZhideng.getAccount());
            jdOrderPt.setHtml(orderDetailCombine.getHtml());
            jdOrderPtMapper.updateById(jdOrderPt);
            log.info("设置ck状态，如果下满5单直接放弃");
            LambdaQueryWrapper<JdOrderPt> notNull = Wrappers.<JdOrderPt>lambdaQuery().eq(JdOrderPt::getPtPin, jdOrderPt.getPtPin()).isNotNull(JdOrderPt::getPaySuccessTime)
                    .gt(JdOrderPt::getCreateTime, DateUtil.beginOfDay(new Date()));
            List<JdOrderPt> jdOrderPtsMax5 = this.jdOrderPtMapper.selectList(notNull);
            if (CollUtil.isNotEmpty(jdOrderPtsMax5) && jdOrderPtsMax5.size() >= PreConstant.FIVE) {
                log.info("当前订单大于5单");
                if (ObjectUtil.isNotNull(jdCkZhideng)) {
                    log.info("设置状态满5单 状态为2 pin:{}", jdOrderPt.getPtPin());
                    jdCkZhideng.setIsEnable(PreConstant.TWO);
                    if (ObjectUtil.isNull(jdCkZhideng.getWriteOffCode())) {
                        Integer offCode = this.jdCkZhidengMapper.selectOffCode();
                        jdCkZhideng.setWriteOffCode(offCode);
                    }
                    this.jdCkZhidengMapper.updateById(jdCkZhideng);
                }
            }
        }

    }


    /**
     * 删除订单
     *
     * @param jdMchOrderDb
     */
    public void deleteOrderByOrderId(JdMchOrder jdMchOrderDb) {
        try {
            if (ObjectUtil.isNotNull(jdMchOrderDb.getOriginalTradeId())) {
                JdOrderPt jdOrderPtDb = this.jdOrderPtMapper.selectById(jdMchOrderDb.getOriginalTradeId());
                if (ObjectUtil.isNotNull(jdOrderPtDb)) {
                    log.info("删除订单");
                    OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
                    OkHttpClient client = builder.followRedirects(false).build();
                    JdDjCookie jdDjCookie = jdDjCookieSpring.jdDjCookieBuild(jdOrderPtDb.getCurrentCk(), client, redisTemplate, jdCkZhidengMapper, null);
                    if (ObjectUtil.isNotNull(jdDjCookie)) {
                        OrderDeleteCombine deleteCombine = getOrderDeleteCombine(jdOrderPtDb.getOrderId(), client, jdDjCookie);
                        if (ObjectUtil.isNotNull(deleteCombine)) {
                            log.info("当前订单删除完成制空删除把微信链接置为不可以支付");
                            jdOrderPtDb.setIsEnable(PreConstant.ZERO);
                            jdOrderPtDb.setIsWxSuccess(PreConstant.ZERO);
                            this.jdOrderPtMapper.updateById(jdOrderPtDb);
                        } else {
                            log.error("删除订单报错orderId:{}", jdOrderPtDb.getOrderId());
                            throw new RuntimeException("当前删除订单报错");
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("删除订单失败请查看日志msg:{}", e.getMessage());
        }
    }

    private OrderDeleteCombine getOrderDeleteCombine(String orderId, OkHttpClient client, JdDjCookie jdDjCookie) throws Exception {
        CancelCombine cancelCombine = CancelCombine.getDjencrypt(jdDjCookie, orderId);
        cancelCombine = cancelCombineSpring.cancelCombineRequst(cancelCombine, client);
        OrderDeleteCombine deleteCombine = OrderDeleteCombine.getDjencrypt(jdDjCookie, orderId);
        deleteCombine = orderDeleteCombineSpring.orderDeleteCombineRequst(deleteCombine, client);
        return deleteCombine;
    }
}
