package com.xd.pre.jddj.douy;

import cn.hutool.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Authr2 {
    public static void main(String[] args) throws Exception {
        String bodyData1 = "need_mobile=0&platform_app_id=664&platform=aweme&change_bind=0&account_sdk_source=app&code=64e2e989117cd215znVArCe2fTZJjg5CftYa&device_platform=android&os=android&ssmix=a&_rticket=1664356120187&cdid=827fe943-9487-42d6-abcf-a7e95be72a21&channel=tengxun_jg_tt_0706&aid=13&app_name=news_article&version_code=896&version_name=8.9.6&manifest_version_code=8960&update_version_code=89607&ab_version=668775%2C4137891%2C4413283%2C4761884%2C4790573%2C662176%2C4761866%2C668776%2C4761874%2C660830%2C4761882%2C4838812%2C1859937%2C668779%2C4761879%2C668774%2C4761873%2C662099%2C4761838%2C3596064%2C4775526&ab_group=94567%2C102753&ab_feature=102749%2C94563&resolution=900*1600&dpi=320&device_type=M2007J22C&device_brand=Xiaomi&language=zh&os_api=25&os_version=7.1.2&ac=wifi&dq_param=0&immerse_pool_type=101&session_id=d8bce4ae-b37f-40d8-94ed-0339a3f92c03&tma_jssdk_version=2.53.0&rom_version=25&plugin=0&isTTWebView=0&host_abi=armeabi-v7a&client_vid=2827920%2C4681423%2C3194524%2C4539075%2C3383553&iid=4024638567632376&device_id=136765450490936";
        String url = "https://security.snssdk.com/passport/auth/bind_with_mobile_login/?passport-sdk-version=30858&device_platform=android&os=android&ssmix=a&_rticket=1664356120207&cdid=827fe943-9487-42d6-abcf-a7e95be72a21&channel=tengxun_jg_tt_0706&aid=13&app_name=news_article&version_code=896&version_name=8.9.6&manifest_version_code=8960&update_version_code=89607&ab_version=668775%2C4137891%2C4413283%2C4761884%2C4790573%2C662176%2C4761866%2C668776%2C4761874%2C660830%2C4761882%2C4838812%2C1859937%2C668779%2C4761879%2C668774%2C4761873%2C662099%2C4761838%2C3596064%2C4775526&ab_group=94567%2C102753&ab_feature=102749%2C94563&resolution=900%2A1600&dpi=320&device_type=M2007J22C&device_brand=Xiaomi&language=zh&os_api=25&os_version=7.1.2&ac=wifi&dq_param=0&immerse_pool_type=101&session_id=d8bce4ae-b37f-40d8-94ed-0339a3f92c03&tma_jssdk_version=2.53.0&rom_version=25&plugin=0&isTTWebView=0&host_abi=armeabi-v7a&client_vid=2827920%2C4681423%2C3194524%2C4539075%2C3383553&iid=4024638567632376&device_id=136765450490936&okhttp_version=4.1.103.9-dut&ttnet_version=4.1.103.9-dut";
        String bodyStr = HttpRequest.post(url)
                .body(bodyData1)
                .header("X-SS-STUB", "67A224F0D5701865DA70E980F2DD8A8F")
                .header("x-tt-passport-csrf-token", "99a8ddf12d6a7eda6cf775f7cdaa5c9d")
                .header("X-SS-REQ-TICKET", "1664356120210")
                .header("x-tt-dt", "AAA3IJXITANBOMY4OP4NR2QU3P4SRU65VRCHVXI3B2MD4MBFNSBFILUEZBLSOLRN4KV2GVX23SBG6WENQ72JOMX2MJBBZIS4CULTBBPLQ4M5VXN7VGKNHUASOUHJXSAW5MZWRVO5GTD6HBH34NT4KQA")
                .header("passport-sdk-version", "30858")
                .header("X-Tt-Token", "00dbca0a8165c5d9aaee46472fd3335ec802165b9ef97a84a8f9792870ce64ad7ed28284d60231a8dfaf9adca343d8937440c8a72aa6b70b6167a7657cc91c1b43d15778f406fe000fa50a518c9b3c4e5ec87db9a4dbe50f753525c900cc9601a78ae-1.0.1")
                .header("sdk-version", "2")
                .header("x-vc-bdturing-sdk-version", "2.2.1.cn")
                .header("x-tt-store-region", "cn-sc")
                .header("x-tt-store-region-src", "did")
                .header("x-tt-store-region-did", "cn-sc")
                .header("x-tt-store-region-uid", "none")
                .header("User-Agent", "com.ss.android.article.news/8960 (Linux; U; Android 7.1.2; zh_CN; M2007J22C; Build/QP1A.190711.020;tt-ok/3.12.13.1)")
                .header("X-Ladon", "134QHy5vmW4etWEyBKYk51qEoBxweAm9XIrfBJLpz0kb4dD0")
                .header("X-Gorgon", "0404801940017610f5c98deccae4ee048857d41847a552621931")
                .header("X-Khronos", "1664356120")
                .header("X-Argus", "yYCc1NsgVNtlJC2RXLP014T8u8nkPUqGcYY5I/tJYce5kQ1ko7brXyNJJbFouFikNwkYSnWMLPMd/vZW+Fq+UYRWjXwMaBvT8KSGZ7wo08vvCiYBskVC+QQZYmjsmXirLo6o2Gv/dvrmwSjmE6cLLeqfg09RdU7ke+VQoQimqLgspMsfLoK+gctRinrIRGMsXf7nWd8S/P77Gp9o5D9mfPZaQELE/HkuTKRXS+0QOVX0x+JME14I4SUPNyOjQtaBtreMf8tp9AVbqUNzbcW+7Se9")
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("Content-Length", "1030")
                .header("Host", "security.snssdk.com")
                .header("Connection", "Keep-Alive")
                .header("Cookie", "passport_csrf_token=99a8ddf12d6a7eda6cf775f7cdaa5c9d; passport_csrf_token_default=99a8ddf12d6a7eda6cf775f7cdaa5c9d; install_id=4024638567632376; ttreq=1$f36666a2a03776db4dbd01c5d3ac50e19221a925; n_mh=8nysT__BxDL_VpPZTRMYKZZSN1pywPhZ9o63MSmzGLg; d_ticket=88bde8a3fe854f8af464001f3c6c1ccdb0dc7; sid_guard=dbca0a8165c5d9aaee46472fd3335ec8%7C1664356094%7C21600%7CWed%2C+28-Sep-2022+15%3A08%3A14+GMT; uid_tt=19fdd6112bb5b597b96aa3c9404e8867; uid_tt_ss=19fdd6112bb5b597b96aa3c9404e8867; sid_tt=dbca0a8165c5d9aaee46472fd3335ec8; sessionid=dbca0a8165c5d9aaee46472fd3335ec8; sessionid_ss=dbca0a8165c5d9aaee46472fd3335ec8; odin_tt=9bf2fb2f960c447377bce2518164adbdaa05caa88cdf8645bfa96b876426c68cc84209a7881afa28102341c65b4fcdc90fa7fdd496c670a6a680295858d135d1; msToken=-ziQcinjvzyDd6MheIZo0IKmLgPf1RBxZMBZYu4l7BGxIo2EMgD1m8VRV1uZg0DtnndYYdeeTwNH9BXyUHv5E9x7MDSj2jmfmAIfi702rYk=")
                .header("cache-control", "no-cache")
                .execute().body();
        System.out.println(bodyStr);
    }
}
