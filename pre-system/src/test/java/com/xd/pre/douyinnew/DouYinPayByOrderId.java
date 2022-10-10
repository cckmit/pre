package com.xd.pre.douyinnew;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.modules.px.douyin.pay.PayDto;
import com.xd.pre.modules.px.douyin.pay.PayRiskInfoAndPayInfoUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.net.URLEncoder;
import java.util.Map;

@Slf4j
public class DouYinPayByOrderId {

    public static void main(String[] args) {
        try {
            String ck = "install_id=3743163984904813; ttreq=1$c937432add1ac5543b40dc8b95cb769bf024bf3a; passport_csrf_token=dc084fdfd9182b2006ac015d23d5094e; passport_csrf_token_default=dc084fdfd9182b2006ac015d23d5094e; d_ticket=5d8498d9c5c57a18f23083f8b948b45743690; multi_sids=659356656346136%3A140a336dd81551eaa30bc0e9e8d336fd; n_mh=8nysT__BxDL_VpPZTRMYKZZSN1pywPhZ9o63MSmzGLg; passport_assist_user=CkCRydX49tKRiP6NfppL8EZXqhP7I0lHXjcq-1NuFi9tetbHhO7j8WgKWcNY0u1c2_pwQmIxWsLy25zu5vuCS4y2GkgKPEawcjcdGGFhQ7XJU9Cvcme37ad7_x2LoXTiOHQl20bPqoQm-Xexq_YwQPA0X1fytaQzn-aCrETNNkRTDBDpip0NGImv1lQiAQMRQLcc; sid_guard=140a336dd81551eaa30bc0e9e8d336fd%7C1664383681%7C5183999%7CSun%2C+27-Nov-2022+16%3A48%3A00+GMT; uid_tt=1e4686eabe61b69fd57f1db3639b39b0; uid_tt_ss=1e4686eabe61b69fd57f1db3639b39b0; sid_tt=140a336dd81551eaa30bc0e9e8d336fd; sessionid=140a336dd81551eaa30bc0e9e8d336fd; sessionid_ss=140a336dd81551eaa30bc0e9e8d336fd; msToken=DefrbKNjA4krIh7tp5KF_ZXXM1__4BIGoe2_r-2pbIFokQpdlsAe8eodr9epNPRS43Yu3Wpkh4HktFYRO-i2ASuuPCj8e7LOFmIy0hm1yEw=; odin_tt=6d9c36866ea653b47da090d72de41c3dc7d3fd9ee612b92b9ebc39c13771ad6b673778a8226572dd44962c023c295f393b1c45e775948b04767e74215842a6f3";
            PayDto payDto = PayDto.builder().ck(ck).device_id("3426504802566350").iid("2107090848974670").pay_type("2")
                    .orderId("4986697737823903299").userIp("183.221.16.53").build();
            String bodyData = PayRiskInfoAndPayInfoUtils.buildPayForm(payDto);
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            // String bodyData = "app_name=aweme&channel=dy_tiny_juyouliang_dy_and24&device_platform=android&iid=3743163984904813&order_id=4983651837194409539&os=android&device_id=2538093503847412&aid=1128&pay_type=1";
//            String url = "https://ec.snssdk.com/order/createpay?device_id=2538093503847412&aid=1128&device_platform=android&device_type=SM-G955N&request_tag_from=h5&app_name=aweme&version_name=17.3.0&app_type=normal&channel=dy_tiny_juyouliang_dy_and24&iid=3743163984904813&version_code=170300&os=android&os_version=5.1.1";
            String url = PayRiskInfoAndPayInfoUtils.buidPayUrl(payDto);
            String X_SS_STUB = SecureUtil.md5(bodyData).toUpperCase();
            String signData = String.format("{\"header\": {\"X-SS-STUB\": \"%s\",\"deviceid\": \"\",\"ktoken\": \"\",\"cookie\" : \"\"},\"url\": \"%s\"}",
                    X_SS_STUB, url
            );
            String signHt = HttpRequest.post("http://110.42.246.12:8191/tt1213").body(signData).execute().body();
            String x_gorgon = JSON.parseObject(signHt).getString("x-gorgon");
            String x_khronos = JSON.parseObject(signHt).getString("x-khronos");
            RequestBody body = RequestBody.create(mediaType, bodyData);
            Request.Builder builder = new Request.Builder();
            Map<String, String> header = PreUtils.buildIpMap("223.104.24.246");
            for (String s : header.keySet()) {
                builder.header(s, header.get(s));
            }
            Request request = builder.url(url)
                    .post(body)
                    .addHeader("X-SS-STUB", X_SS_STUB)
                    .addHeader("Cookie", ck)
                    .addHeader("X-Gorgon", x_gorgon)
                    .addHeader("X-Khronos", x_khronos)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            Response response = client.newCall(request).execute();
            String payData = response.body().string();
            log.info("支付消息返回数据msg:{}", payData);
            String payUrl = JSON.parseObject(JSON.parseObject(JSON.parseObject(JSON.parseObject(payData).getString("data")).getString("data"))
                    .getString("sdk_info")).getString("url");
            System.out.println(payUrl);
            response.close();
            String payReUrl = "https://ds.alipay.com/?from=mobilecodec&scheme="
                    + URLEncoder.encode("alipayqr://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=") + payUrl;
//            System.err.println(payReUrl);
            //alipays://platformapi/startapp?appId=20000067&url=http%3A%2F%2F134.122.134.69%3A8082%2Frecharge%2Fzfb%3Forder_id%3DSP2210012316069040391319127864
            payReUrl = String.format("alipays://platformapi/startapp?appId=20000067&url=%s", URLEncoder.encode("http://gkd7ji.natappfree.cc/1.html"));
            System.err.println(payReUrl);

        } catch (Exception e) {
            log.error("支付报错msg:{}", e.getMessage());
        }
    }
}
