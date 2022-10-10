package com.xd.pre.jddj.douy;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.net.URLEncoder;

@Slf4j
public class PayByOreder {
    public static void main(String[] args) throws Exception {
        String url  = "https://ec.snssdk.com/order/createpay?b_type_new=3&sub_b_type=13&ecom_appid=7386&webcast_appid=6822&live_request_from_jsb=1&live_sdk_version=896&webcast_sdk_version=2070&webcast_gps_access=2&webcast_app_id=6822&app_name=news_article&openlive_personal_recommend=1&device_platform=android&os=android&channel=tengxun_jg_tt_0706&aid=13&version_code=896&version_name=8.9.6&manifest_version_code=8960";
        OkHttpClient client = new OkHttpClient();
        String bodyData1 = "{\"b_type\":3,\"env_type\":\"2\",\"activity_id\":\"\",\"origin_type\":\"0\",\"origin_id\":\"0\",\"new_source_type\":\"0\",\"new_source_id\":\"0\",\"source_type\":\"0\",\"source_id\":\"0\",\"schema\":\"snssdk143://\",\"extra\":\"{}\",\"entrance_params\":\"{}\",\"order_id\":\"4983256867704290883\",\"sub_way\":0,\"pay_type\":1,\"pay_risk_info\":\"{\\\"biometric_params\\\":\\\"1\\\",\\\"is_jailbreak\\\":\\\"2\\\",\\\"openudid\\\":\\\"\\\",\\\"order_page_style\\\":0,\\\"checkout_id\\\":3,\\\"ecom_payapi\\\":true,\\\"ip\\\":\\\"183.221.16.173\\\"}\",\"pay_amount_composition\":[],\"pay_info\":\"{\\\"sdk_version\\\":\\\"v2\\\",\\\"dev_info\\\":{\\\"reqIp\\\":\\\"183.221.16.173\\\",\\\"os\\\":\\\"android\\\",\\\"isH5\\\":false,\\\"cjSdkVersion\\\":\\\"6.3.5\\\",\\\"aid\\\":\\\"13\\\",\\\"ua\\\":\\\"com.ss.android.article.news/8960 (Linux; U; Android 10; zh_CN; PACT00; Build/QP1A.190711.020; Cronet/TTNetVersion:68deaea9 2022-07-19 QuicVersion:12a1d5c5 2022-06-27)\\\",\\\"riskUa\\\":\\\"\\\",\\\"lang\\\":\\\"zh-Hans\\\",\\\"deviceId\\\":\\\"321506381413262\\\",\\\"osVersion\\\":\\\"10\\\",\\\"vendor\\\":\\\"\\\",\\\"model\\\":\\\"\\\",\\\"netType\\\":\\\"\\\",\\\"appVersion\\\":\\\"8.9.6\\\",\\\"appName\\\":\\\"news_article\\\",\\\"devicePlatform\\\":\\\"android\\\",\\\"deviceType\\\":\\\"PACT00\\\",\\\"channel\\\":\\\"tengxun_jg_tt_0706\\\",\\\"openudid\\\":\\\"\\\",\\\"versionCode\\\":\\\"896\\\",\\\"ac\\\":\\\"wifi\\\",\\\"brand\\\":\\\"OPPO\\\",\\\"iid\\\":\\\"3444096406694551\\\",\\\"bioType\\\":\\\"1\\\"},\\\"bank_card_info\\\":{},\\\"credit_pay_info\\\":{\\\"installment\\\":\\\"1\\\"},\\\"zg_ext_param\\\":\\\"{\\\\\\\"decision_id\\\\\\\":\\\\\\\"659356656346136_1664349071251661\\\\\\\",\\\\\\\"qt_c_pay_url\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"retain_c_pay_url\\\\\\\":\\\\\\\"\\\\\\\"}\\\",\\\"voucher_no_list\\\":[],\\\"jh_ext_info\\\":\\\"{\\\\\\\"payapi_cache_id\\\\\\\":\\\\\\\"20220928151111251640az7y0b2x1c23\\\\\\\"}\\\",\\\"sub_ext\\\":\\\"\\\",\\\"biometric_params\\\":\\\"1\\\",\\\"is_jailbreak\\\":\\\"2\\\",\\\"checkout_id\\\":3,\\\"pay_amount_composition\\\":[]}\"}";
        String X_SS_STUB = SecureUtil.md5("json_form=" + URLEncoder.encode(bodyData1)).toUpperCase();
        String signData = String.format("{\"header\": {\"X-SS-STUB\": \"%s\",\"deviceid\": \"\",\"ktoken\": \"\",\"cookie\" : \"\"},\"url\": \"%s\"}",
                X_SS_STUB, url
        );
        String signHt = HttpRequest.post("http://110.42.246.12:8191/tt1213").body(signData).execute().body();
        log.info("msg:{}", signHt);
        String x_gorgon = JSON.parseObject(signHt).getString("x-gorgon");
        String x_khronos = JSON.parseObject(signHt).getString("x-khronos");
        RequestBody requestBody = new FormBody.Builder()
                .add("json_form", bodyData1)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Cookie", "sid_tt=cf88609b562b6fc48a5a830e0683fd87;")
                .addHeader("sdk-version", "2")
                .addHeader("X-SS-STUB", X_SS_STUB)
                .addHeader("User-Agent", "com.ss.android.article.news/8960 (Linux; U; Android 10; zh_CN; PACT00; Build/QP1A.190711.020; Cronet/TTNetVersion:68deaea9 2022-07-19 QuicVersion:12a1d5c5 2022-06-27)")
                .addHeader("X-Gorgon", x_gorgon)
                .addHeader("X-Khronos", x_khronos)
                .build();
        Response response = client.newCall(request).execute();
        String string = response.body().string();
        System.out.println(string);
    }
}
