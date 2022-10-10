package com.xd.pre.modules.px.appstorePc;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xd.pre.common.constant.PreConstant;
import com.xd.pre.common.des.DesUtil;
import com.xd.pre.common.sign.JdSgin;
import com.xd.pre.common.utils.R;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.common.utils.px.dto.SignVoAndDto;
import com.xd.pre.common.utils.px.dto.UrlEntity;
import com.xd.pre.modules.px.appstorePc.pcScan.PcThorDto;
import com.xd.pre.modules.px.appstorePc.pcScan.ScanUtils;
import com.xd.pre.modules.px.appstorePc.pcScan.dto.PcOrderDto;
import com.xd.pre.modules.px.service.ProxyProductService;
import com.xd.pre.modules.px.task.ProductProxyTask;
import com.xd.pre.modules.px.weipinhui.service.WphService;
import com.xd.pre.modules.sys.domain.*;
import com.xd.pre.modules.sys.mapper.JdAppStoreConfigMapper;
import com.xd.pre.modules.sys.mapper.JdCkMapper;
import com.xd.pre.modules.sys.mapper.JdMchOrderMapper;
import com.xd.pre.modules.sys.mapper.JdOrderPtMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.activemq.ScheduledMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.Queue;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PcAppStoreService {

    @Resource
    private JdCkMapper jdCkMapper;
    @Resource
    private JdOrderPtMapper jdOrderPtMapper;

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Resource(name = "product_pc_account")
    private Queue product_pc_account;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource
    private JdMchOrderMapper jdMchOrderMapper;

    @Resource
    private JdAppStoreConfigMapper jdAppStoreConfigMapper;

    @Autowired
    @Lazy(value = true)
    private WphService wphService;

    /**
     * match方法
     *
     * @param jdMchOrder
     * @param jdLog
     */
    public R match(JdMchOrder jdMchOrder, JdAppStoreConfig jdAppStoreConfig, JdLog jdLog) {
        log.info("查询数据库是否含有pc端没有过期的。并且存在能下单，能扫码的ck,并且今天下单没有满3k的ck");
        LambdaQueryWrapper<JdCk> wrapper = getPcCkNum();
        Integer count = jdCkMapper.selectCount(wrapper);
        if (count < 20) {
            log.info("当前pc登录太少开始发送消息队列到mq。生成pc代码信息");
            sendMessageSenc(product_pc_account, JSON.toJSONString(jdAppStoreConfig), 4);
            return null;
        }
        log.info("随机跑满");
        OkHttpClient client = buildClient();
        Map<String, String> headerMap = PreUtils.buildIpMap(jdLog.getIp());
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int i1 = PreUtils.randomCommon(0, count - 1, 1)[0];
            ids.add(i1);
        }
        ids = ids.stream().sorted().collect(Collectors.toList());
        Page<JdCk> jdCkPage = new Page<>(ids.get(0), 1);
        jdCkPage = jdCkMapper.selectPage(jdCkPage, wrapper);
        JdCk orderCk = jdCkPage.getRecords().get(0);
        // orderCk = jdCkMapper.selectById(464644);
        // String orderId = "250443606467";
        // orderCk = jdCkMapper.selectById(464768);
        //  log.info("开始下单");
        String orderId = submitGPOrder(jdAppStoreConfig, client, orderCk, headerMap);
        PcOrderDto pcOrderDto = PcOrderDto.builder().orderId(orderId).build();
        pcOrderDto.setJdCk(orderCk);
        if (StrUtil.isBlank(orderId)) {
            log.info("当前账号下单错误msg:{}", orderCk);
            return null;
        }
        log.info("开始获取订单的h5页面msg:{}", orderId);
        String payUrl301_1 = getPayUrl301_1(orderCk, orderId, headerMap, client);
        if (StrUtil.isBlank(payUrl301_1)) {
            //TODO 重复执行
        }
        pcOrderDto.setPayUrl301_1(payUrl301_1);
        String payUrl301_2 = getPayUrl301_2(orderCk.getThor(), payUrl301_1, headerMap, client);
        if (StrUtil.isBlank(payUrl301_2)) {
            //TODO 重复执行
        }
        pcOrderDto.setPayUrl301_2(payUrl301_2);
        String payUrl301_3 = getPayUrl301_2(orderCk.getThor(), payUrl301_2, headerMap, client);
        if (StrUtil.isBlank(payUrl301_3)) {
            //TODO 重复执行
        }
        pcOrderDto.setPayUrl301_3(payUrl301_3);
        String reqInfo = PreUtils.parseUrl(payUrl301_3).getParams().get("reqInfo");
        String sign = PreUtils.parseUrl(payUrl301_3).getParams().get("sign");
        pcOrderDto.setReqInfo(reqInfo);
        pcOrderDto.setSign(sign);
        String deviceId = PreUtils.getRandomString(90).toUpperCase();
        String fingerprint = PreUtils.getRandomString(32).toUpperCase();
        pcOrderDto.setDeviceId(deviceId);
        pcOrderDto.setFingerprint(fingerprint);
        PcOrderDto pcOrderDtoT = getPaySign(orderCk.getThor(), pcOrderDto, headerMap, client);
        if (ObjectUtil.isNull(pcOrderDtoT)) {
            //TODO 重复执行
        }
        pcOrderDto = pcOrderDtoT;
        pcOrderDtoT = weixinConfirm(orderCk.getThor(), pcOrderDto, headerMap, client);
        if (ObjectUtil.isNull(pcOrderDtoT)) {
            //TODO 重复执行
            return null;
        }
        pcOrderDto = pcOrderDtoT;
        String payUrl301_4 = getPayUrl301_2(orderCk.getThor(), pcOrderDto.getWeixinConfirm(), headerMap, client);
        if (StrUtil.isNotBlank(payUrl301_4) && payUrl301_4.contains("IINDEX0000018")) {
            orderCk.setPcRisk(PreConstant.ZERO);
            jdCkMapper.updateById(orderCk);
            return null;
        }
        log.info("执行最后一步获取二维码的signCode");
        pcOrderDtoT = qrCodeSign(orderCk.getThor(), pcOrderDto, headerMap, client);
        if (ObjectUtil.isNull(pcOrderDtoT)) {
            //TODO 重复执行
        }
        pcOrderDto = pcOrderDtoT;
        for (int i = 0; i < 5; i++) {
            log.info("执行签证index:{}", i);
            pcOrderDtoT = scanBuild(orderCk, pcOrderDto, headerMap, client);
            if (ObjectUtil.isNull(pcOrderDtoT)) {
                JdProxyIpPort oneIp = this.proxyProductService.getOneIp(PreConstant.ZERO, PreConstant.ZERO, false);
                OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
                client = builder.proxy(proxy).connectTimeout(2, TimeUnit.SECONDS).readTimeout(2, TimeUnit.SECONDS).build();
            } else {
                log.info("当前签证信息msg:{}", pcOrderDtoT.getTokenKey());
                pcOrderDto = pcOrderDtoT;
                break;
            }
        }
        log.info("当前订单执行完成msg:{}", pcOrderDto);
        Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent("匹配锁定成功:" + jdMchOrder.getTradeNo(), "锁定成功", 10, TimeUnit.MINUTES);
        if (!ifAbsent) {
            log.info("当前有京东已经匹配");
            return null;
        }
        JdOrderPt jdOrderPtDb = JdOrderPt.builder().orderId(pcOrderDto.getOrderId()).ptPin(pcOrderDto.getJdCk().getPtPin()).expireTime(DateUtil.offsetMinute(new Date(), 690))
                .createTime(new Date()).skuPrice(jdAppStoreConfig.getSkuPrice()).skuName(jdAppStoreConfig.getSkuName()).skuId(jdAppStoreConfig.getSkuId())
                .wxPayExpireTime(DateUtil.offsetMinute(new Date(), 5)).isWxSuccess(PreConstant.ONE).isMatch(PreConstant.ONE)
                .currentCk(pcOrderDto.getJdCk().getThor()).hrefUrl(pcOrderDto.getTokenKey()).weixinUrl(pcOrderDto.getTokenKey())
                .orgAppCk(pcOrderDto.getJdCk().getCk()).mark(JSON.toJSONString(pcOrderDto)).build();
        jdOrderPtMapper.insert(jdOrderPtDb);
        jdMchOrder.setOriginalTradeNo(jdOrderPtDb.getOrderId());
        jdMchOrder.setOriginalTradeId(jdOrderPtDb.getId());
        long l = (System.currentTimeMillis() - jdMchOrder.getCreateTime().getTime()) / 1000;
        jdMchOrder.setMatchTime(l);
        this.jdMchOrderMapper.updateById(jdMchOrder);
        log.info("当前匹配成功msg:{}", jdOrderPtDb.getOrderId());
        return R.ok(jdMchOrder);
    }

    @Autowired
    private ProxyProductService proxyProductService;

    public PcOrderDto scanBuild(JdCk jdCk, PcOrderDto pcOrderDto, Map<String, String> headerMap, OkHttpClient client) {
        try {
            String appjmpUrl = "https://pcashier.jd.com/image/virtualH5Pay?sign=%s";
            String bodyData = String.format("{\"action\":\"to\",\"to\":\"%s\"}", URLUtil.encode(String.format(appjmpUrl, pcOrderDto.getQrCodeSign())));
            SignVoAndDto signVoAndDto = new SignVoAndDto("genToken", bodyData);
            signVoAndDto = JdSgin.newSign(signVoAndDto);
            String url = String.format("https://api.m.jd.com/client.action?functionId=genToken&clientVersion=9.4.4&client=android&uuid=%s&st=%s&sign=%s&sv=120",
                    signVoAndDto.getUuid(), signVoAndDto.getSt(), signVoAndDto.getSign());
            RequestBody requestBody = new FormBody.Builder().add("body", bodyData).build();
            log.debug("么用");
            Request.Builder header = new Request.Builder().url(url)
                    .addHeader("Cookie", jdCk.getCk()).post(requestBody);
            setHeader(headerMap, header);
            log.debug("设置请求头");
            Response response = client.newCall(header.build()).execute();
            String resStr = response.body().string();
            response.close();
            log.info("签证信息msg:{}", resStr);
            // JdProxyIpPort oneIp = this.proxyProductService.getOneIp(PreConstant.ZERO, PreConstant.ZERO, false);
            // OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
            // client = builder.proxy(proxy).connectTimeout(2, TimeUnit.SECONDS).readTimeout(2, TimeUnit.SECONDS).build();
            String tokenKey = JSON.parseObject(resStr).getString("tokenKey");
            if (ObjectUtil.isNull(tokenKey) || tokenKey.equals("xxx")) {
                log.info("当前ip黑了。删除TODO");
                return null;
            }
            if (tokenKey.length() <= 50) {
                jdCk.setIsEnable(0);
                jdCkMapper.updateById(jdCk);
                log.info("查询是否是不能用，如果不是不能用直接可以使用的");
                ScanUtils scanUtils = new ScanUtils();
                String loginInfo = scanUtils.getLoginInfo(client, jdCk.getCk(), jdCk.getId());
                if (StrUtil.isNotBlank(loginInfo)) {
                    PcOrderDto pcOrderDtoT = new PcOrderDto();
                    BeanUtil.copyProperties(pcOrderDto, pcOrderDtoT);
                    pcOrderDtoT.setTokenKey("https://un.m.jd.com/cgi-bin/app/appjmp?tokenKey=" + tokenKey);
                    return pcOrderDtoT;
                } else {
                    return null;
                }
            }
            PcOrderDto pcOrderDtoT = new PcOrderDto();
            BeanUtil.copyProperties(pcOrderDto, pcOrderDtoT);
            pcOrderDtoT.setTokenKey("https://un.m.jd.com/cgi-bin/app/appjmp?tokenKey=" + tokenKey);
            return pcOrderDtoT;
        } catch (Exception e) {
            log.error("当前报错msg:{}", e);
        }
        return null;
    }


    public PcOrderDto qrCodeSign(String thorStr, PcOrderDto pcOrderDto, Map<String, String> headerMap, OkHttpClient client) {
        try {
            PcOrderDto pcOrderDtoT = new PcOrderDto();
            BeanUtil.copyProperties(pcOrderDto, pcOrderDtoT);
            PcThorDto pcThorDto = JSON.parseObject(thorStr, PcThorDto.class);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            String body = String.format("{\"orderId\":\"%s\",\"paySign\":\"%s\",\"riskReqVo\":{\"deviceId\":\"%s\",\"fingerprint\":\"%s\"}}",
                    pcOrderDto.getOrderId(), pcOrderDto.getPaySign(), pcOrderDto.getDeviceId(), pcOrderDto.getFingerprint());
            RequestBody requestBody = RequestBody.create(JSON, body);
            Request.Builder header = new Request.Builder().url("https://pcashier.jd.com/weixin/getWeixinImageUrl?cashierId=1&appId=pcashier")
                    .addHeader("Cookie", "thor=" + pcThorDto.getThor())
                    .post(requestBody);
            setHeader(headerMap, header);
            Response response = client.newCall(header.build()).execute();
            String resStr = response.body().string();
            response.close();
            log.info("qrCodeSign:{}", resStr);
            if (StrUtil.isNotBlank(resStr) && resStr.contains("weixinImageUrl")) {
                String weixinImageUrl = com.alibaba.fastjson.JSON.parseObject(resStr).getString("weixinImageUrl");
                if (weixinImageUrl.contains("qrCodeSign")) {
                    UrlEntity urlEntity = PreUtils.parseUrl(weixinImageUrl);
                    String qrCodeSign = urlEntity.getParams().get("qrCodeSign");
                    pcOrderDtoT.setQrCodeSign(qrCodeSign);
                    return pcOrderDtoT;
                }
                if (weixinImageUrl.contains("pcashier.jd.com/weixin/getWeixinImage")) {
                    pcOrderDtoT.setWeixinImageUrl("https:" + weixinImageUrl);
                    return pcOrderDtoT;
                }

            }

        } catch (Exception e) {
            log.info("qrCodeSign报错msg:{}", e.getMessage());
        }
        return null;

    }

    public PcOrderDto weixinConfirm(String thorStr, PcOrderDto pcOrderDto, Map<String, String> headerMap, OkHttpClient client) {
        try {
            PcThorDto pcThorDto = JSON.parseObject(thorStr, PcThorDto.class);
            PcOrderDto pcOrderDtoT = new PcOrderDto();
            BeanUtil.copyProperties(pcOrderDto, pcOrderDtoT);
            String bankPayRequestStr = String.format("{\"orderId\":\"%s\",\"pageId\":\"%s\",\"paySign\":\"%s\",\"riskReqVo\":{\"deviceId\":\"%s\",\"fingerprint\":\"%s\"},\"payingChannel\":{\"bankCode\":\"weixin\",\"channelSign\":\"%s\",\"payAmount\":\"%s\",\"agencyCode\":\"617\"}}"
                    , pcOrderDto.getOrderId(), pcOrderDto.getPageId(), pcOrderDto.getPaySign(), pcOrderDto.getDeviceId(), pcOrderDto.getFingerprint(), pcOrderDto.getChannelSign(), pcOrderDto.getShouldPay());
            RequestBody requestBody = new FormBody.Builder()
                    .add("bankPayRequestStr", bankPayRequestStr)
                    .build();
            Request.Builder header = new Request.Builder().url("https://pcashier.jd.com/weixin/weixinConfirm?cashierId=1&appId=pcashier")
                    .addHeader("Cookie", "thor=" + pcThorDto.getThor())
                    .post(requestBody);
            setHeader(headerMap, header);
            Response response = client.newCall(header.build()).execute();
            String resStr = response.body().string();
            String location = response.header("Location");
            response.close();
            // https://pcashier.jd.com/weixin/weixinPage?cashierId=1&orderId=250359359695&sign=e7ac3d7263c7a6ce5024f270481b7f2b&appId=pcashier
            log.info("weixinConfirmmsg:{}", resStr);
            if (StrUtil.isNotBlank(location) && !location.contains("login") && location.contains("weixinPage")) {
                pcOrderDtoT.setWeixinConfirm(location);
                return pcOrderDtoT;
            }
        } catch (Exception e) {
            log.error("weixinConfirm报错了msg:{}", e.getMessage());
        }
        return null;
    }

    public PcOrderDto getPaySign(String thorStr, PcOrderDto pcOrderDto, Map<String, String> headerMap, OkHttpClient client) {
        try {
            PcThorDto pcThorDto = JSON.parseObject(thorStr, PcThorDto.class);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            String body = String.format("{\"orderId\":\"%s\",\"reqInfo\":\"%s\",\"sign\":\"%s\",\"aksType\":\"sm\",\"riskReqVo\":{\"deviceId\":\"%s\",\"fingerprint\":\"%s\"}}",
                    pcOrderDto.getOrderId(), pcOrderDto.getReqInfo(), pcOrderDto.getSign(), pcOrderDto.getDeviceId(), pcOrderDto.getFingerprint());
            RequestBody requestBody = RequestBody.create(JSON, body);
            Request.Builder header = new Request.Builder().url(pcOrderDto.getIsYouka() ? "https://pay.jd.com/api-d-cashier/jdpay/getPayChannel?cashierId=1&appId=wenlv" :
                    "https://pay.jd.com/api-d-cashier/jdpay/getPayChannel?cashierId=1&appId=pcashier")
                    .addHeader("Cookie", "thor=" + pcThorDto.getThor())
                    .post(requestBody);
            setHeader(headerMap, header);
            Response response = client.newCall(header.build()).execute();
            String resStr = response.body().string();
            log.info("getPaySignmsg:{}", resStr);
            PcOrderDto pcOrderDtoT = new PcOrderDto();
            BeanUtil.copyProperties(pcOrderDto, pcOrderDtoT);
            if (resStr.contains("IINDEX00")) {
                String messageInfo = com.alibaba.fastjson.JSON.parseObject(resStr).getString("messageInfo");
                pcOrderDtoT.setOrderStatus(PreConstant.ZERO);
                pcOrderDtoT.setOrderInfo(messageInfo);
                return pcOrderDtoT;
            }
            if (!resStr.contains("paySign")) {
                return null;
            }
            JSONObject payJsonObj = com.alibaba.fastjson.JSON.parseObject(resStr);

            String paySign = payJsonObj.getString("paySign");
            String pageId = payJsonObj.getString("pageId");
            String shouldPay = payJsonObj.getString("shouldPay");
            pcOrderDtoT.setPaySign(paySign);
            pcOrderDtoT.setPageId(pageId);
            pcOrderDtoT.setShouldPay(shouldPay);
            String platPayChannelList = com.alibaba.fastjson.JSON.parseObject(payJsonObj.getString("platPayInfo")).getString("platPayChannelList");
            if (!platPayChannelList.contains("微信支付")) {
                return null;
            }
            List<JSONObject> jsonObjects = com.alibaba.fastjson.JSON.parseArray(platPayChannelList, JSONObject.class);
            for (JSONObject jsonObject : jsonObjects) {
                if (com.alibaba.fastjson.JSON.toJSONString(jsonObject).contains("微信支付")) {
                    String channelSign = jsonObject.getString("channelSign");
                    String bankcode = jsonObject.getString("bankcode");
                    String agencyCode = jsonObject.getString("agencyCode");
                    pcOrderDtoT.setChannelSign(channelSign);
                    pcOrderDtoT.setBankcode(bankcode);
                    pcOrderDtoT.setAgencyCode(agencyCode);
                    return pcOrderDtoT;
                }
            }
        } catch (Exception e) {
            log.info("获取paySign报错msg:{}", e.getMessage());
        }
        return null;
    }

    public String getPayUrl301_2(String thor, String payUrl301_1, Map<String, String> headerMap, OkHttpClient client) {
        try {
            PcThorDto pcThorDto = JSON.parseObject(thor, PcThorDto.class);
            Request.Builder header = new Request.Builder().url(payUrl301_1)
                    .addHeader("Cookie", "thor=" + pcThorDto.getThor())
                    .get();
            setHeader(headerMap, header);
            Response response = client.newCall(header.build()).execute();
            String location = response.header("Location");
            log.info("当前路由跳转302msg:{}", location);
            response.close();
            if (StrUtil.isBlank(location)) {
                return null;
            } else {
                return location.trim();
            }
        } catch (Exception e) {
            log.error("请求getPayUrl301_2报错msg:{},{}", payUrl301_1);
        }
        return null;

    }


    public String getPayUrl301_1(JdCk jdCk, String orderId, Map<String, String> headerMap, OkHttpClient client) {
        try {
            PcThorDto pcThorDto = JSON.parseObject(jdCk.getThor(), PcThorDto.class);
            String dataEncode = URLEncoder.encode(String.format("[{\"orderType\":34,\"erpOrderId\":\"%s\"}]", orderId));
            String url = String.format("https://ordergw.jd.com/orderCenter/app/1.0/?queryList=%s", dataEncode);
            Request.Builder header = new Request.Builder().url(url)
                    .addHeader("Cookie", "thor=" + pcThorDto.getThor())
                    .addHeader("Content-Type", "application/json")
                    .get();
            setHeader(headerMap, header);
            Response response = client.newCall(header.build()).execute();
            String resStr = response.body().string();
            log.info("getPayUrl301_1>>>>>>msg:{}", resStr);
            response.close();
            if (!resStr.contains("等待付款")) {
                log.info("当前订单不知道咋个回事可能过期了,请查看");
                return null;
            }
            resStr = resStr.replace("null(", "");
            resStr = resStr.replace(")", "");
            List<JSONObject> appOrderInfoList = JSON.parseArray(JSON.parseObject(resStr).getString("appOrderInfoList"), JSONObject.class);
            JSONObject jsonObject = appOrderInfoList.get(0);
            List<JSONObject> operations = JSON.parseArray(jsonObject.getString("operations"), JSONObject.class);
            for (JSONObject operation : operations) {
                if (operation.get("name").equals("付款")) {
                    String payurl301 = operation.getString("url");
                    return "https:" + payurl301;
                }
            }

        } catch (Exception e) {
            log.info("当前报错了msg:{}", e.getMessage());
        }
        return null;
    }


    public String submitGPOrder(JdAppStoreConfig jdAppStoreConfig, OkHttpClient client, JdCk jdCk, Map<String, String> headerMap) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            String bodyData = String.format("{\"appKey\":\"android\",\"brandId\":\"999440\",\"buyNum\":1,\"payMode\":\"0\",\"rechargeversion\":\"10.9\",\"skuId\":\"%s\",\"totalPrice\":\"%s\",\"type\":1,\"version\":\"1.10\"}",
                    jdAppStoreConfig.getSkuId().split("_")[0], (jdAppStoreConfig.getSkuPrice().intValue() * 100) + "");
            paramMap.put("body", bodyData);
            SignVoAndDto signVoAndDto = new SignVoAndDto("submitGPOrder", bodyData);
            signVoAndDto = JdSgin.newSign(signVoAndDto);
            log.error("签证完成msg:{}", signVoAndDto);
            String url = String.format("https://api.m.jd.com/client.action?functionId=submitGPOrder&clientVersion=%s&client=android&uuid=%s&st=%s&sign=%s&sv=%s",
                    signVoAndDto.getClientVersion(), signVoAndDto.getUuid(), signVoAndDto.getSt(), signVoAndDto.getSign(), signVoAndDto.getSv());
            log.info("开始下单");
            RequestBody requestBody = new FormBody.Builder()
                    .add("body", bodyData)
                    .build();
            Request.Builder header = new Request.Builder().url(url)
                    .addHeader("Cookie", jdCk.getCk())
                    .post(requestBody);
            setHeader(headerMap, header);
            Response response = client.newCall(header.build()).execute();
            String resStr = response.body().string();
            log.info("submitGPOrder>>>>>>msg:{}", resStr);
            response.close();
            if (resStr.contains("orderId")) {
                String result = JSON.parseObject(resStr).getString("result");
                String orderId = JSON.parseObject(result).getString("orderId");
                log.info("返回订单id:{},当前下单账号msg:{}", orderId, jdCk.getPtPin());
                return orderId;
            }
            if (resStr.contains("销售火爆，请稍后再试")) {
                jdCk.setIsAppstoreOrder(PreConstant.FUYI_1);
                jdCkMapper.updateById(jdCk);
            }
        } catch (Exception e) {
            log.info("执行下单失败msg:{}", e.getMessage());
        }
        return null;
    }

    public static void setHeader(Map<String, String> headerMap, Request.Builder builder) {
        if (CollUtil.isNotEmpty(headerMap)) {
            for (String key : headerMap.keySet()) {
                builder.header(key, headerMap.get(key));
            }
        }
    }


    private LambdaQueryWrapper<JdCk> getPcCkNum() {
        DateTime beginOfDay = DateUtil.beginOfDay(new Date());
        List<String> max2800Pins = jdOrderPtMapper.selectPcAppStoreOrderMax(beginOfDay);
        LambdaQueryWrapper<JdCk> wrapper = Wrappers.<JdCk>lambdaQuery()
                .eq(JdCk::getIsEnable, PreConstant.ONE)
                .eq(JdCk::getPcRisk, PreConstant.ZERO)
                .eq(JdCk::getIsPc, PreConstant.ONE).gt(JdCk::getPcExpireDate, new Date()).eq(JdCk::getIsAppstoreOrder, PreConstant.ONE);
        if (CollUtil.isNotEmpty(max2800Pins)) {
            wrapper.notIn(JdCk::getPtPin, max2800Pins);
        }
        return wrapper;
    }

    //查询订单接口
    @JmsListener(destination = "product_pc_account", containerFactory = "queueListener", concurrency = "20")
    public void product_pc_account(String message) {
        JdAppStoreConfig storeConfig = JSON.parseObject(message, JdAppStoreConfig.class);
        Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent("pc扫码任务", JSON.toJSONString(storeConfig), 1, TimeUnit.MINUTES);
        log.info("判断是否10分钟之前有订单过来。如有来的订单。可以添加扫码任务，如果没有。就删除了。");
        // List<JdAppStoreConfig> jdAppStoreConfigs = jdAppStoreConfigMapper.selectList(Wrappers.<JdAppStoreConfig>lambdaQuery().eq(JdAppStoreConfig::getGroupNum, storeConfig.getGroupNum()));
        // List<String> skuIds = jdAppStoreConfigs.stream().map(it -> it.getSkuId()).collect(Collectors.toList());
        if (!ifAbsent) {
            log.info("当前有任务执行了。请骚等");
            return;
        }
        Integer count = jdMchOrderMapper.selectCount(Wrappers.<JdMchOrder>lambdaQuery().
                gt(JdMchOrder::getCreateTime, DateUtil.offsetMinute(new Date(), -10)).eq(JdMchOrder::getPassCode, storeConfig.getGroupNum()));
        if (count >= 1) {
            sendMessageSenc(product_pc_account, JSON.toJSONString(storeConfig), 80);
        } else {
            return;
        }
        Integer pcCkNum = jdCkMapper.selectCount(getPcCkNum());
        if (pcCkNum >= 50) {
            log.info("当前任务可以继续保留");
            return;
        }
        log.info("开始执行扫码操作");
        LambdaQueryWrapper<JdCk> pcWrappers = Wrappers.<JdCk>lambdaQuery().eq(JdCk::getIsEnable, PreConstant.ONE)
                .in(JdCk::getIsAppstoreOrder, Arrays.asList(0, 1))
                .in(JdCk::getIsPc, Arrays.asList(0, 1))
                .apply("(pc_expire_date <now() or pc_expire_date is null)");
        // .and(o -> o.lt(JdCk::getPcExpireDate, new Date()).or().isNull(JdCk::getPcExpireDate));
        List<JdCk> needPc = jdCkMapper.selectList(pcWrappers);
        if (needPc.size() <= 10) {
            return;
        }
        log.info("随机选择appck来执行扫码");
        List<JdCk> ids = new ArrayList<>();
        for (int i = 0; i < (needPc.size() > 60 ? 60 : needPc.size()); i++) {
            int r = PreUtils.randomCommon(1, needPc.size() - 1, 1)[0];
            JdCk jdCk = needPc.get(r);
            ids.add(jdCk);
        }
        ids = ids.stream().distinct().collect(Collectors.toList());
        log.info("随机选择完成。开始执行扫码操作");
        // OkHttpClient client = wphService.buildClient();
        for (JdCk jdCk : ids) {
            ScanUtils scanUtils = new ScanUtils();
            OkHttpClient client = buildClient();
            PcThorDto pcThorDto = scanUtils.scanPcByAppCk(jdCk, client, jdCkMapper, redisTemplate, wphService, PreConstant.ZERO);
            log.info("当前账号扫码操作技术msg:{}", pcThorDto);
        }

    }

    public OkHttpClient buildClient() {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        String isAble = redisTemplate.opsForValue().get("是否使用代理");
        if (StrUtil.isNotBlank(isAble) && Integer.valueOf(isAble) == PreConstant.ONE) {
            if (CollUtil.isNotEmpty(ProductProxyTask.okClient)) {
                Map<Integer, OkHttpClient> okClient = ProductProxyTask.okClient;
                for (Integer ex : okClient.keySet()) {
                    OkHttpClient client = ProductProxyTask.okClient.get(ex);
                    ProductProxyTask.okClient.remove(ex);
                    return client;
                }
            }
            JdProxyIpPort oneIp = this.proxyProductService.getOneIp(PreConstant.ZERO, PreConstant.ZERO, false);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
            builder.proxy(proxy);
            log.info("当前使用的代理msg:{}", oneIp);
        } else {
            redisTemplate.opsForValue().set("是否使用代理", "1");
        }
        OkHttpClient client = builder.connectTimeout(3, TimeUnit.SECONDS).readTimeout(3, TimeUnit.SECONDS)
                .callTimeout(3, TimeUnit.SECONDS).writeTimeout(3, TimeUnit.SECONDS)
                .followRedirects(false).build();
        return client;
    }


    //   redisTemplate.opsForValue().set("获取手机号:" + phone, System.currentTimeMillis() + "", 180, TimeUnit.SECONDS);
    // 发送消息，destination是发送到的队列，message是待发送的消息
    private void sendMessageSenc(Destination destination, final String message, Integer minit) {
        Map<String, Object> headers = new HashMap<>();
        //发送延迟队列，延迟10秒,单位毫秒
        headers.put(ScheduledMessage.AMQ_SCHEDULED_DELAY, minit * 1000);
        jmsMessagingTemplate.convertAndSend(destination, message, headers);
    }

    public void selectOrderStataus(JdOrderPt jdOrderPt, JdMchOrder jdMchOrder) {
        log.info("查询订单是否支付成功msg:{}", jdOrderPt.getOrderId());
        String bodyData = String.format("{\"apiVersion\":\"new\",\"appKey\":\"android\",\"orderId\":\"%s\",\"rechargeversion\":\"10.9\",\"version\":\"1.10\"}", jdOrderPt.getOrderId());
        SignVoAndDto signVoAndDto = new SignVoAndDto("getGPOrderDetail", bodyData);
        signVoAndDto = JdSgin.newSign(signVoAndDto);
        String url = String.format("https://api.m.jd.com/client.action?functionId=getGPOrderDetail&clientVersion=%s&client=android&uuid=%s&st=%s&sign=%s&sv=%s",
                signVoAndDto.getClientVersion(), signVoAndDto.getUuid(), signVoAndDto.getSt(), signVoAndDto.getSign(), signVoAndDto.getSv()
        );
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("body", bodyData);
        String body = HttpRequest.post(url).form(stringObjectHashMap).header("Cookie", jdOrderPt.getOrgAppCk()).execute().body();
        if (StrUtil.isNotBlank(body) && body.contains("交易完成")) {
            body = JSON.parseObject(body).getString("result");
            Integer orderStatus = JSON.parseObject(body).getInteger("orderStatus");
            if (orderStatus == 8) {
                jdOrderPt.setSuccess(PreConstant.ONE);
                String cardInfos = JSON.parseObject(body).getString("cardInfos");
                try {
                    List<JSONObject> jsonObjects = JSON.parseArray(DesUtil.decode(cardInfos, "2E1ZMAF88CCE5EBE551FR3E9AA6FF322"), JSONObject.class);
                    StringBuilder ca = new StringBuilder();
                    StringBuilder mi = new StringBuilder();
                    for (JSONObject jsonObject : jsonObjects) {
                        String cardNo = jsonObject.getString("cardNo");
                        String cardPass = jsonObject.getString("cardPass");
                        if (jsonObjects.indexOf(jsonObject) == jsonObjects.size() - 1) {
                            ca.append(cardNo);
                            mi.append(cardPass);
                        } else {
                            ca.append(cardNo + "----");
                            mi.append(cardPass + "----");
                        }

                    }
                    jdOrderPt.setCardNumber(ca.toString());
                    jdOrderPt.setCarMy(mi.toString());
                    jdOrderPt.setPaySuccessTime(new Date());
                    jdOrderPtMapper.updateById(jdOrderPt);
                    jdMchOrder.setStatus(PreConstant.TWO);
                    jdMchOrderMapper.updateById(jdMchOrder);
                } catch (Exception e) {
                    log.info("解密失败msg:{}", e.getMessage());
                }

            }
        }


    }
}
