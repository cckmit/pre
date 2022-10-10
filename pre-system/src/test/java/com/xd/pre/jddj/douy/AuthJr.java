package com.xd.pre.jddj.douy;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

@Slf4j
public class AuthJr {
    public static void main(String[] args) throws Exception {
        String url = "https://api5-normal-c-lq.amemv.com/passport/open/auth/?iid=2159866761981789&device_id=1341864848921502&ac=wifi&channel=shenmasem_ls_dy_031&aid=1128&app_name=aweme&version_code=220400&version_name=22.4.0&device_platform=android&os=android&ssmix=a&device_type=M2007J22C&device_brand=Xiaomi&language=zh&os_api=25&os_version=7.1.2&manifest_version_code=220401&resolution=900*1600&dpi=320&update_version_code=22409900&_rticket=1664351976744&package=com.ss.android.ugc.aweme&mcc_mnc=46002&cpu_support64=false&host_abi=armeabi-v7a&ts=1664351976&is_guest_mode=0&app_type=normal&appTheme=light&need_personal_recommend=1&minor_status=0&is_android_pad=0&cdid=bc44a4bf-bae5-47bd-9b4e-13956862ee3e&md=0";
        String bodyData1 = "scope=user_info%2Cfriend.list%2Cmobile&sign=aea615ab910015038f73c47e45d21466&source=native&mix_mode=1&signature=aea615ab910015038f73c47e45d21466&app_identity=a3aef48b92d9927bcc00b64ec0b65b97&response_type=code&client_key=awikua6yvbqai0ht&state=dy_state&from=opensdk&skip_confirm=true";

        String X_SS_STUB = SecureUtil.md5(bodyData1).toUpperCase();
        String signData = String.format("{\"header\": {\"X-SS-STUB\": \"%s\",\"deviceid\": \"\",\"ktoken\": \"\",\"cookie\" : \"\"},\"url\": \"%s\"}",
                X_SS_STUB, url
        );
        String signHt = HttpRequest.post("http://110.42.246.12:8191/tt1213").body(signData).execute().body();
        log.info("msg:{}", signHt);
        String x_gorgon = JSON.parseObject(signHt).getString("x-gorgon");
        String x_khronos = JSON.parseObject(signHt).getString("x-khronos");
        String bodyStr = HttpRequest.post(url)
                .body(bodyData1)
                .header("Cookie", "passport_csrf_token=221568319a2c6620d5c45ed7f3ba0759; passport_csrf_token_default=221568319a2c6620d5c45ed7f3ba0759; install_id=2159866761981789; ttreq=1$d02416bf0d1a074b9e95b227099104c70a87a2de; d_ticket=353bbfdedda3468df2344f967cbfca197e475; multi_sids=659356656346136%3A35f3079104909107686a51b65e331e54; odin_tt=5d6dfc8b1eb99a9cf94afb5cf0dd776ee719b6b270a88ad7478069e41a0e624863bf214cbf8735f02cba19f1048b974497b152cb78d9d50bda6b4b445ad39ae41f303282ce00c513eedf97280e775b64; n_mh=8nysT__BxDL_VpPZTRMYKZZSN1pywPhZ9o63MSmzGLg; passport_assist_user=CkBB5njj4gOmHkv9Hi5MaHZm2PZLVdUeOTn41LApp1GZvDNrUjFZcdfed9BneqHPHH3l2CbNbhwMvWxGoOA0PALiGkgKPFP0I2oaFtT3DZj59tuMd3CRG7uFHZhFz_IqYqTFxrqCzP3TF5qqlPqcXyTpBJ1fqtzxIHQaa3-q1bL8sxCPiJ0NGImv1lQiAQOGki3w; sid_guard=35f3079104909107686a51b65e331e54%7C1664350751%7C5184000%7CSun%2C+27-Nov-2022+07%3A39%3A11+GMT; uid_tt=7db94ab129a4623642092720425762a7; uid_tt_ss=7db94ab129a4623642092720425762a7; sid_tt=35f3079104909107686a51b65e331e54; sessionid=35f3079104909107686a51b65e331e54; sessionid_ss=35f3079104909107686a51b65e331e54")
                .header("x-tt-dt", "AAAQGPMILDPZZTRY747IVEI3RGKDQM5G34JJPIXZKSBBQN23ZBZWVYQUEWMNNATYN3QB7PHPZ7M6VIHUETUXBBPMKBZNJYCXLNO6CJ6Q4DMFRG3CCNFZPQ26LIWOMDOZ26E4LVO3LBDEG2N4I4B7HUY")
                .header("activity_now_client", "1664351977568")
                .header("X-SS-REQ-TICKET", "1664351976762")
                .header("x-bd-kmsv", "1")
                .header("x-vc-bdturing-sdk-version", "2.2.1.cn")
                .header("passport-sdk-version", "20374")
                .header("sdk-version", "2")
                .header("x-tt-multi-sids", "659356656346136%3A35f3079104909107686a51b65e331e54")
                .header("X-Tt-Token", "0035f3079104909107686a51b65e331e5402e2913fa4ffbb62f4a6efa7d3aebb35a259c06fbae4e0bdf1b134e7bd12a5d0489f86c568144bf5adb643354d347870eab5554265258a445e3661a8b0fb60f34aeccf85c0e50c5a39657d1d7b191fc23e0-1.0.1")
                .header("multi_login", "1")
                .header("X-SS-STUB", "2F5FFF78229CD0A25FA203F544A5EF6X")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-SS-DP", "1128")
                .header("x-tt-trace-id", "00-831bbef00d4c46b41a8139e4c8fb0468-831bbef00d4c46b4-01")
                .header("User-Agent", "com.ss.android.ugc.aweme/220401 (Linux; U; Android 7.1.2; zh_CN; M2007J22C; Build/QP1A.190711.020; Cronet/TTNetVersion:35608bcb 2022-07-15 QuicVersion:6fe86402 2022-05-31)")
//                .header("X-Gorgon", "0404406d40017f5c1b57520029464491cb175cc806563b5e8ce3")
//                .header("X-Khronos", "1664351976")
                .header("X-Argus", "qkTwDw9MMO5JKt8YtaqhE4kizmIxe9qKYnWzJb34cEpZIuEKMH9ilZ9pnzOc25IvvRC06NUd8VheWi5MMYco1HBW/YCKUvwFO34mU45Mn5oKPD219v+6a5Y9BKjY3QrknbgpTnGL7uSuPYp65dw/gWDbr0xK8R+ekfhl/PVrv1I4YwrNJb3D2ECUH7Uqn6V8uz76qne3Qg2x//JKJYRKb9pJVWRZBdNInZbrBnastp0XE56vN1U2C0vP+vqT0HKwYUHBZTMDr2+hwD7C0PdITpFxGLPIqbYIZBFOPL9BXoFRsyYOrbKy5fFjUQjUSytI84GOoSyN3GE0Z5ks9g7DZL0kiXsXxLnXhgndaFltxofWMw==")
                .header("X-Ladon", "6DvrKf2to0OByWRX/PPetvqZxFufgeeYn/jgL7oVcHZDoIpx")
                .execute().body();
        System.out.println(bodyStr);
    }
}
