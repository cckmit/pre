package com.xd.pre.jddj.douy;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.xd.pre.pcScan.Demo;
import okhttp3.*;

import java.util.Map;

public class Douycheck {
    public static void main(String[] args) throws Exception {
        Map<String, String> ipAndPort = Douyin3.getIpAndPort();
        OkHttpClient client = Demo.getOkHttpClient(ipAndPort.get("ip"), Integer.valueOf(ipAndPort.get("port")));
        String check = check(client, "13568504866");
        System.out.println(check);

    }

    public static String check(OkHttpClient client, String phone) throws Exception {
        String phoneBash64 = JavaJs.initialToolbarSDK(phone);
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        String bodyData = "auto_read=0&account_sdk_source=app&mix_mode=1&type=31&unbind_exist=35&mobile=" + phoneBash64 + "&device_platform=android&os=android&ssmix=a&_rticket=1664334265430&cdid=827fe943-9487-42d6-abcf-a7e95be72a21&channel=tengxun_jg_tt_0706&aid=13&app_name=news_article&version_code=896&version_name=8.9.6&manifest_version_code=8960&update_version_code=89607&ab_version=1859937%2C668779%2C4761879%2C662176%2C4761866%2C668775%2C4137891%2C4413283%2C4689322%2C4761884%2C4790573%2C668774%2C4761873%2C662099%2C4761838%2C668776%2C4761874%2C660830%2C4761882%2C4838812%2C3596064%2C4775526&ab_group=94567%2C102753&ab_feature=102749%2C94563&resolution=900*1600&dpi=320&device_type=M2007J22C&device_brand=Xiaomi&language=zh&os_api=25&os_version=7.1.2&ac=wifi&dq_param=0&immerse_pool_type=-2&session_id=b23c6776-670e-4cfa-b23f-bd0ee06b21fd&rom_version=25&plugin=0&isTTWebView=0&host_abi=armeabi-v7a&client_vid=2827920%2C4681423%2C3194524%2C4539075%2C3383553&iid=4024638567632376&device_id=136765450490936";
        String X_SS_STUB1 = SecureUtil.md5(bodyData).toUpperCase();
        String url1 = "https://security.snssdk.com/passport/mobile/send_code/v1/?passport-sdk-version=30858&device_platform=android&os=android&ssmix=a&_rticket=1664334265431&cdid=827fe943-9487-42d6-abcf-a7e95be72a21&channel=tengxun_jg_tt_0706&aid=13&app_name=news_article&version_code=896&version_name=8.9.6&manifest_version_code=8960&update_version_code=89607&ab_version=1859937%2C668779%2C4761879%2C662176%2C4761866%2C668775%2C4137891%2C4413283%2C4689322%2C4761884%2C4790573%2C668774%2C4761873%2C662099%2C4761838%2C668776%2C4761874%2C660830%2C4761882%2C4838812%2C3596064%2C4775526&ab_group=94567%2C102753&ab_feature=102749%2C94563&resolution=900*1600&dpi=320&device_type=M2007J22C&device_brand=Xiaomi&language=zh&os_api=25&os_version=7.1.2&ac=wifi&dq_param=0&immerse_pool_type=-2&session_id=b23c6776-670e-4cfa-b23f-bd0ee06b21fd&rom_version=25&plugin=0&isTTWebView=0&host_abi=armeabi-v7a&client_vid=2827920%2C4681423%2C3194524%2C4539075%2C3383553&iid=4024638567632376&device_id=136765450490936&okhttp_version=4.1.103.9-dut&ttnet_version=4.1.103.9-dut";
        String signData1 = String.format("{\"header\": {\"X-SS-STUB\": \"%s\",\"deviceid\": \"\",\"ktoken\": \"\",\"cookie\" : \"\"},\"url\": \"%s\"}",
                X_SS_STUB1, url1
        );
        String signHt1 = HttpRequest.post("http://110.42.246.12:8191/tt1213").body(signData1).execute().body();
        String x_gorgon1 = JSON.parseObject(signHt1).getString("x-gorgon");
        String x_khronos1 = JSON.parseObject(signHt1).getString("x-khronos");
        RequestBody body = RequestBody.create(mediaType, bodyData);
        Request request = new Request.Builder()
                .url(url1)
                .post(body)
                .addHeader("X-SS-STUB", X_SS_STUB1)
                .addHeader("x-tt-passport-csrf-token", "693fe4a9f8f50acb3d5ebb6850346f7e")
                .addHeader("X-SS-REQ-TICKET", "1664294870626")
                .addHeader("x-tt-dt", "AAAQQBIQBUE7R6UFIVGWROCTIJQX5WKVYZNJCNSNWTYB55CA3Z6RCCKLLHYBRJDBU22GSBITBYONI47LO7C7G2EAOYVGJRLWM5AQ")
                .addHeader("passport-sdk-version", "30858")
                .addHeader("sdk-version", "2")
                .addHeader("x-vc-bdturing-sdk-version", "2.2.1.cn")
                .addHeader("x-tt-local-region", "unknown")
                .addHeader("x-tt-store-region-did", "none")
                .addHeader("x-tt-store-region-uid", "none")
                .addHeader("User-Agent", "com.ss.android.article.news/8960 (Linux; U; Android 7.1.2; zh_CN; M2007J22C; Build/QP1A.190711.020;tt-ok/3.12.13.1)")
                .addHeader("X-Gorgon", x_gorgon1)
                .addHeader("X-Khronos", x_khronos1)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Host", "security.snssdk.com")
                .addHeader("Connection", "Keep-Alive")
                .addHeader("Cookie", "passport_csrf_token=693fe4a9f8f50acb3d5ebb6850346f7e; passport_csrf_token_default=693fe4a9f8f50acb3d5ebb6850346f7e; install_id=4024638567632376; ttreq=1$f36666a2a03776db4dbd01c5d3ac50e19221a925; odin_tt=61392fe066c8cebf7dc66271b3f8301681392c20c29e3dd8db4e316e34190ec99433374813d3b721425f4981c0fbbafa6a8da3885fc75d3dfec1490bba9f93a5")
                .build();
        Response response = client.newCall(request).execute();
        String errorMsg = response.body().string();
        response.close();
        return errorMsg;
    }
}
