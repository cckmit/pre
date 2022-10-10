//package com.xd.pre.modules.px.service;
//
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.core.date.DateUtil;
//import cn.hutool.core.util.StrUtil;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.xd.pre.common.utils.px.PreUtils;
//import com.xd.pre.common.utils.px.dto.UrlEntity;
//import com.xd.pre.modules.sys.domain.JdOrderPt;
//import com.xd.pre.modules.sys.domain.JdPayOrderPostAddress;
//import lombok.extern.slf4j.Slf4j;
//import okhttp3.*;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//@Slf4j
//public class GetWeiXinPayServiceByPostService {
//
//
//    public static OkHttpClient ckMa() throws Exception {
//        OkHttpClient client = new OkHttpClient().newBuilder()
//                .cookieJar(localCookieJar)   //为OkHttp设置自动携带Cookie的功能
//                .build();
//        return client;
//    }
//
//    static LocalCookieJar localCookieJar = new LocalCookieJar();
//    public static String url = "https://un.m.jd.com/cgi-bin/app/appjmp?tokenKey=%s&to=https://gamerecg.m.jd.com?skuId=11183343342&chargeType=13759&skuName=App Store 充值卡 100元（电子卡）Apple ID 充值&skuPrice=100.00&lbs={\"lat\":\"31.196431\",\"lng\":\"103.1212169\",\"provinceId\":\"22\",\"cityId\":\"1930\",\"districtId\":\"49324\",\"provinceName\":\"四川\",\"cityName\":\"成都市\",\"districtName\":\"双流区\"}";
//
//    /*ublic static void main1(String[] args) throws Exception {
//        OkHttpClient client = ckMa();
//        GetWeiXinPayServiceByPostService getWeiXinPayServiceByPost = new GetWeiXinPayServiceByPostService();
//        String orderId = "241290646154";
//        JdPayOrderPostAddress Step2Jdappmpaya = JdPayOrderPostAddress.builder()
//                .url(String.format("https://wq.jd.com/jdpaygw/jdappmpay?dealId=%s", orderId))
//                .referer("https://wqs.jd.com/")
//                .origin("")
//                .userAgent("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4239.0 Mobile Safari/537.36")
//                .appId("jd_m_pay").build();
//        String payId = getWeiXinPayServiceByPost.step2Jdappmpay(Step2Jdappmpaya, client);
//        System.out.println(payId);
//    }*/
//
//
//    private static Process proc = null;//java进程类
//
//    public static String prpaerId(String python_home, String photo_python, String ck) throws IOException, InterruptedException {
//        String[] arguments = new String[]{python_home, photo_python, ck};//实际上后两个参数传进入也没使用。当真正需要有参数传入时可以是利用这种方式传参
//        proc = Runtime.getRuntime().exec(arguments);
//        BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(), "GBK"));
//        String line = "";
//        String returnResult = "";
//        while ((line = in.readLine()) != null) {
//            log.info("执行成功:{}", line);
//            returnResult = line;
//        }
//        in.close();
//        int re = proc.waitFor();//返回0：成功。其余返回值均表示失败，如：返回错误代码1：操作不允许，表示调用python脚本失败
//        if (re == 0) {
//            return returnResult;
//        }
//        return null;
//    }
//
//    public static void main(String[] args) throws Exception {
//        url = String.format(url, "AAEAMLKeXGosupwJk7rvm7rdSGw4Uir7pU2n6QAZ_l_BFF0T_yoyZjBxNI8o2ucTwETQpw0");
//        OkHttpClient client = ckMa();
//        GetWeiXinPayServiceByPostService getWeiXinPayServiceByPost = new GetWeiXinPayServiceByPostService();
//        boolean init = getWeiXinPayServiceByPost.init(client, url);
//        String ck = getck(client);
//        System.out.println(ck);
////        String python_url = prpaerId("F:\\ProgramData\\Anaconda3\\python.exe", "C:\\Users\\Administrator\\Downloads\\so\\emurunjdsign\\appstorm\\xia_dan.py", ck);
////        System.out.println(python_url);
//        //传入python中
//        String python_url = getWeiXinPayServiceByPost.startDingdan(ck);
//        JdPayOrderPostAddress jd_m_pay = JdPayOrderPostAddress.builder().url("https://wq.jd.com/bases/orderlist/list?order_type=1&start_page=1&page_size=10&last_page=0")
//                .referer("https://wqs.jd.com/")
//                .origin("")
//                .userAgent("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4239.0 Mobile Safari/537.36")
//                .appId("jd_m_pay").build();
//        List<JdOrderPt> jdOrderPts = getWeiXinPayServiceByPost.step1OrderList(ck, jd_m_pay, client);
//        for (JdOrderPt jdOrderPt : jdOrderPts) {
//            JdPayOrderPostAddress Step2Jdappmpaya = JdPayOrderPostAddress.builder()
//                    .url(String.format("https://wq.jd.com/jdpaygw/jdappmpay?dealId=%s", jdOrderPt.getOrderId()))
//                    .referer("https://wqs.jd.com/")
//                    .origin("")
//                    .userAgent("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4239.0 Mobile Safari/537.36")
//                    .appId("jd_m_pay").build();
//            String orderId = jdOrderPt.getOrderId();
////            String payId = getWeiXinPayServiceByPost.step2Jdappmpay(Step2Jdappmpaya, client);
//            String payId = PreUtils.parseUrl(python_url).getParams().get("payId");
//            JdPayOrderPostAddress step3Check = JdPayOrderPostAddress.builder()
//                    .url("https://pay.m.jd.com/newpay/index.action")
//                    .referer("https://pay.m.jd.com/cpay/newPay-index.html?payId=%s&appId=jd_m_pay")
//                    .origin("https://pay.m.jd.com")
//                    .userAgent("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4239.0 Mobile Safari/537.36")
//                    .appId("jd_m_pay").build();
//            Boolean check = getWeiXinPayServiceByPost.step3Check(step3Check, payId, client);
//            if (!check) {
//                continue;
//            }
//            JdPayOrderPostAddress step4wapWeiXinPay = JdPayOrderPostAddress.builder()
//                    .url("https://pay.m.jd.com/index.action?functionId=wapWeiXinPay&body={\"appId\":\"jd_m_pay\",\"payId\":\"%s\",\"eid\":\"%s\"}&appId=jd_m_pay")
//                    .referer(" https://wqs.jd.com/")
//                    .origin("https://pay.m.jd.com")
//                    .userAgent("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4239.0 Mobile Safari/537.36")
//                    .appId("jd_m_pay").build();
//            getWeiXinPayServiceByPost.step4GetWeiXinPayUrl(step4wapWeiXinPay, payId, client);
////            getWeiXinPayServiceByPost.step4Demo(payId);
////            getWeiXinPayServiceByPost.step4Demo(payId);
//        }
//    }
//
//    public static String getck(OkHttpClient client) {
//        LocalCookieJar cookieJar = (LocalCookieJar) client.cookieJar();
//        List<Cookie> cookies = cookieJar.cookies;
//        StringBuilder stringBuilder = new StringBuilder();
//        for (Cookie ck : cookies) {
//            stringBuilder.append(ck.name() + "=" + ck.value() + ";");
//        }
//        return stringBuilder.toString();
//    }
//
//    public boolean init(OkHttpClient client, String url) throws Exception {
//        Request request = new Request.Builder()
//                .url(url)
//                .get()
//                .build();
//        Response response = client.newCall(request).execute();
////        log.info(response.body().string());
//        return Boolean.FALSE;
//    }
//
//    public String startDingdan(String ck) throws Exception {
//        OkHttpClient client1 = new OkHttpClient().newBuilder()
//                .build();
//        String url = "https://gamerecg.m.jd.com/game/submitOrder.action";
//        RequestBody requestBody = new FormBody.Builder()
//                .add("chargeType", "13759")
//                .add("skuId", "11183343342")
//                .add("brandId", "999440")
//                .add("payPwd", "")
//                .add("customs", "")
//                .add("gamesrv", "")
//                .add("accounttype", "")
//                .add("chargetype", "")
//                .add("couponIds", "")
//                .add("useBean", "")
//                .add("skuName", "1")
//                .add("buyNum", "1")
//                .add("type", "1")
//                .add("payMode", "0")
//                .add("totalPrice", "100")
//                .build();
//        Request request = new Request.Builder().post(requestBody).url(url)
//                .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
//                .header("accept-encoding", "gzip, deflate, br")
//                .header("accept-language", "zh-CN,zh;q=0.9")
//                .header("cache-control", "max-age=0")
//                .header("content-type", "application/x-www-form-urlencoded")
//                .header("origin", "https://gamerecg.m.jd.com")
//                .header("referer", "https://gamerecg.m.jd.com/?skuId=11183343342&lng=103.1212169&lat=31.196431&sid=19221a7219f0663d5c7fe613b6d0bd7w&un_area=22_1930_49324_49398")
////                .header("sec-ch-ua-mobile ","?1")
//                .header("sec-ch-ua-platform", "\"Android\"")
//                .header("sec-fetch-dest", "document")
//                .header("sec-fetch-mode", "navigate")
//                .header("sec-fetch-site", "same-origin")
//                .header("sec-fetch-user", "?1")
//                .header("cookie", ck)
//                .header("upgrade-insecure-requests", "1")
//                .header("user-agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4239.0 Mobile Safari/537.36")
//                .build();
//        Response execute = client1.newCall(request).execute();
//        HttpUrl url1 = execute.request().url();
//        String preIdUrl = url1.url().toString();
//        log.info("prerId为：{}", url1.url().toString());
//        return preIdUrl;
//    }
//
//    public Boolean step4GetWeiXinPayUrl(JdPayOrderPostAddress jdPayOrderPostAddress, String payId, OkHttpClient client) throws Exception {
//        String url = String.format(jdPayOrderPostAddress.getUrl(), payId, PreUtils.getRandomString(90));
//        Request request = new Request.Builder().get().url(url)
////                .header("origin", jdPayOrderPostAddress.getOrigin())
//                .header("referer", jdPayOrderPostAddress.getReferer())
//                .header("user-agent", jdPayOrderPostAddress.getUserAgent()).build();
//        Response execute = client.newCall(request).execute();
////        String ptPin = PreUtils.getPtPinByTokenCk(ckByTokey);
//        String body = execute.body().string();
//        log.info("微信支付链接msg:{}", body);
////        System.out.println(body);
//      /*  //TODO 获取一个代理
//        HttpResponse referer = HttpRequest.get(String.format(jdPayOrderPostAddress.getUrl(), payId, PreUtils.getRandomString(90)))
//                .header("Cookie", ckByTokey)
//                .header("origin", jdPayOrderPostAddress.getOrigin())
//                .header("referer", jdPayOrderPostAddress.getReferer())
//                .header("user-agent", jdPayOrderPostAddress.getUserAgent())
//                .timeout(5000)
//                .execute();*/
////        log.info("获取微信支付链接为[body:{}]", body);
//        return null;
//    }
//
//    public Boolean step3Check(JdPayOrderPostAddress jdPayOrderPostAddress, String payId, OkHttpClient client) throws Exception {
//        //TODO 获取一个代理
//        RequestBody requestBody = new FormBody.Builder()
//                .add("appId", jdPayOrderPostAddress.getAppId())
//                .add("payId", payId)
//                .build();
//        Request request = new Request.Builder().post(requestBody).url(jdPayOrderPostAddress.getUrl())
//                .header("referer", jdPayOrderPostAddress.getReferer())
//                .header("origin", jdPayOrderPostAddress.getOrigin())
//                .header("user-agent", jdPayOrderPostAddress.getUserAgent()).build();
//        Response execute = client.newCall(request).execute();
//        String body = execute.body().string();
//
//     /*   Map<String, Object> formMap = new HashMap<>();
//        formMap.put("appId", jdPayOrderPostAddress.getAppId());
//        formMap.put("payId", payId);
//        HttpResponse referer = HttpRequest.post(jdPayOrderPostAddress.getUrl())
//                .cookie(ckByTokey)
//                .header("origin", jdPayOrderPostAddress.getOrigin())
//                .header("referer", String.format(jdPayOrderPostAddress.getReferer(), payId))
//                .header("user-agent", jdPayOrderPostAddress.getUserAgent())
//                .form(formMap)
//                .timeout(5000)
//                .execute();*/
//        if (StrUtil.isNotBlank(body) && body.contains("wapWeiXinPay")) {
//            return true;
//        }
//        return false;
//    }
//
//
//    public String step2Jdappmpay(JdPayOrderPostAddress jdPayOrderPostAddress, OkHttpClient client) throws Exception {
//        //TODO 获取一个代理
//        Request request = new Request.Builder().get().url(jdPayOrderPostAddress.getUrl())
//                .header("user-agent", jdPayOrderPostAddress.getUserAgent()).build();
//        Response execute = client.newCall(request).execute();
//        String body = execute.body().string();
//        log.info("execute.body().toString():msg:{}", body);
//        JSONObject jsonObject = JSON.parseObject(body);
//        if (Integer.valueOf(jsonObject.get("errno").toString()) != 0) {
//            return null;
//        }
//        String jumpurl = JSON.parseObject(jsonObject.get("data").toString()).get("jumpurl").toString();
////        log.info("msg:[jumpurl:{},ptPin:{}]", jumpurl, ptPin);
//        UrlEntity urlEntity = PreUtils.parseUrl(jumpurl);
//        String payId = urlEntity.getParams().get("payId");
//        return payId;
//    }
//
//
//    public List<JdOrderPt> step1OrderList(String ckByTokey, JdPayOrderPostAddress jdPayOrderPostAddress, OkHttpClient client) {
//        try {
//            String ptPin = PreUtils.getPtPinByTokenCk(ckByTokey);
//            if (StrUtil.isBlank(ptPin)) {
//                return null;
//            }
//            Request request = new Request.Builder().get().url(jdPayOrderPostAddress.getUrl())
//                    .header("referer", jdPayOrderPostAddress.getReferer())
//                    .header("user-agent", jdPayOrderPostAddress.getUserAgent()).build();
//            Response execute = client.newCall(request).execute();
//            String body = execute.body().string();
//
//            //TODO 获取一个代理
///*            HttpResponse referer = HttpRequest.get(jdPayOrderPostAddress.getUrl())
//                    .cookie(ckByTokey)
//                    .header("referer", jdPayOrderPostAddress.getReferer())
//                    .header("user-agent", jdPayOrderPostAddress.getUserAgent())
//                    .timeout(5000)
//                    .execute();
//            String body = referer.body();*/
//            JSONObject jsonObject = JSON.parseObject(body);
//            if (!"0".equals(jsonObject.get("errCode"))) {
//                return null;
//            }
//            JSONArray orderList = JSON.parseArray(jsonObject.get("orderList").toString());
//            if (CollUtil.isEmpty(orderList)) {
//                return null;
//            }
//            List<JdOrderPt> jdOrderPts = new ArrayList<>();
//            for (Object oneStr : orderList) {
//                JSONObject one = JSON.parseObject(oneStr.toString());
//                String orderId = one.get("orderId").toString();
//                BigDecimal factPrice = new BigDecimal(one.get("factPrice").toString()).divide(new BigDecimal(100), BigDecimal.ROUND_HALF_UP, 2);
//                Integer s = Integer.valueOf(JSON.parseObject(one.get("stateInfo").toString()).get("payLeftTime").toString());
//                Date expire_time = DateUtil.offsetSecond(new Date(), s);
//                Object productListObj = JSON.parseArray(one.get("productList").toString()).get(0);
//                JSONObject productList = JSON.parseObject(productListObj.toString());
//                String skuId = productList.get("skuId").toString();
//                String title = productList.get("title").toString();
//                JdOrderPt jdOrderPt = JdOrderPt.builder().orderId(orderId).ptPin(ptPin).success(0).expireTime(expire_time)
//                        .createTime(new Date()).skuPrice(factPrice).skuName(title).skuId(skuId).build();
//                log.info("入库数据msg:[jdOrderPt:{}]", jdOrderPt);
//                jdOrderPts.add(jdOrderPt);
//            }
//            return jdOrderPts;
//        } catch (Exception e) {
//            log.error("解析订单报错");
//        }
//        return null;
//    }
//
//}
