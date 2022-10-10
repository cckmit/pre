package com.xd.pre.testPay;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.common.utils.px.dto.UrlEntity;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

@Slf4j
public class TestPay {
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 1; i++) {
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType,
                    String.format("{\"mch_id\": \"1\",\"subject\": \"支付1000元\",\"body\": \"支付1000元\",\"out_trade_no\": " +
                                    "\"%s\",\"amount\": \"%s.00\",\"notify_url\": \"http://210.16.122.100/pre/jd/callbackTemp\",\"return_url\": \"http://1111.com/success.html\",\"timestamp\": \"2014-07-24 03:07:50\",\"sign\": \"c815c971fdabbda20f8fdf7d0ee658ff\",\"client_ip\":\"192.168.2.1\",\"pass_code\":\"8\"}",
                            PreUtils.getRandomString(15), (i + 1) * 100 + ""));
            Request request = new Request.Builder()
                    .url("http://210.16.122.100/api/px/createOrder")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            String urlData = response.body().string();
            log.info("外部请求地址msg:{}", urlData);
            String pay_url = JSON.parseObject(JSON.parseObject(urlData).getString("data")).getString("pay_url");
            //http://210.16.122.100/api/px/pay?orderId=202210051149015264&sign=16764b9b9f7d904a14ea8037a3b92a60
            UrlEntity urlEntity = PreUtils.parseUrl(pay_url);
            HttpRequest.get(String.format("http://210.16.122.100/api/px/pay?orderId=%s&sign=%s",
                    urlEntity.getParams().get("orderId"), urlEntity.getParams().get("sign"))).execute();
        }
    }
}
