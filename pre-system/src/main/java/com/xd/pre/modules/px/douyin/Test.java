/*
package com.xd.pre.modules.px.douyin;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xd.pre.common.constant.PreConstant;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.common.utils.px.dto.UrlEntity;
import com.xd.pre.modules.sys.domain.JdProxyIpPort;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Test {


    public static void main(String[] args) throws Exception {
        TimeInterval timer = DateUtil.timer();
        String ip = "42.54.82.249";
        String port = "4231";
        String customized_price = 1 * 100 + "";
//        String short_id = "xuda_yevs_niang";
        String short_id = "11111";
        String ck = "53125d354ad5ca74146f24103fe01a21";
        JdProxyIpPort zhiLianIp = JdProxyIpPort.builder().ip(ip).port(port).build();
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(zhiLianIp.getIp(), Integer.valueOf(zhiLianIp.getPort())));
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.proxy(proxy);
        OkHttpClient client = builder.connectTimeout(3, TimeUnit.SECONDS).readTimeout(3, TimeUnit.SECONDS).build();
        String createOrderStr = createOrder(customized_price, short_id, ck, client);
        log.info("耗时createOrderStr:{}", timer.interval());
        if (StrUtil.isBlank(createOrderStr) || !createOrderStr.contains("order_id") || !createOrderStr.contains("https://tp-pay.snssdk.com/cashdesk")) {
            log.error("没有获取到支付链接。请查看日志");
            return;
        }
        JSONObject payUrlJson = JSON.parseObject(createOrderStr);
        if (payUrlJson.getInteger("status_code") != PreConstant.ZERO) {
            log.error("没有获取到支付链接。请查看日志");
            return;
        }
        String payUrlJsonDataStr = payUrlJson.getString("data");
        JSONObject payUrlJsonDataJson = JSON.parseObject(payUrlJsonDataStr);
        Object order_id = payUrlJsonDataJson.get("order_id");
        log.info("支付订单为msg:{}", order_id);
        String params = payUrlJsonDataJson.getString("params");
//        Boolean orderStatus = getOrderStatus("https://tp-pay.snssdk.com/cashdesk/?app_id=800095745677&encodeType=base64&merchant_id=1200009574&out_order_no=10000017111010059945513999&return_scheme=&return_url=aHR0cHM6Ly93d3cuZG91eWluLmNvbS9wYXk=&sign=cf552e5d8b99bf3f9fc8bf69903ed781&sign_type=MD5&switch=00&timestamp=1655661184&total_amount=100&trade_no=SP2022062001523633610511364415&trade_type=H5&uid=92801979958");
        log.debug("获取订单状态开始");
        String orderStatus = getOrderStatus(params, client);
        log.debug("获取订单状态结束");
        log.info("耗时getOrderStatus:{}", timer.interval());
        if (StrUtil.isBlank(orderStatus)) {
            log.error("获取订单状态报错");
            return;
        }
        if (orderStatus.contains("交易已成功，请查询订单状态")) {
            log.info("支付成功msg:{}", short_id);
        }
        String process = JSON.parseObject(orderStatus).getString("process");
        if (StrUtil.isBlank(process) || !process.contains("cache")) {
            log.error("当前订单,{}没有获取到process", order_id);
        }
        String trade_no = PreUtils.parseUrl(params).getParams().get("trade_no");
        String mwebUrl = getMwebUrl(process, trade_no, client);
        log.info("耗时getMwebUrl:{}", timer.interval());
    }

    private static String getMwebUrl(String process, String trade_no, OkHttpClient client) {
        try {
            RequestBody requestBody = new FormBody.Builder()
                    .add("scene", "h5")
                    .add("risk_info", "{\"device_platform\":\"android\"}")
                    .add("biz_content", String.format("{\"trade_no\":\"%s\",\"ptcode\":\"wx\",\"ptcode_info\":{\"bank_card_id\":\"\",\"business_scene\":\"\"}}", trade_no))
                    .add("process", process)
                    .build();
            Request request = new Request.Builder()
                    .url("https://tp-pay.snssdk.com/gateway-cashier2/tp/cashier/trade_confirm")
                    .post(requestBody)
                    .addHeader("user-agent", "Mozilla/5.0 (Linux; Android 8.0.0; SM-G955U Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Mobile Safari/537.36 Edg/102.0.5005.124")
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

    private static String createOrder(String customized_price, String short_id, String ck, OkHttpClient client) {
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

    private static String getOrderStatus(String params, OkHttpClient client) {
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
}
*/
