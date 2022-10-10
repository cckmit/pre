//package com.xd.pre.modules.px.utils;
//
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.core.date.DateTime;
//import cn.hutool.core.date.DateUtil;
//import cn.hutool.core.io.file.FileReader;
//import cn.hutool.core.util.ObjectUtil;
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.http.HttpRequest;
//import cn.hutool.http.HttpResponse;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.xd.pre.common.constant.PreConstant;
//import com.xd.pre.common.utils.px.PreUtils;
//import com.xd.pre.modules.px.service.LocalCookieJar;
//import com.xd.pre.modules.sys.domain.JdAppStoreConfig;
//import com.xd.pre.modules.sys.domain.JdOrderPt;
//import com.xd.pre.modules.sys.domain.JdPayOrderPostAddress;
//import com.xd.pre.modules.sys.domain.JdProxyIpPort;
//import lombok.extern.slf4j.Slf4j;
//import okhttp3.*;
//
//import java.math.BigDecimal;
//import java.net.InetSocketAddress;
//import java.net.Proxy;
//import java.net.URLDecoder;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//public class Demo {
//
//    public static void main(String[] args) throws Exception {
//        FileReader fileReader = new FileReader("C:\\Users\\Administrator\\Desktop\\mck测试.txt");
//        List<String> strings = fileReader.readLines();
//        for (String ck : strings) {
//            System.out.println(ck);
//            Thread.sleep(2000L);
//            JdOrderPt jdOrderPt = JdOrderPt.builder().currentCk(ck).build();
//            HttpRequest httpRequest = HttpRequest.get("http://webapi.http.zhimacangku.com/getip?num=1&type=1&pro=&city=0&yys=0&port=1&time=1&ts=0&ys=0&cs=0&lb=1&sb=0&pb=4&mr=1&regions=");
//            HttpResponse execute = httpRequest.execute();
//            String body = execute.body();
//            String[] split = body.trim().split(":");
//            JdProxyIpPort oneIp = JdProxyIpPort.builder().ip(split[0]).port(split[1]).build();
//            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
//
//            OkHttpClient client = clientManager(proxy, jdOrderPt);
//            JdPayOrderPostAddress jdPayOrderPostAddress_2 = JdPayOrderPostAddress.builder().url("https://gamerecg.m.jd.com/game/submitOrder.action")
//                    .userAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1")
//                    .build();
//            JdAppStoreConfig jdAppStoreConfig = JdAppStoreConfig.builder().skuId("11183343342").skuPrice(new BigDecimal(100)).build();
//            String localData = submitOrder_2(client, jdPayOrderPostAddress_2, jdAppStoreConfig);
//            if (StrUtil.isBlank(localData)) {
//                continue;
//            }
//            String payId = PreUtils.parseUrl(localData).getParams().get("payId");
//            JdPayOrderPostAddress JdPayOrderPostAddress_5 = JdPayOrderPostAddress.builder()
//                    .url("https://pay.m.jd.com/index.action?functionId=wapWeiXinPay&body={\"appId\":\"jd_m_pay\",\"payId\":\"%s\",\"eid\":\"%s\"}&appId=jd_m_pay")
//                    .userAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1")
//                    .referer("https://pay.m.jd.com/cpay/newPay-index.html?appId=jd_m_yxdk&payId=%s")
//                    .build();
//            String cru = ckManager(client);
//            JdOrderPt jdOrderPtR = checkAndReturnOrderPt(cru, payId, oneIp, jdAppStoreConfig, JdPayOrderPostAddress_5);
//            if (ObjectUtil.isNotNull(jdOrderPtR)) {
//                System.out.println(JSON.toJSONString(jdOrderPtR));
//            }
//        }
//
////        String ck = "pt_key=AAJibNr7ADAmry_Xckn2oX9ydCtkHR20UvupBPMkRTEj-mN7nKIEL2ppYBdkJFbuYi3sUJg7K7g;pt_pin=jd_52fd7d64d5751";
//    }
//    public static String ckManager(OkHttpClient client) {
//        LocalCookieJar cookieJar = (LocalCookieJar) client.cookieJar();
//        List<Cookie> cookies = cookieJar.cookies;
//        StringBuilder stringBuilder = new StringBuilder();
//        for (Cookie ck : cookies) {
//            stringBuilder.append(ck.name() + "=" + ck.value() + ";");
//        }
//        return stringBuilder.toString();
//    }
//
//    public static String getWeiXinPayUrlMath_5(JdPayOrderPostAddress jdPayOrderPostAddress_5, JdOrderPt jdOrderPt, OkHttpClient client, Map<String, String> map) {
//        try {
//            if (ObjectUtil.isNull(jdOrderPt)) {
//                return null;
//            }
//            String url = String.format(jdPayOrderPostAddress_5.getUrl(), jdOrderPt.getPrerId(), PreUtils.getRandomString("eidAf7ec812217sc2unIhDbfRC2vyIiCyWCfp9rpygQ1n05pH+F1dg0Jdhd0vcmUDK5s/mtSTjOeIOzXUO1lnWYQ/J491OJXOd6I2dnstXCXFGiREnBu".length()));
//            Request.Builder header = new Request.Builder()
//                    .url(url)
//                    .get()
//                    .header("user-agent", jdPayOrderPostAddress_5.getUserAgent())
//                    .header("cookie", jdOrderPt.getCurrentCk())
//                    .header("referer", String.format(jdPayOrderPostAddress_5.getReferer(), jdOrderPt.getPrerId()));
//            if (CollUtil.isNotEmpty(map)) {
//                for (String key : map.keySet()) {
//                    header.header(key, map.get(key));
//                }
//            }
//            Request request = header.build();
//            Response response = client.newCall(request).execute();
//            String body = response.body().string();
//            response.close();
//            log.info("---------Msg:[{}]", body);
//            if (body.contains("wx.tenpay.com")) {
//                return JSON.parseObject(body).get("mweb_url").toString();
//            }
//            return null;
//        } catch (Exception e) {
//            log.error("获取微信支付失败e:{}", e.getMessage());
//        }
//        return null;
//    }
//
//    public static JdOrderPt checkAndReturnOrderPt(String ck, String payId, JdProxyIpPort oneIp, JdAppStoreConfig jdAppStoreConfig,
//                                                  JdPayOrderPostAddress JdPayOrderPostAddress_5) {
//        try {
//            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
//            OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy).build();
//            RequestBody requestBody = new FormBody.Builder()
//                    .add("appId", "jd_m_yxdk")
//                    .add("lastPage", "https://gamerecg.m.jd.com/")
//                    .add("payId", payId)
//                    .build();
//            Request request = new Request.Builder().post(requestBody).url("https://pay.m.jd.com/newpay/index.action")
//                    .header("referer", "https://pay.m.jd.com/cpay/newPay-index.html?appId=jd_m_yxdk&payId=" + payId)
//                    .header("origin", "https://pay.m.jd.com")
//                    .header("cookie", ck)
//                    .header("user-agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4239.0 Mobile Safari/537.36").build();
//            Response execute = client.newCall(request).execute();
//            String body = execute.body().string();
//            execute.close();
//            if (StrUtil.isNotBlank(body) && body.contains("wapWeiXinPay")) {
//                List<JSONObject> jsonObjects = JSON.parseArray(JSON.parseObject(JSON.parseObject(body).get("payParamsObject").toString()).get("payChannelList").toString(), JSONObject.class);
//                for (JSONObject jsonObject : jsonObjects) {
//                    if ("wapWeiXinPay".equals(jsonObject.get("code").toString()) && Integer.valueOf(jsonObject.get("status").toString()) == 1) {
//                        log.info("当前订单有微信支付msg:{}", payId);
//                        JdOrderPt.JdOrderPtBuilder orderPtBuilder = JdOrderPt.builder();
//                        JSONObject payParamsObject = JSON.parseObject(JSON.parseObject(body).get("payParamsObject").toString());
//                        orderPtBuilder.orderId(payParamsObject.get("orderId").toString());
//                        String pt_pin = URLDecoder.decode(PreUtils.get_pt_pin(ck));
//                        orderPtBuilder.ptPin(pt_pin);
////                        int countdownTime = new BigDecimal(payParamsObject.getString("countdownTime")).intValue();
//                        DateTime expire_time = DateUtil.offsetMinute(new Date(), 690);
//                        orderPtBuilder.expireTime(expire_time);
//                        orderPtBuilder.createTime(new Date());
//                        orderPtBuilder.skuPrice(new BigDecimal(payParamsObject.getString("payprice")));
//                        orderPtBuilder.skuName(jdAppStoreConfig.getSkuName());
//                        orderPtBuilder.skuId(jdAppStoreConfig.getSkuId());
//                        orderPtBuilder.prerId(payId);
//                        orderPtBuilder.isWxSuccess(PreConstant.ONE);
//                        orderPtBuilder.currentCk(ck);
//                        orderPtBuilder.ip(oneIp.getIp());
//                        orderPtBuilder.port(oneIp.getPort());
//                        orderPtBuilder.failTime(PreConstant.ZERO);
//                        orderPtBuilder.isEnable(PreConstant.FIVE);
//                        JdOrderPt jdOrderPt = orderPtBuilder.build();
//                        String weiXinPayUrlMath_5 = getWeiXinPayUrlMath_5(JdPayOrderPostAddress_5, jdOrderPt, client, null);
//                        if (StrUtil.isBlank(weiXinPayUrlMath_5)) {
//                            return null;
//                        }
//                        jdOrderPt.setWeixinUrl(weiXinPayUrlMath_5);
////                        this.jdOrderPtMapper.insert(jdOrderPt);
//                        return jdOrderPt;
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("google检查报错。请查看。msg:{}", e.getStackTrace());
//        }
//        return null;
//    }
//
//
//    public static OkHttpClient clientManager(Proxy proxy, JdOrderPt jdOrderPt) {
//        try {
//            OkHttpClient.Builder builder = new OkHttpClient().newBuilder()
//                    .followRedirects(false);
//            if (ObjectUtil.isNotNull(proxy)) {
//                builder.proxy(proxy);
//            }
//            //代理设置好了
//            if (ObjectUtil.isNotNull(jdOrderPt)) {
//                LocalCookieJar localCookieJar = new LocalCookieJar();
//                LocalCookieJar localCookieJar1 = new LocalCookieJar(jdOrderPt.getCurrentCk());
//                builder.cookieJar(localCookieJar);
//            }
//
////            builder.cookieJar()
//            return builder.build();
//        } catch (Exception e) {
//            log.error("报错msg:e:{}", e.getMessage());
//        }
//        return null;
//    }
////
////    public static String ckManager(OkHttpClient client) {
////        LocalCookieJar cookieJar = (LocalCookieJar) client.cookieJar();
////        List<Cookie> cookies = cookieJar.cookies;
////        StringBuilder stringBuilder = new StringBuilder();
////        for (Cookie ck : cookies) {
////            stringBuilder.append(ck.name() + "=" + ck.value() + ";");
////        }
////        return stringBuilder.toString();
////    }
//
//    private static String submitOrder_2(OkHttpClient client, JdPayOrderPostAddress jdPayOrderPostAddress_2, JdAppStoreConfig jdAppStoreConfig) {
//        try {
//            RequestBody requestBody = new FormBody.Builder()
//                    .add("chargeType", "13759")
//                    .add("skuId", jdAppStoreConfig.getSkuId())
//                    .add("brandId", "999440")
//                    .add("payPwd", "")
//                    .add("customs", "")
//                    .add("gamesrv", "")
//                    .add("accounttype", "")
//                    .add("chargetype", "")
//                    .add("couponIds", "")
//                    .add("useBean", "")
//                    .add("skuName", "1")
//                    .add("buyNum", "1")
//                    .add("type", "1")
//                    .add("payMode", "0")
//                    .add("totalPrice", jdAppStoreConfig.getSkuPrice().toString())
//                    .build();
//            Request request = new Request.Builder().post(requestBody).url(jdPayOrderPostAddress_2.getUrl())
//                    .header("origin", "https://gamerecg.m.jd.com")
//                    .header("referer", String.format("https://gamerecg.m.jd.com/?skuId=%s&lng=123.1212169&lat=37.196431&sid=19221a7219f0663d5c7fe613b6d0bd7w&un_area=22_1930_49324_49398", jdAppStoreConfig.getSkuId()))
//                    .header("user-agent", jdPayOrderPostAddress_2.getUserAgent())
//                    .build();
//            Response execute = client.newCall(request).execute();
//            String location = execute.header("Location");
//            execute.close();
//            Request locationRe = new Request.Builder()
//                    .url(location)
//                    .get()
//                    .build();
//            Response execute1 = client.newCall(locationRe).execute();
//            execute1.close();
//            log.info("本地跳转路径msg:{}", location);
//            return location;
//        } catch (Exception e) {
//            log.error("下单失败msg:[e:{}]", e.getMessage());
//        }
//        return null;
//    }
//}
