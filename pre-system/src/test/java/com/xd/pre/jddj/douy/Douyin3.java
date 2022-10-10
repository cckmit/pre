package com.xd.pre.jddj.douy;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.modules.px.douyin.buyRender.res.BuyRenderRoot;
import com.xd.pre.pcScan.Demo;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Douyin3 {
    public static Map<String, String> getIpAndPort() {
        String s = HttpUtil.get("http://webapi.http.zhimacangku.com/getip?num=1&type=1&pro=&city=0&yys=0&port=1&time=1&ts=0&ys=0&cs=0&lb=1&sb=0&pb=4&mr=1&regions=");
        String[] split = s.split(":");
        HashMap<String, String> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("ip", split[0]);
        stringObjectHashMap.put("port", split[1].replace("\r\n", ""));
        return stringObjectHashMap;
    }

    public static void main(String[] args) throws Exception {
//        String ck = "install_id=119169286681983; ttreq=1$95c9258aaed0231ca4e6c6a888d5227c28dc0621; passport_csrf_token=804daba8a1c414854c8778d212a35af9; passport_csrf_token_default=804daba8a1c414854c8778d212a35af9;sid_tt=8bd98afb08a175ebb2d91b6ff52c79a2; msToken=aVv2E_vYKIt16_8Rh8Yet4giUQ4gJ19t46WBFTKsbJ4JFjSsbH-o2_xu0EFsFVNLSJynyC6N1Q5f_Z2YKLF6II9AahyugFtn4jAWGYH8fKw=; odin_tt=5891617e7276cf20a7811516e68697efed77d7f86c1c6616bb4e5785c6f50be02eb1d13a913433e922011760d1f30f277b44585d6b7f8554c2e7971c28bd62c7";
//        String ck = "sid_tt=a3318efaec2c7e35aeba9231ce16d7b4;";
        String ck = "msToken=ozB0lzQlZSfhMO89FyaM9bIFkH2WlQSp5QsrNAB5K4JWCzI7XYXbHX9nRsbDovL60oL057eXVQPvL8hHRCFDAJHWNq5FtmjocWmsJrizJK0=; __ac_signature=_02B4Z6wo00f01q5Y1bAAAIDDJRNOU8iwKl6ueNEAAMiu25; tt_webid=7148348565085308423; ttcid=f5809ef849264c0794131bec5745f7cf24; local_city_cache=%E8%AE%B8%E6%98%8C; csrftoken=9832f0c55d57ce5944f19282e117eb29; s_v_web_id=verify_l8ldokao_uzOgirXl_kcRt_4LEP_9xYG_kddHuz8SSB4v; _tea_utm_cache_24=undefined; MONITOR_WEB_ID=2f58f1fc-395f-490b-b74b-aaeff0d42309; passport_csrf_token=49fb2e94e91a1bb46ea22b9953d85a33; passport_csrf_token_default=49fb2e94e91a1bb46ea22b9953d85a33; n_mh=zZgm57pXJ9eU1op-6-gPBKq0dTvW4KJntl42JmCxz_c; sso_auth_status=34ce743ea769dc8c3205895a69c7ae7f%2Cdc238a694f615cf1b2fd0d8fb9a9b1ea; sso_auth_status_ss=34ce743ea769dc8c3205895a69c7ae7f%2Cdc238a694f615cf1b2fd0d8fb9a9b1ea; sso_uid_tt=868d09e198c8214a51d906ad86cc53bf; sso_uid_tt_ss=868d09e198c8214a51d906ad86cc53bf; toutiao_sso_user=e5860adfdff1f820b9a58c0deae7c10b; toutiao_sso_user_ss=e5860adfdff1f820b9a58c0deae7c10b; sid_ucp_sso_v1=1.0.0-KDhlYTFmNDkzYWE1OGNkYTRhYzY5Y2Y1NDYxOTJhYjMxM2Q4MGYzZDkKFgjs2aCNtIyLBxCattCZBhgYOAJA7AcaAmxxIiBlNTg2MGFkZmRmZjFmODIwYjlhNThjMGRlYWU3YzEwYg; ssid_ucp_sso_v1=1.0.0-KDhlYTFmNDkzYWE1OGNkYTRhYzY5Y2Y1NDYxOTJhYjMxM2Q4MGYzZDkKFgjs2aCNtIyLBxCattCZBhgYOAJA7AcaAmxxIiBlNTg2MGFkZmRmZjFmODIwYjlhNThjMGRlYWU3YzEwYg; passport_auth_status=0caacba2e580049dbcf0ef990cbb5c73%2C51b7825aaf2f76f54abad7b64d92449f; passport_auth_status_ss=0caacba2e580049dbcf0ef990cbb5c73%2C51b7825aaf2f76f54abad7b64d92449f; sid_guard=92c772a01b8ab2042d76866993b43978%7C1664359195%7C5183999%7CSun%2C+27-Nov-2022+09%3A59%3A54+GMT; uid_tt=0ce88c26b2f2b929150896c4f1eb5ae9; uid_tt_ss=0ce88c26b2f2b929150896c4f1eb5ae9; sid_tt=92c772a01b8ab2042d76866993b43978; sessionid=92c772a01b8ab2042d76866993b43978; sessionid_ss=92c772a01b8ab2042d76866993b43978; sid_ucp_v1=1.0.0-KDFmZmQ2ZmMxYWI4NzI3MzgzZDQ5Y2FiMTdiYzI5ZWRlYjExOThiMDEKGAjs2aCNtIyLBxCbttCZBhgYIAw4AkDsBxoCaGwiIDkyYzc3MmEwMWI4YWIyMDQyZDc2ODY2OTkzYjQzOTc4; ssid_ucp_v1=1.0.0-KDFmZmQ2ZmMxYWI4NzI3MzgzZDQ5Y2FiMTdiYzI5ZWRlYjExOThiMDEKGAjs2aCNtIyLBxCbttCZBhgYIAw4AkDsBxoCaGwiIDkyYzc3MmEwMWI4YWIyMDQyZDc2ODY2OTkzYjQzOTc4; tt_anti_token=fbYxXJAcEtb-1ad10962e492fbb10fbc6432aa88c851e0ba89b2cc6c68bf9e1a40ef7e498c9c; tt_scid=jE5og96i1s.feae3Q8OCq-jw6ZqEzYXYj1JhrK-EA8tdMQH-v6NbgzuftQi9W8zO80a4; ttwid=1%7C_-L7IH1KUJKKL0X1Y-1GdzrclEKiLyw59T7lpq-p44s%7C1664359199%7Cf206c4def6619d119113e67910548c77772c14e43e60081348485d3994fb622d; odin_tt=b7be92a979b0939beb89ffa5e99c72cd6d00efd9a68e5d061850720fe13cdea3807a9ae3babc42ec779c89fdeae7ac5c";
//        String ck = "d_ticket=cc347104c8af39c150f54d1ddf85878c40a42; odin_tt=370b7180e150ac2f58155ec2e766de4e5535ddd45f1efa1ae9e9e5aa3ec652304a2e9f8b55c0d13203b6d9384d4cbcfd6c873a17388ea57d048e21534d0a4910df2df856e53dafef1f94565eb5d52dde; odin_tt=542480c311d93e77c80eedfcf2286248730bfd7900ad73e0f688c18bc490bf0f8ff72e7c761b3c19db5ba42121f33fa033fa7dce9d926009682872fd9436f2ecbad4d2a18ede3bb90e2c65c8c4ab2ec6; n_mh=_F5Ny6fLZ_igOUsisyoqLoC-8KYERmIuq2gBbP6QtxI; sid_guard=a25159e7fab694b64a23af73810b2427%7C1664355597%7C5184000%7CSun%2C+27-Nov-2022+08%3A59%3A57+GMT; uid_tt=ab3fc4adc1e10f83553f6419360aa82b; uid_tt_ss=ab3fc4adc1e10f83553f6419360aa82b; sid_tt=a25159e7fab694b64a23af73810b2427; sessionid=a25159e7fab694b64a23af73810b2427; sessionid_ss=a25159e7fab694b64a23af73810b2427; reg-store-region=; passport_csrf_token=5f0bea6e93e5dc497c13295271710ad7; passport_csrf_token_default=5f0bea6e93e5dc497c13295271710ad7; install_id=4147784148851319; ttreq=1$15963d544c9bb04ff72fc63735bb80124657e724";
        String url = "https://ken.snssdk.com/order/buyRender?b_type_new=3&sub_b_type=13&ecom_appid=7386&webcast_appid=6822&live_request_from_jsb=1&live_sdk_version=896&webcast_sdk_version=2070&webcast_language=zh&webcast_locale=zh_CN&webcast_gps_access=2&webcast_app_id=6822&app_name=news_article&openlive_personal_recommend=1&device_platform=android&os=android&ssmix=a&_rticket=1664267930082&cdid=4543b4be-f010-4426-8ed5-767e7a2c9aac&channel=oppo_13_64&aid=13&version_code=896&version_name=8.9.6&manifest_version_code=8960&update_version_code=89607&ab_version=668779%2C4761879%2C662176%2C4761866%2C662099%2C4761838%2C668776%2C4761874%2C1859937%2C668774%2C4761873%2C660830%2C4761882%2C4838690%2C668775%2C4091914%2C4394174%2C4407627%2C4689321%2C4761884%2C4778839%2C4790596%2C3540006%2C3596064%2C4775593&ab_group=94565%2C102755&ab_feature=94563%2C102749&resolution=1080*2200&dpi=480&device_type=PACT00&device_brand=OPPO&language=zh&os_api=29&os_version=10&ac=wifi&dq_param=0&plugin=0&client_vid=4539074%2C3194525%2C3383553%2C2827920%2C4681421&isTTWebView=0&host_abi=arm64-v8a&tma_jssdk_version=2.53.0&rom_version=coloros_v7.1_pact00_11_f.27&immerse_pool_type=101&iid=119169286681983&device_id=321506381413262";
        String body = "{\"address\":null,\"display_scene\":\"buy_again\",\"platform_coupon_id\":null,\"kol_coupon_id\":null,\"auto_select_best_coupons\":true,\"customize_pay_type\":\"{\\\"checkout_id\\\":1,\\\"bio_type\\\":\\\"1\\\"}\",\"first_enter\":true,\"source_type\":\"2\",\"shape\":0,\"marketing_channel\":\"\",\"forbid_redpack\":false,\"support_redpack\":true,\"use_marketing_combo\":false,\"entrance_params\":\"{\\\"previous_page\\\":\\\"toutiao_mytab\\\",\\\"new_source_type\\\":\\\"order_list_page\\\",\\\"carrier_source\\\":\\\"order_list_page\\\",\\\"source_method\\\":\\\"order_buy_once\\\",\\\"is_groupbuying\\\":0}\",\"shop_requests\":[{\"shop_id\":\"GceCTPIk\",\"product_requests\":[{\"product_id\":\"3556357046087622442\",\"sku_id\":\"1736502463777799\",\"sku_num\":1,\"new_source_type\":\"order_list_page\",\"select_privilege_properties\":[]}]}]}";
        String X_SS_STUB = SecureUtil.md5("json_form=" + URLEncoder.encode(body)).toUpperCase();
        String signData = String.format("{\"header\": {\"X-SS-STUB\": \"%s\",\"deviceid\": \"\",\"ktoken\": \"\",\"cookie\" : \"\"},\"url\": \"%s\"}",
                X_SS_STUB, url
        );
        String signHt = HttpRequest.post("http://110.42.246.12:8191/tt1213").body(signData).execute().body();
        log.info("msg:{}", signHt);
        String x_gorgon = JSON.parseObject(signHt).getString("x-gorgon");
        String x_khronos = JSON.parseObject(signHt).getString("x-khronos");
        String tarceid = JSON.parseObject(signHt).getString("tarceid");
        Map<String, String> ipAndPort = getIpAndPort();
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ipAndPort.get("ip"), Integer.valueOf(ipAndPort.get("port"))));
        String resBody = HttpRequest.post(url)
                .form("json_form", body)
                .header("Cookie", ck)
                .setProxy(proxy)
                .header("sdk-version", "2")
                .header("X-SS-STUB", X_SS_STUB)
                .header("x-tt-trace-id", tarceid)
                .header("User-Agent", "com.ss.android.article.news/8960 (Linux; U; Android 10; zh_CN; PACT00; Build/QP1A.190711.020; Cronet/TTNetVersion:68deaea9 2022-07-19 QuicVersion:12a1d5c5 2022-06-27)")
                .header("X-Gorgon", x_gorgon)
                .header("X-Khronos", x_khronos)
                .execute().body();
        log.info("预下单数据：{}", resBody);
        BuyRenderRoot buyRenderRoot = JSON.parseObject(JSON.parseObject(resBody).getString("data"), BuyRenderRoot.class);
        String bodyData1 = String.format("{\"area_type\":\"169\",\"receive_type\":1,\"travel_info\":{\"departure_time\":0,\"trave_type\":1,\"trave_no\":\"\"},\"pickup_station\":\"\",\"traveller_degrade\":\"\",\"b_type\":3,\"env_type\":\"2\",\"activity_id\":\"\",\"origin_type\":\"3002070010\",\"origin_id\":\"99514375927_3556357046087622442\",\"new_source_type\":\"product_detail\",\"new_source_id\":\"0\",\"source_type\":\"0\",\"source_id\":\"0\",\"schema\":\"snssdk143://\",\"extra\":\"{\\\"page_type\\\":\\\"lynx\\\",\\\"alkey\\\":\\\"1128_99514375927_0_3556357046087622442_010\\\",\\\"c_biz_combo\\\":\\\"8\\\",\\\"render_track_id\\\":\\\"%s\\\",\\\"risk_info\\\":\\\"{\\\\\\\"biometric_params\\\\\\\":\\\\\\\"1\\\\\\\",\\\\\\\"is_jailbreak\\\\\\\":\\\\\\\"2\\\\\\\",\\\\\\\"openudid\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"order_page_style\\\\\\\":0,\\\\\\\"checkout_id\\\\\\\":1,\\\\\\\"ecom_payapi\\\\\\\":true,\\\\\\\"ip\\\\\\\":\\\\\\\"39.144.42.162\\\\\\\",\\\\\\\"sub_order_info\\\\\\\":[]}\\\"}\",\"marketing_plan_id\":\"%s\",\"s_type\":\"\",\"entrance_params\":\"{\\\"order_status\\\":4,\\\"previous_page\\\":\\\"toutiao_mytab__order_list_page\\\",\\\"carrier_source\\\":\\\"order_detail\\\",\\\"ecom_scene_id\\\":\\\"1041\\\",\\\"room_id\\\":\\\"\\\",\\\"promotion_id\\\":\\\"\\\",\\\"author_id\\\":\\\"\\\",\\\"group_id\\\":\\\"\\\",\\\"anchor_id\\\":\\\"\\\",\\\"source_method\\\":\\\"open_url\\\",\\\"ecom_group_type\\\":\\\"\\\",\\\"module_label\\\":\\\"\\\",\\\"ecom_icon\\\":\\\"\\\",\\\"brand_verified\\\":\\\"0\\\",\\\"discount_type\\\":\\\"\\\",\\\"full_return\\\":\\\"0\\\",\\\"is_activity_banner\\\":0,\\\"is_exist_size_tab\\\":\\\"0\\\",\\\"is_groupbuying\\\":\\\"0\\\",\\\"is_package_sale\\\":\\\"0\\\",\\\"is_replay\\\":\\\"0\\\",\\\"is_short_screen\\\":\\\"0\\\",\\\"is_with_video\\\":1,\\\"label_name\\\":\\\"\\\",\\\"market_channel_hot_fix\\\":\\\"\\\",\\\"rank_id_source\\\":\\\"\\\",\\\"show_dou_campaign\\\":0,\\\"show_rank\\\":\\\"not_in_rank\\\",\\\"upfront_presell\\\":0,\\\"warm_up_status\\\":\\\"0\\\",\\\"auto_coupon\\\":0,\\\"coupon_id\\\":\\\"\\\",\\\"with_sku\\\":\\\"0\\\",\\\"item_id\\\":\\\"0\\\",\\\"commodity_id\\\":\\\"3556357046087622442\\\",\\\"commodity_type\\\":6,\\\"product_id\\\":\\\"3556357046087622442\\\",\\\"extra_campaign_type\\\":\\\"\\\"}\",\"sub_b_type\":\"3\",\"gray_feature\":\"PlatformFullDiscount\",\"sub_way\":0,\"pay_type\":1,\"post_addr\":{\"province\":{},\"city\":{},\"town\":{},\"street\":{\"id\":\"\",\"name\":\"\"}},\"post_tel\":\"%s\",\"address_id\":\"0\",\"price_info\":{\"origin\":1000,\"freight\":0,\"coupon\":0,\"pay\":1000},\"pay_info\":\"{\\\"sdk_version\\\":\\\"v2\\\",\\\"dev_info\\\":{\\\"reqIp\\\":\\\"39.144.42.162\\\",\\\"os\\\":\\\"android\\\",\\\"isH5\\\":false,\\\"cjSdkVersion\\\":\\\"6.3.5\\\",\\\"aid\\\":\\\"13\\\",\\\"ua\\\":\\\"com.ss.android.article.news/8960+(Linux;+U;+Android+10;+zh_CN;+PACT00;+Build/QP1A.190711.020;+Cronet/TTNetVersion:68deaea9+2022-07-19+QuicVersion:12a1d5c5+2022-06-27)\\\",\\\"riskUa\\\":\\\"\\\",\\\"lang\\\":\\\"zh-Hans\\\",\\\"deviceId\\\":\\\"321506381413262\\\",\\\"osVersion\\\":\\\"10\\\",\\\"vendor\\\":\\\"\\\",\\\"model\\\":\\\"\\\",\\\"netType\\\":\\\"\\\",\\\"appVersion\\\":\\\"8.9.6\\\",\\\"appName\\\":\\\"news_article\\\",\\\"devicePlatform\\\":\\\"android\\\",\\\"deviceType\\\":\\\"PACT00\\\",\\\"channel\\\":\\\"oppo_13_64\\\",\\\"openudid\\\":\\\"\\\",\\\"versionCode\\\":\\\"896\\\",\\\"ac\\\":\\\"wifi\\\",\\\"brand\\\":\\\"OPPO\\\",\\\"iid\\\":\\\"119169286681983\\\",\\\"bioType\\\":\\\"1\\\"},\\\"credit_pay_info\\\":{\\\"installment\\\":\\\"1\\\"},\\\"bank_card_info\\\":{},\\\"voucher_no_list\\\":[],\\\"zg_ext_param\\\":\\\"{\\\\\\\"decision_id\\\\\\\":\\\\\\\"%s\\\\\\\",\\\\\\\"qt_c_pay_url\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"retain_c_pay_url\\\\\\\":\\\\\\\"\\\\\\\"}\\\",\\\"jh_ext_info\\\":\\\"{\\\\\\\"payapi_cache_id\\\\\\\":\\\\\\\"%s\\\\\\\"}\\\",\\\"sub_ext\\\":\\\"\\\",\\\"biometric_params\\\":\\\"1\\\",\\\"is_jailbreak\\\":\\\"2\\\",\\\"order_page_style\\\":0,\\\"checkout_id\\\":1,\\\"pay_amount_composition\\\":[]}\",\"render_token\":\"%s\",\"win_record_id\":\"\",\"marketing_channel\":\"\",\"identity_card_id\":\"\",\"pay_amount_composition\":[],\"user_account\":{},\"queue_count\":0,\"store_id\":\"\",\"shop_id\":\"GceCTPIk\",\"combo_id\":\"1736502463777799\",\"combo_num\":1,\"product_id\":\"3556357046087622442\",\"buyer_words\":\"\",\"stock_info\":[{\"stock_type\":1,\"stock_num\":1,\"sku_id\":\"1736502463777799\",\"warehouse_id\":\"0\"}],\"warehouse_id\":0,\"coupon_info\":{},\"freight_insurance\":false,\"cert_insurance\":false,\"allergy_insurance\":false,\"room_id\":\"\",\"author_id\":\"\",\"content_id\":\"0\",\"promotion_id\":\"\",\"ecom_scene_id\":\"1041\",\"shop_user_id\":\"\",\"group_id\":\"\",\"privilege_tag_keys\":[],\"select_privilege_properties\":[],\"platform_deduction_info\":{},\"win_record_info\":{\"win_record_id\":\"\",\"win_record_type\":\"\"}}",
                buyRenderRoot.getRender_track_id(),
                buyRenderRoot.getTotal_price_result().getMarketing_plan_id(),
                "18408282243",
                buyRenderRoot.getPay_method().getDecision_id(),
                buyRenderRoot.getPay_method().getPayapi_cache_id(),
                buyRenderRoot.getRender_token()
        );

        String X_SS_STUB1 = SecureUtil.md5("json_form=" + URLEncoder.encode(bodyData1)).toUpperCase();
        String url1 = "https://ec.snssdk.com/order/newcreate/vtl?can_queue=1&b_type_new=3&sub_b_type=13&ecom_appid=7386&webcast_appid=6822&live_request_from_jsb=1&live_sdk_version=896&webcast_sdk_version=2070&webcast_language=zh&webcast_locale=zh_CN&webcast_gps_access=2&webcast_app_id=6822&app_name=news_article&openlive_personal_recommend=1&device_platform=android&os=android&ssmix=a&_rticket=1664083081163&cdid=4543b4be-f010-4426-8ed5-767e7a2c9aac&channel=oppo_13_64&aid=13&version_code=896&version_name=8.9.6&manifest_version_code=8960&update_version_code=89607&ab_version=668775%2C4091914%2C4394174%2C4407627%2C4689321%2C4761884%2C4778839%2C4790596%2C660830%2C4761882%2C4838690%2C668774%2C4761873%2C662176%2C4761866%2C662099%2C4761838%2C668776%2C4761874%2C1859937%2C668779%2C4761879%2C4792846%2C3540006%2C3596064&ab_group=94565%2C102755&ab_feature=94563%2C102749&resolution=1080*2200&dpi=480&device_type=PACT00&device_brand=OPPO&language=zh&os_api=29&os_version=10&ac=wifi&dq_param=0&plugin=0&client_vid=4539074%2C3194525%2C3383553%2C2827920%2C4681421&isTTWebView=0&host_abi=arm64-v8a&tma_jssdk_version=2.53.0&rom_version=coloros_v7.1_pact00_11_f.27&immerse_pool_type=101&iid=119169286681983&device_id=321506381413262";
        String signData1 = String.format("{\"header\": {\"X-SS-STUB\": \"%s\",\"deviceid\": \"\",\"ktoken\": \"\",\"cookie\" : \"\"},\"url\": \"%s\"}",
                X_SS_STUB1, url1
        );
        String signHt1 = HttpRequest.post("http://110.42.246.12:8191/tt1213").body(signData1).execute().body();
        log.info("msg:{}", signHt1);
        String x_gorgon1 = JSON.parseObject(signHt1).getString("x-gorgon");
        String x_khronos1 = JSON.parseObject(signHt1).getString("x-khronos");
        String tarceid1 = JSON.parseObject(signHt1).getString("tarceid");
        OkHttpClient client = Demo.getOkHttpClient(ipAndPort.get("ip"), Integer.valueOf(ipAndPort.get("port")));
        RequestBody requestBody = new FormBody.Builder()
                .add("json_form", bodyData1)
                .build();
        Map<String, String> headers = PreUtils.buildIpMap("39.144.42.162");
        Request.Builder builder = new Request.Builder();
        for (String s : headers.keySet()) {
            builder.header(s, headers.get(s));
        }
        Request request = builder.url(url1)
                .post(requestBody)
                .addHeader("Cookie", ck)
                .addHeader("X-SS-STUB", X_SS_STUB1)
                .addHeader("x-tt-trace-id", tarceid1)
                .addHeader("User-Agent", "com.ss.android.article.news/8960 (Linux; U; Android 10; zh_CN; PACT00; Build/QP1A.190711.020; Cronet/TTNetVersion:68deaea9 2022-07-19 QuicVersion:12a1d5c5 2022-06-22)")
                .addHeader("X-Gorgon", x_gorgon1)
                .addHeader("X-Khronos", x_khronos1)
                .build();
        Response response = client.newCall(request).execute();
        String bodyRes = response.body().string();
        response.close();
        log.info("msg:{}", bodyRes);

    }
}
