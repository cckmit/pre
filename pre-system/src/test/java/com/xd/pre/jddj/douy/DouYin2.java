package com.xd.pre.jddj.douy;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xd.pre.modules.px.douyin.buyRender.res.BuyRenderRoot;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.net.URLEncoder;

@Slf4j
public class DouYin2 {
    public static void main(String[] args) throws Exception {
        String url = "https://ken.snssdk.com/order/buyRender?b_type_new=3&sub_b_type=13&ecom_appid=7386&webcast_appid=6822&live_request_from_jsb=1&live_sdk_version=896&webcast_sdk_version=2070&webcast_language=zh&webcast_locale=zh_CN&webcast_gps_access=2&webcast_app_id=6822&app_name=news_article&openlive_personal_recommend=1&device_platform=android&os=android&ssmix=a&_rticket=1664102966940&cdid=4543b4be-f010-4426-8ed5-767e7a2c9aac&channel=oppo_13_64&aid=13&version_code=896&version_name=8.9.6&manifest_version_code=8960&update_version_code=89607&ab_version=668775%2C4091914%2C4394174%2C4407627%2C4689321%2C4761884%2C4778839%2C4790596%2C660830%2C4761882%2C4838690%2C668774%2C4761873%2C662176%2C4761866%2C662099%2C4761838%2C668776%2C4761874%2C1859937%2C668779%2C4761879%2C4792846%2C3540006%2C3596064&ab_group=94565%2C102755&ab_feature=94563%2C102749&resolution=1080*2200&dpi=480&device_type=PACT00&device_brand=OPPO&language=zh&os_api=29&os_version=10&ac=wifi&dq_param=0&plugin=0&client_vid=4539074%2C3194525%2C3383553%2C2827920%2C4681421&isTTWebView=0&session_id=a5a12e8e-5f96-44a6-9487-d9e36a3df81c&host_abi=arm64-v8a&tma_jssdk_version=2.53.0&rom_version=coloros_v7.1_pact00_11_f.27&immerse_pool_type=101&iid=119169286681983&device_id=321506381413262";
        String productId = "3556357046087622442";
        String skuId = "1736502463777799";
        BuyRenderRoot buyRenderRoot = DouY.getBuyRenderRoot(url, productId, skuId);
        String bodyData1 = String.format("{" +
                        "\"area_type\": \"169\"," +
                        "\"receive_type\": 1," +
                        "\"travel_info\": {" +
                        "\"departure_time\": 0," +
                        "\"trave_type\": 1," +
                        "\"trave_no\": \"\"" +
                        "}," +
                        "\"pickup_station\": \"\"," +
                        "\"traveller_degrade\": \"\"," +
                        "\"b_type\": 3," +
                        "\"env_type\": \"2\"," +
                        "\"activity_id\": \"\"," +
                        "\"origin_type\": \"0\"," +
                        "\"origin_id\": \"0\"," +
                        "\"new_source_type\": \"0\"," +
                        "\"new_source_id\": \"0\"," +
                        "\"source_type\": \"0\"," +
                        "\"source_id\": \"0\"," +
                        "\"schema\": \"snssdk143://\"," +
                        "\"extra\": \"{\\\"page_type\\\":\\\"lynx\\\",\\\"render_track_id\\\":\\\"%s\\\",\\\"risk_info\\\":\\\"{\\\\\\\"biometric_params\\\\\\\":\\\\\\\"1\\\\\\\",\\\\\\\"is_jailbreak\\\\\\\":\\\\\\\"2\\\\\\\",\\\\\\\"openudid\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"order_page_style\\\\\\\":0,\\\\\\\"checkout_id\\\\\\\":1,\\\\\\\"ecom_payapi\\\\\\\":true,\\\\\\\"ip\\\\\\\":\\\\\\\"183.221.16.173\\\\\\\",\\\\\\\"sub_order_info\\\\\\\":[]}\\\"}\"," +
                        "\"marketing_plan_id\": \"%s\"," +
                        "\"s_type\": \"\"," +
                        "\"entrance_params\": \"{\\\"previous_page\\\":\\\"toutiao_mytab\\\",\\\"new_source_type\\\":\\\"order_list_page\\\",\\\"carrier_source\\\":\\\"order_list_page\\\",\\\"source_method\\\":\\\"order_buy_once\\\",\\\"is_groupbuying\\\":0,\\\"extra_campaign_type\\\":\\\"\\\"}\"," +
                        "\"sub_b_type\": \"3\"," +
                        "\"gray_feature\": \"PlatformFullDiscount\"," +
                        "\"sub_way\": 0," +
                        "\"pay_type\": 10," +
                        "\"new_year_festival_scene\": \"buy_again\"," +
                        "\"post_addr\": {" +
                        "\"province\": {}," +
                        "\"city\": {}," +
                        "\"town\": {}," +
                        "\"street\": {" +
                        "\"id\": \"\"," +
                        "\"name\": \"\"" +
                        "}" +
                        "}," +
                        "\"post_tel\": \"13568504862\"," +
                        "\"address_id\": \"0\"," +
                        "\"price_info\": {" +
                        "\"origin\": 1000," +
                        "\"freight\": 0," +
                        "\"coupon\": 0," +
                        "\"pay\": 1000" +
                        "}," +
                        "\"pay_info\": \"{\\\"sdk_version\\\":\\\"v2\\\",\\\"dev_info\\\":{\\\"reqIp\\\":\\\"183.221.16.173\\\",\\\"os\\\":\\\"android\\\",\\\"isH5\\\":false,\\\"cjSdkVersion\\\":\\\"6.3.5\\\",\\\"aid\\\":\\\"13\\\",\\\"ua\\\":\\\"com.ss.android.article.news/8960 (Linux; U; Android 10; zh_CN; PACT00; Build/QP1A.190711.020; Cronet/TTNetVersion:68deaea9 2022-07-19 QuicVersion:12a1d5c5 2022-06-27)\\\",\\\"riskUa\\\":\\\"\\\",\\\"lang\\\":\\\"zh-Hans\\\",\\\"deviceId\\\":\\\"321506381413262\\\",\\\"osVersion\\\":\\\"10\\\",\\\"vendor\\\":\\\"\\\",\\\"model\\\":\\\"\\\",\\\"netType\\\":\\\"\\\",\\\"appVersion\\\":\\\"8.9.6\\\",\\\"appName\\\":\\\"news_article\\\",\\\"devicePlatform\\\":\\\"android\\\",\\\"deviceType\\\":\\\"PACT00\\\",\\\"channel\\\":\\\"oppo_13_64\\\",\\\"openudid\\\":\\\"\\\",\\\"versionCode\\\":\\\"896\\\",\\\"ac\\\":\\\"wifi\\\",\\\"brand\\\":\\\"OPPO\\\",\\\"iid\\\":\\\"119169286681983\\\",\\\"bioType\\\":\\\"1\\\"},\\\"credit_pay_info\\\":{\\\"installment\\\":\\\"1\\\"},\\\"bank_card_info\\\":{},\\\"voucher_no_list\\\":[],\\\"zg_ext_param\\\":\\\"{\\\\\\\"decision_id\\\\\\\":\\\\\\\"%s\\\\\\\",\\\\\\\"qt_c_pay_url\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"retain_c_pay_url\\\\\\\":\\\\\\\"\\\\\\\"}\\\",\\\"jh_ext_info\\\":\\\"{\\\\\\\"payapi_cache_id\\\\\\\":\\\\\\\"%s\\\\\\\"}\\\",\\\"sub_ext\\\":\\\"\\\",\\\"biometric_params\\\":\\\"1\\\",\\\"is_jailbreak\\\":\\\"2\\\",\\\"order_page_style\\\":0,\\\"checkout_id\\\":1,\\\"pay_amount_composition\\\":[]}\"," +
                        "\"render_token\": \"%s\"," +
                        "\"win_record_id\": \"\"," +
                        "\"marketing_channel\": \"\"," +
                        "\"identity_card_id\": \"\"," +
                        "\"pay_amount_composition\": []," +
                        "\"user_account\": {}," +
                        "\"queue_count\": 0," +
                        "\"store_id\": \"\"," +
                        "\"shop_id\": \"GceCTPIk\"," +
                        "\"combo_id\": \"1736502463777799\"," +
                        "\"combo_num\": 1," +
                        "\"product_id\": \"3556357046087622442\"," +
                        "\"buyer_words\": \"\"," +
                        "\"stock_info\": [" +
                        "{" +
                        "\"stock_type\": 1," +
                        "\"stock_num\": 1," +
                        "\"sku_id\": \"1736502463777799\"," +
                        "\"warehouse_id\": \"0\"" +
                        "}" +
                        "]," +
                        "\"warehouse_id\": 0," +
                        "\"coupon_info\": {}," +
                        "\"freight_insurance\": false," +
                        "\"cert_insurance\": false," +
                        "\"allergy_insurance\": false," +
                        "\"room_id\": \"\"," +
                        "\"author_id\": \"\"," +
                        "\"content_id\": \"\"," +
                        "\"promotion_id\": \"\"," +
                        "\"ecom_scene_id\": \"\"," +
                        "\"shop_user_id\": \"\"," +
                        "\"group_id\": \"\"," +
                        "\"privilege_tag_keys\": []," +
                        "\"select_privilege_properties\": []," +
                        "\"platform_deduction_info\": {}," +
                        "\"win_record_info\": {" +
                        "\"win_record_id\": \"\"," +
                        "\"win_record_type\": \"\"" +
                        "}" +
                        "}",
                buyRenderRoot.getRender_track_id(),
                buyRenderRoot.getTotal_price_result().getMarketing_plan_id(),
                buyRenderRoot.getPay_method().getDecision_id(),
                buyRenderRoot.getPay_method().getPayapi_cache_id(),
                buyRenderRoot.getRender_token()
//                "1_7c6c5c6ab892fbff_56FNBzQcc3JbPb+KgeQ3nyQzCbcTQp6LrtSvErKbJ6hSBqOFDoRkasDKioWKn42iWwgbG7bjOEodHJmon0TVGnzznlRy/P6K8HAWR7eClJ+y7vhT0hqGcVEHWpOhpBrFP4QVgQO27MBklVf0wpBFsg=="
        );

        JSONObject parseObject = JSON.parseObject(bodyData1);

        String X_SS_STUB1 = SecureUtil.md5("json_form=" + URLEncoder.encode(JSON.toJSONString(parseObject))).toUpperCase();
        String url1 = "https://ec.snssdk.com/order/newcreate/vtl?can_queue=1&b_type_new=3&sub_b_type=13&ecom_appid=7386&webcast_appid=6822&live_request_from_jsb=1&live_sdk_version=896&webcast_sdk_version=2070&webcast_language=zh&webcast_locale=zh_CN&webcast_gps_access=2&webcast_app_id=6822&app_name=news_article&openlive_personal_recommend=1&device_platform=android&os=android&ssmix=a&_rticket=1664083081163&cdid=4543b4be-f010-4426-8ed5-767e7a2c9aac&channel=oppo_13_64&aid=13&version_code=896&version_name=8.9.6&manifest_version_code=8960&update_version_code=89607&ab_version=668775%2C4091914%2C4394174%2C4407627%2C4689321%2C4761884%2C4778839%2C4790596%2C660830%2C4761882%2C4838690%2C668774%2C4761873%2C662176%2C4761866%2C662099%2C4761838%2C668776%2C4761874%2C1859937%2C668779%2C4761879%2C4792846%2C3540006%2C3596064&ab_group=94565%2C102755&ab_feature=94563%2C102749&resolution=1080*2200&dpi=480&device_type=PACT00&device_brand=OPPO&language=zh&os_api=29&os_version=10&ac=wifi&dq_param=0&plugin=0&client_vid=4539074%2C3194525%2C3383553%2C2827920%2C4681421&isTTWebView=0&session_id=45978c35-2594-436d-9a18-8f6a97b52cc7&host_abi=arm64-v8a&tma_jssdk_version=2.53.0&rom_version=coloros_v7.1_pact00_11_f.27&immerse_pool_type=101&iid=119169286681983&device_id=321506381413262";
        String signData1 = String.format("{\"header\": {\"X-SS-STUB\": \"%s\",\"deviceid\": \"\",\"ktoken\": \"\",\"cookie\" : \"\"},\"url\": \"%s\"}",
                X_SS_STUB1, url1
        );
        String signHt1 = HttpRequest.post("http://110.42.246.12:8191/tt1213").body(signData1).execute().body();
        log.info("msg:{}", signHt1);
        String x_gorgon1 = JSON.parseObject(signHt1).getString("x-gorgon");
        String x_khronos1 = JSON.parseObject(signHt1).getString("x-khronos");
        String tarceid = JSON.parseObject(signHt1).getString("tarceid");
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("json_form", bodyData1)
                .build();
        Request request = new Request.Builder()
                .url("https://ec.snssdk.com/order/newcreate/vtl?can_queue=1&b_type_new=3&sub_b_type=13&ecom_appid=7386&webcast_appid=6822&live_request_from_jsb=1&live_sdk_version=896&webcast_sdk_version=2070&webcast_language=zh&webcast_locale=zh_CN&webcast_gps_access=2&webcast_app_id=6822&app_name=news_article&openlive_personal_recommend=1&device_platform=android&os=android&ssmix=a&_rticket=1664168634334&cdid=4543b4be-f010-4426-8ed5-767e7a2c9aac&channel=oppo_13_64&aid=13&version_code=896&version_name=8.9.6&manifest_version_code=8960&update_version_code=89607&ab_version=668774%2C4761873%2C668776%2C4761874%2C660830%2C4761882%2C4838690%2C668779%2C4761879%2C1859937%2C662176%2C4761866%2C662099%2C4761838%2C668775%2C4091914%2C4394174%2C4407627%2C4689321%2C4761884%2C4778839%2C4790596%2C4792846%2C3540006%2C3596064&ab_group=94565%2C102755&ab_feature=94563%2C102749&resolution=1080%2A2200&dpi=480&device_type=PACT00&device_brand=OPPO&language=zh&os_api=29&os_version=10&ac=wifi&dq_param=0&plugin=0&client_vid=4539074%2C3194525%2C3383553%2C2827920%2C4681421&isTTWebView=0&session_id=a5a12e8e-5f96-44a6-9487-d9e36a3df81c&host_abi=arm64-v8a&tma_jssdk_version=2.53.0&rom_version=coloros_v7.1_pact00_11_f.27&immerse_pool_type=101&iid=119169286681983&device_id=321506381413262")
                .post(requestBody)
                .addHeader("Cookie", "install_id=119169286681983; ttreq=1$95c9258aaed0231ca4e6c6a888d5227c28dc0621; passport_csrf_token=804daba8a1c414854c8778d212a35af9; passport_csrf_token_default=804daba8a1c414854c8778d212a35af9; n_mh=8nysT__BxDL_VpPZTRMYKZZSN1pywPhZ9o63MSmzGLg; d_ticket=a0cc5be56fdc802b0e3ab4e4f3bb88ab20bcc; sid_guard=8bd98afb08a175ebb2d91b6ff52c79a2%7C1664018439%7C5184000%7CWed%2C+23-Nov-2022+11%3A20%3A39+GMT; uid_tt=0610f992949cf3c2d31c098493cc97e0; uid_tt_ss=0610f992949cf3c2d31c098493cc97e0; sid_tt=8bd98afb08a175ebb2d91b6ff52c79a2; sessionid=8bd98afb08a175ebb2d91b6ff52c79a2; sessionid_ss=8bd98afb08a175ebb2d91b6ff52c79a2; msToken=_2IUDjQ1-_F7X6bMOFCWKiPLxWIcOTaONfjNt2FPIkNP4bqGgYiGZvJT-BkxTN7gV0ZJzDdw9js-RIb3UTAYPDOtYX8W6OOvr3UIqoio4LA=; odin_tt=16b466912a475da7dd82593c8018cc4566382f6b9d9fd8fce1dd847df5119b375f33d18ce06f53fbfe098ec4b580aece30abd7a76d227600ee7db4034ba9fbd3e7f6fab89ca099c14909a7319cc2dc97")
                .addHeader("Authorization", "Bearer act.3.VFoUyHrlFXRqSUpzzG5VY9o647bWkI9PFun65ukzx_T4Icm3N8P6y1k3z-JtcXy4EJbXks19f4UPTBIrZXgIWhjx_-JJppsbfhCeLRiuoi9pkgoHU4sRfyfft5ObVd0Ap1cue9Yfw9kW3zY9YthqYfSC6cSRtFlcw_LDFA==")
                .addHeader("OpenId", "_0004enPLcYO_YmdkkNYGbKL6kmsNiu3bzrx")
                .addHeader("ClientKey", "awikua6yvbqai0ht")
                .addHeader("odin-tt", "97f604c3abcac860d3e2c5b99ef23c7e7706fbe6cc99c80d4e9528f5d953e6c69f042be34a363e85d4df9fa5feb7d9b35aa2e5243f9984504fb7ceaab04b2d33")
                .addHeader("X-SS-REQ-TICKET", "1664168634337")
                .addHeader("x-tt-dt", "AAATCJR575XLWZ2G24PU7BWE6JLSNQ6BNN6RQHEHJ7MUQIRGNSXKWDDC6FH277FDHABXWGV3SVJRMI6DXZZQKBLOFSJODXQUCCN5P37E7MADAZ2SJKUFNRDQOR6IKW64NP6OTJ2S6ZBA57DJMOOXBSI")
                .addHeader("sdk-version", "2")
                .addHeader("X-Tt-Token", "008bd98afb08a175ebb2d91b6ff52c79a2008145bb8de50f956703ac153e887cee568ce94846414449163289b9c6270cf0e7b73f69e290a7f34a6c0dffacd8c5865715fdc0253fa4c73e920ba5404de399b2f269984cd6d83e8c865b6ad1f3359e080-1.0.1")
                .addHeader("passport-sdk-version", "30858")
                .addHeader("x-vc-bdturing-sdk-version", "2.2.1.cn")
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .addHeader("X-SS-STUB", X_SS_STUB1)
                .addHeader("x-tt-store-region", "cn-sc")
                .addHeader("x-tt-store-region-src", "did")
                .addHeader("x-tt-request-tag", "s=-1;p=0")
                .addHeader("x-tt-trace-id", tarceid)
                .addHeader("User-Agent", "com.ss.android.article.news/8960 (Linux; U; Android 10; zh_CN; PACT00; Build/QP1A.190711.020; Cronet/TTNetVersion:68deaea9 2022-07-19 QuicVersion:12a1d5c5 2022-06-27)")
                .addHeader("X-Gorgon", x_gorgon1)
                .addHeader("X-Khronos", x_khronos1)
                .build();
        Response response = client.newCall(request).execute();
        String bodyRes = response.body().string();
        response.close();
        log.info("msg:{}", bodyRes);
    }
}
