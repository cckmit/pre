package com.xd.pre.douyinnew;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.nosql.redis.RedisDS;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.jddj.douy.Douyin3;
import com.xd.pre.modules.px.douyin.buyRender.BuyRenderParamDto;
import com.xd.pre.modules.px.douyin.buyRender.res.BuyRenderRoot;
import com.xd.pre.modules.px.douyin.submit.SubmitUtils;
import com.xd.pre.modules.sys.domain.DouyinDeviceIid;
import com.xd.pre.pcScan.Demo;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class TestResoData {
    public static Db db = Db.use();
    public static Jedis jedis = RedisDS.create().getJedis();

    public static void main(String[] args) throws Exception {

  /*    String device_id = "3496879052572183";
        String iid = "1983951053784861";
        String ck = "sid_tt=140a336dd81551eaa30bc0e9e8d336fd;";//我的账号
        */
        log.info("查找账号位置msg:{}");
        String device_id = "";
        String iid = "";
        String ck = ";";
        String notUse = "56eb748c437c01e1932423dbe0a32015;936e154a11e17dd7a78293bb6d4602e6;8bddce4a0b88b7b33ad34419b8f7febb;12016212c714adb3acfc1a1c586f7c62;" +
                "ee8c10ff32bdbb4263aa051b43f987d1;33f2eb6aef641d58b7859f6ef4403e05;a0ee1313a37eea915763ec5da6012726;" +
                "6bf923d1af1c9fe3be9e03dea311382e;";
        List<Entity> appCks = db.use().query("select * from douyin_app_ck where is_enable =0 and id >1244 ");
        List<Entity> devicesBds = db.use().query("select * from douyin_device_iid where  id > 2718");
        for (Entity entity : appCks) {
            String uid = entity.getStr("uid");
            String ck_device_lock = jedis.get("抖音和设备号关联:" + uid);
            log.info("当前执行个数:{}", entity.getInt("id"));
            ck = entity.getStr("ck");
            if (StrUtil.isNotBlank(ck_device_lock) || notUse.contains(ck.split("sid_tt=")[1])) {
                continue;
            }
            Set<String> keys = jedis.keys("redis临时锁定:*");
            if (JSON.toJSONString(keys).contains("redis临时锁定:" + uid)) {
                continue;
            }
            jedis.expire("redis临时锁定:" + uid, 60 * 60 * 24);
            for (Entity devicesBd : devicesBds) {
                String deviceDBId = jedis.get("抖音锁定设备:" + devicesBd.getInt("id"));
                if (StrUtil.isNotBlank(deviceDBId)) {
                    continue;
                }

                if (JSON.toJSONString(keys).contains("redis临时锁定:" + devicesBd.getInt("id"))) {
                    continue;
                }
                if (JSON.toJSONString(keys).contains("redis临时锁定:" + uid)) {
                    break;
                }
                String redistLock = jedis.get("redis临时锁定:" + devicesBd.getInt("id"));
                if (StrUtil.isNotBlank(redistLock)) {
                    continue;
                }
                redistLock = jedis.get("redis临时锁定:" + uid);
                if (StrUtil.isNotBlank(redistLock)) {
                    break;
                }

                jedis.set("redis临时锁定:" + uid, uid);
                jedis.set("redis临时锁定:" + devicesBd.getInt("id"), devicesBd.getInt("id") + "");
                jedis.expire("redis临时锁定:" + devicesBd.getInt("id"), 60 * 60 * 24);
                log.info("当前执行的device_id:{}", devicesBd.getInt("id"));
                device_id = devicesBd.getStr("device_id");
                iid = devicesBd.getStr("iid");
                try {
                    System.err.println(device_id + "------" + iid + "--------------" + ck);
                    ck_device_lock = jedis.get("抖音和设备号关联:" + uid);
                    if (StrUtil.isNotBlank(ck_device_lock) || notUse.contains(ck.split("sid_tt=")[1])) {
                        break;
                    }
                    boolean b = mian1(device_id, iid, ck, devicesBd.getInt("id"), entity.getStr("uid"));
                    if (b) {
                        db.use().execute("update douyin_app_ck set is_enable = ? where id = ?", 1, entity.getInt("id"));
                        db.use().execute("update douyin_device_iid set is_enable = ? where id = ?", 1, devicesBd.getInt("id"));
                        log.info(">>>>>>>>>>>>>>>>>>>>>执行成功当前顺序:{},{}", entity.getStr("id"), devicesBd.getInt("id"));
                    }
                } catch (Exception e) {
                    try {
                        boolean b = mian1(device_id, iid, ck, devicesBd.getInt("id"), entity.getStr("uid"));
                        if (b) {
                            log.info(">>>>>>>>>>>>>>>>>>>>>执行成功当前顺序:{},{}", entity.getStr("id"), devicesBd.getInt("id"));
                            db.use().execute("update douyin_app_ck set is_enable = ? where id = ?", 1, entity.getInt("id"));
                        }
                    } catch (Exception e1) {
                    }
                    log.error("========================:{}", e);
                }
            }
        }

    }

    private static boolean mian1(String device_id, String iid, String ck, Integer deiviesId, String uid) throws IOException, SQLException {
        Integer payType = 2;
        String payIp = "183.11.13.172";


        if (device_id.contains("device_id_str=")) {
            device_id = device_id.replace("device_id_str=", "");
        }
        if (iid.contains("install_id_str=")) {
            iid = iid.replace("install_id_str=", "");
        }
/*        BuyRenderParamDto buyRenderParamDto = BuyRenderParamDto.builder().product_id("3556357046087622442").sku_id("1736502463777799").author_id("4051040200033531")
                .ecom_scene_id("1041").shop_id("GceCTPIk").origin_id("4051040200033531_3556357046087622442").origin_type("3002070010")
                .new_source_type("product_detail").build();*/
/*        BuyRenderParamDto buyRenderParamDto = BuyRenderParamDto.builder().product_id("3561751789252519688").sku_id("1739136614382624").author_id("4051040200033531")
                .ecom_scene_id("1003").origin_id("4051040200033531_3561751789252519688").origin_type("3002002002").new_source_type("product_detail").build();*/
        BuyRenderParamDto buyRenderParamDto = BuyRenderParamDto.builder().product_id("3561752220930340544").sku_id("1739136822194211").author_id("4051040200033531")
                .ecom_scene_id("").origin_id("4051040200033531_3561752220930340544").origin_type("3002002002").shop_id("GceCTPIk").new_source_type("product_detail").build();
        System.err.println(JSON.toJSONString(buyRenderParamDto));
/*     BuyRenderParamDto buyRenderParamDto = BuyRenderParamDto.builder().product_id("3574327743640429367").sku_id("1745277214000191").author_id("4051040200033531")
                .ecom_scene_id("").origin_id("4051040200033531_3574327743640429367").origin_type("3002002002").new_source_type("product_detail").build();
        System.err.println(JSON.toJSONString(buyRenderParamDto));*/
//        BuyRenderParamDto buyRenderParamDto = BuyRenderParamDto.builder().product_id("3574327743640429367").sku_id("1745277214000191").author_id("4051040200033531")
//                .ecom_scene_id("").origin_id("4051040200033531_3574327743640429367").origin_type("3002002002").new_source_type("product_detail").build();
//        System.err.println(JSON.toJSONString(buyRenderParamDto));
        String body = SubmitUtils.buildBuyRenderParamData(buyRenderParamDto);
        Map<String, String> ipAndPort = Douyin3.getIpAndPort();
        OkHttpClient client = Demo.getOkHttpClient(ipAndPort.get("ip"), Integer.valueOf(ipAndPort.get("port")));


//        String body = "{\"address\":null,\"platform_coupon_id\":null,\"kol_coupon_id\":null,\"auto_select_best_coupons\":true,\"customize_pay_type\":\"{\\\"checkout_id\\\":1,\\\"bio_type\\\":\\\"1\\\"}\",\"first_enter\":true,\"source_type\":\"1\",\"shape\":0,\"marketing_channel\":\"\",\"forbid_redpack\":false,\"support_redpack\":true,\"use_marketing_combo\":false,\"entrance_params\":\"{\\\"order_status\\\":3,\\\"previous_page\\\":\\\"order_list_page\\\",\\\"carrier_source\\\":\\\"order_detail\\\",\\\"ecom_scene_id\\\":\\\"1041\\\",\\\"room_id\\\":\\\"\\\",\\\"promotion_id\\\":\\\"\\\",\\\"author_id\\\":\\\"\\\",\\\"group_id\\\":\\\"\\\",\\\"anchor_id\\\":\\\"4051040200033531\\\",\\\"source_method\\\":\\\"open_url\\\",\\\"ecom_group_type\\\":\\\"video\\\",\\\"discount_type\\\":\\\"\\\",\\\"full_return\\\":\\\"0\\\",\\\"is_exist_size_tab\\\":\\\"0\\\",\\\"rank_id_source\\\":\\\"\\\",\\\"show_rank\\\":\\\"not_in_rank\\\",\\\"warm_up_status\\\":\\\"0\\\",\\\"coupon_id\\\":\\\"\\\",\\\"brand_verified\\\":\\\"0\\\",\\\"label_name\\\":\\\"\\\",\\\"with_sku\\\":\\\"0\\\",\\\"is_replay\\\":\\\"0\\\",\\\"is_package_sale\\\":\\\"0\\\",\\\"is_groupbuying\\\":\\\"0\\\"}\",\"shop_requests\":[{\"shop_id\":\"GceCTPIk\",\"product_requests\":[{\"product_id\":\"3556357046087622442\",\"sku_id\":\"1736502463777799\",\"sku_num\":1,\"author_id\":\"4051040200033531\",\"ecom_scene_id\":\"1041\",\"origin_id\":\"4051040200033531_3556357046087622442\",\"origin_type\":\"3002070010\",\"new_source_type\":\"product_detail\",\"select_privilege_properties\":[]}]}]}";
        String url = "https://ken.snssdk.com/order/buyRender?b_type_new=2&request_tag_from=lynx&os_api=25&device_type=SM-G973N&ssmix=a&manifest_version_code=169&dpi=240&is_guest_mode=0&uuid=354730528934825&app_name=aweme&version_name=17.3.0&ts=1664384063&cpu_support64=false&app_type=normal&appTheme=dark&ac=wifi&host_abi=arm64-v8a&update_version_code=17309900&channel=dy_tiny_juyouliang_dy_and24&_rticket=1664384064117&device_platform=android&iid=" + iid + "&version_code=170300&cdid=78d30492-1201-49ea-b86a-1246a704711d&os=android&is_android_pad=0&openudid=199d79fbbeff0e58&device_id=" + device_id + "&resolution=720%2A1280&os_version=5.1.1&language=zh&device_brand=Xiaomi&aid=1128&minor_status=0&mcc_mnc=46011";
        String X_SS_STUB = SecureUtil.md5("json_form=" + URLEncoder.encode(body)).toUpperCase();
        String signData = String.format("{\"header\": {\"X-SS-STUB\": \"%s\",\"deviceid\": \"\",\"ktoken\": \"\",\"cookie\" : \"\"},\"url\": \"%s\"}",
                X_SS_STUB, url
        );
        String signHt = HttpRequest.post("http://110.42.246.12:8191/tt1213").body(signData).execute().body();
        String x_gorgon = JSON.parseObject(signHt).getString("x-gorgon");
        String x_khronos = JSON.parseObject(signHt).getString("x-khronos");
        RequestBody requestBody = new FormBody.Builder()
                .add("json_form", body)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("X-SS-STUB", X_SS_STUB)
                .addHeader("Cookie", ck)
                .addHeader("X-Gorgon", x_gorgon)
                .addHeader("X-Khronos", x_khronos)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = client.newCall(request).execute();
        String resBody = response.body().string();
        log.info("预下单数据msg:{}", resBody);
        response.close();
        if (resBody.contains("失败")) {
            db.use().execute("update douyin_app_ck set is_enable = ? where uid = ?", -1, uid);
        }
        BuyRenderRoot buyRenderRoot = JSON.parseObject(JSON.parseObject(resBody).getString("data"), BuyRenderRoot.class);
        String url1 = "https://ec.snssdk.com/order/newcreate/vtl?can_queue=1&b_type_new=2&request_tag_from=lynx&os_api=5&device_type=ELE-AL00&ssmix=a&manifest_version_code=170301&dpi=240&is_guest_mode=0&uuid=354730528931234&app_name=aweme&version_name=17.3.0&ts=1664384138&cpu_support64=false&app_type=normal&appTheme=dark&ac=wifi&host_abi=armeabi-v7a&update_version_code=17309900&channel=dy_tiny_juyouliang_dy_and24&device_platform=android&iid=" + iid + "&version_code=170300&cdid=78d30492-1201-49ea-b86a-1246a704711d&os=android&is_android_pad=0&openudid=27b54460b6dbb870&device_id=" + device_id + "&resolution=720*1280&os_version=7.1.1&language=zh&device_brand=samsung&aid=1128&minor_status=0&mcc_mnc=46007";
        String bodyData1 = String.format("{\"area_type\":\"169\",\"receive_type\":1,\"travel_info\":{\"departure_time\":0,\"trave_type\":1,\"trave_no\":\"\"}," +
                        "\"pickup_station\":\"\",\"traveller_degrade\":\"\",\"b_type\":3,\"env_type\":\"2\",\"activity_id\":\"\"," +
                        "\"origin_type\":\"%s\"," +
                        "\"origin_id\":\"%s\"," +
                        "\"new_source_type\":\"product_detail\",\"new_source_id\":\"0\",\"source_type\":\"0\"," +
                        "\"source_id\":\"0\",\"schema\":\"snssdk143://\",\"extra\":\"{\\\"page_type\\\":\\\"lynx\\\"," +
                        "\\\"alkey\\\":\\\"1128_99514375927_0_3556357046087622442_010\\\"," +
                        "\\\"c_biz_combo\\\":\\\"8\\\"," +
                        "\\\"render_track_id\\\":\\\"%s\\\"," +
                        "\\\"risk_info\\\":\\\"{\\\\\\\"biometric_params\\\\\\\":\\\\\\\"1\\\\\\\"" +
                        ",\\\\\\\"is_jailbreak\\\\\\\":\\\\\\\"2\\\\\\\",\\\\\\\"openudid\\\\\\\":\\\\\\\"\\\\\\\"," +
                        "\\\\\\\"order_page_style\\\\\\\":0,\\\\\\\"checkout_id\\\\\\\":1,\\\\\\\"ecom_payapi\\\\\\\":true," +
                        "\\\\\\\"ip\\\\\\\":\\\\\\\"%s\\\\\\\"," +
                        "\\\\\\\"sub_order_info\\\\\\\":[]}\\\"}\"," +
                        "\"marketing_plan_id\":\"%s\"," +
                        "\"s_type\":\"\"" +
                        ",\"entrance_params\":\"{\\\"order_status\\\":4,\\\"previous_page\\\":\\\"toutiao_mytab__order_list_page\\\"," +
                        "\\\"carrier_source\\\":\\\"order_detail\\\"," +
                        "\\\"ecom_scene_id\\\":\\\"%s\\\",\\\"room_id\\\":\\\"\\\"," +
                        "\\\"promotion_id\\\":\\\"\\\",\\\"author_id\\\":\\\"\\\",\\\"group_id\\\":\\\"\\\",\\\"anchor_id\\\":\\\"\\\"," +
                        "\\\"source_method\\\":\\\"open_url\\\",\\\"ecom_group_type\\\":\\\"\\\",\\\"module_label\\\":\\\"\\\"," +
                        "\\\"ecom_icon\\\":\\\"\\\",\\\"brand_verified\\\":\\\"0\\\",\\\"discount_type\\\":\\\"\\\",\\\"full_return\\\":\\\"0\\\"," +
                        "\\\"is_activity_banner\\\":0," +
                        "\\\"is_exist_size_tab\\\":\\\"0\\\",\\\"is_groupbuying\\\":\\\"0\\\",\\\"is_package_sale\\\":\\\"0\\\"," +
                        "\\\"is_replay\\\":\\\"0\\\",\\\"is_short_screen\\\":\\\"0\\\",\\\"is_with_video\\\":1,\\\"label_name\\\":\\\"\\\"," +
                        "\\\"market_channel_hot_fix\\\":\\\"\\\",\\\"rank_id_source\\\":\\\"\\\",\\\"show_dou_campaign\\\":0," +
                        "\\\"show_rank\\\":\\\"not_in_rank\\\",\\\"upfront_presell\\\":0,\\\"warm_up_status\\\":\\\"0\\\",\\\"auto_coupon\\\":0," +
                        "\\\"coupon_id\\\":\\\"\\\",\\\"with_sku\\\":\\\"0\\\",\\\"item_id\\\":\\\"0\\\"," +
                        "\\\"commodity_id\\\":\\\"%s\\\",\\\"commodity_type\\\":6," +
                        "\\\"product_id\\\":\\\"%s\\\",\\\"extra_campaign_type\\\":\\\"\\\"}\"," +
                        "\"sub_b_type\":\"3\",\"gray_feature\":\"PlatformFullDiscount\",\"sub_way\":0," +
                        "\"pay_type\":%d," +
                        "\"post_addr\":{\"province\":{},\"city\":{},\"town\":{},\"street\":{\"id\":\"\",\"name\":\"\"}}," +
                        "\"post_tel\":\"%s\",\"address_id\":\"0\",\"price_info\":{\"origin\":1000,\"freight\":0,\"coupon\":0," +
                        "\"pay\":1000}," +
                        "\"pay_info\":\"{\\\"sdk_version\\\":\\\"v2\\\",\\\"dev_info\\\":{\\\"reqIp\\\":\\\"39.144.42.162\\\",\\\"os\\\":\\\"android\\\"," +
                        "\\\"isH5\\\":false,\\\"cjSdkVersion\\\":\\\"6.3.5\\\",\\\"aid\\\":\\\"13\\\"," +
                        "\\\"ua\\\":\\\"com.ss.android.article.news/8960+(Linux;+U;+Android+10;+zh_CN;" +
                        "+PACT00;+Build/QP1A.190711.020;+Cronet/TTNetVersion:68deaea9+2022-07-19+QuicVersion:12a1d5c5+2022-06-27)\\\"," +
                        "\\\"riskUa\\\":\\\"\\\",\\\"lang\\\":\\\"zh-Hans\\\"," +
                        "\\\"deviceId\\\":\\\"%s\\\",\\\"osVersion\\\":\\\"10\\\"," +
                        "\\\"vendor\\\":\\\"\\\",\\\"model\\\":\\\"\\\",\\\"netType\\\":\\\"\\\"," +
                        "\\\"appVersion\\\":\\\"8.9.6\\\",\\\"appName\\\":\\\"aweme\\\"," +
                        "\\\"devicePlatform\\\":\\\"android\\\",\\\"deviceType\\\":\\\"PACT00\\\"," +
                        "\\\"channel\\\":\\\"oppo_13_64\\\",\\\"openudid\\\":\\\"\\\"," +
                        "\\\"versionCode\\\":\\\"896\\\",\\\"ac\\\":\\\"wifi\\\",\\\"brand\\\":\\\"OPPO\\\",\\\"iid\\\":\\\"%s\\\",\\\"bioType\\\":\\\"1\\\"}," +
                        "\\\"credit_pay_info\\\":{\\\"installment\\\":\\\"1\\\"},\\\"bank_card_info\\\":{},\\\"voucher_no_list\\\":[]," +
                        "\\\"zg_ext_param\\\":" +
                        "\\\"{\\\\\\\"decision_id\\\\\\\":\\\\\\\"%s\\\\\\\",\\\\\\\"qt_c_pay_url\\\\\\\":\\\\\\\"\\\\\\\"," +
                        "\\\\\\\"retain_c_pay_url\\\\\\\":\\\\\\\"\\\\\\\"}\\\"," +
                        "\\\"jh_ext_info\\\":\\\"{\\\\\\\"payapi_cache_id\\\\\\\":\\\\\\\"%s\\\\\\\"}\\\"," +
                        "\\\"sub_ext\\\":\\\"\\\",\\\"biometric_params\\\":\\\"1\\\",\\\"is_jailbreak\\\":\\\"2\\\"," +
                        "\\\"order_page_style\\\":0,\\\"checkout_id\\\":1,\\\"pay_amount_composition\\\":[]}\"," +
                        "\"render_token\":\"%s\"," +
                        "\"win_record_id\":\"\",\"marketing_channel\":\"\",\"identity_card_id\":\"\"," +
                        "\"pay_amount_composition\":[],\"user_account\":{},\"queue_count\":0,\"store_id\":\"\"," +
                        "\"shop_id\":\"GceCTPIk\"," +
                        "\"combo_id\":\"%s\"," +
                        "\"combo_num\":1," +
                        "\"product_id\":\"%s\",\"buyer_words\":\"\",\"stock_info\":[{\"stock_type\":1,\"stock_num\":1," +
                        "\"sku_id\":\"%s\"" +
                        ",\"warehouse_id\":\"0\"}],\"warehouse_id\":0,\"coupon_info\":{},\"freight_insurance\":false,\"cert_insurance\":false," +
                        "\"allergy_insurance\":false,\"room_id\":\"\",\"author_id\":\"\",\"content_id\":\"0\",\"promotion_id\":\"\"," +
                        "\"ecom_scene_id\":\"%s\"," +
                        "\"shop_user_id\":\"\",\"group_id\":\"\"," +
                        "\"privilege_tag_keys\":[],\"select_privilege_properties\":[]," +
                        "\"platform_deduction_info\":{},\"win_record_info\":{\"win_record_id\":\"\",\"win_record_type\":\"\"}}",
                buyRenderParamDto.getOrigin_type(),
                buyRenderParamDto.getOrigin_id(),
                buyRenderRoot.getRender_track_id(),
                payIp,
                buyRenderRoot.getTotal_price_result().getMarketing_plan_id(),
                buyRenderParamDto.getEcom_scene_id(),
                buyRenderParamDto.getProduct_id(),
                buyRenderParamDto.getProduct_id(),
                payType,
                PreUtils.getTel(),
                device_id,
                iid,
                buyRenderRoot.getPay_method().getDecision_id(),
                buyRenderRoot.getPay_method().getPayapi_cache_id(),
                buyRenderRoot.getRender_token(),
                buyRenderParamDto.getSku_id(),
                buyRenderParamDto.getProduct_id(),
                buyRenderParamDto.getSku_id(),
                buyRenderParamDto.getEcom_scene_id()
        );
        System.out.println(bodyData1);
        String X_SS_STUB1 = SecureUtil.md5("json_form=" + URLEncoder.encode(bodyData1)).toUpperCase();
        String signData1 = String.format("{\"header\": {\"X-SS-STUB\": \"%s\",\"deviceid\": \"\",\"ktoken\": \"\",\"cookie\" : \"\"},\"url\": \"%s\"}",
                X_SS_STUB1, url1
        );
        String signHt1 = HttpRequest.post("http://110.42.246.12:8191/tt1213").body(signData1).execute().body();
        log.info("msg:{}", signHt1);
        String x_gorgon1 = JSON.parseObject(signHt1).getString("x-gorgon");
        String x_khronos1 = JSON.parseObject(signHt1).getString("x-khronos");
        String tarceid1 = JSON.parseObject(signHt1).getString("tarceid");
        RequestBody requestBody1 = new FormBody.Builder()
                .add("json_form", bodyData1)
                .build();
        Map<String, String> headers = PreUtils.buildIpMap(payIp);
        Request.Builder builder = new Request.Builder();
        for (String s : headers.keySet()) {
            builder.header(s, headers.get(s));
        }
        /**
         * x-tt-dt: AAA47HUS7TM7WXDTGING7ABL3JWMMLVWJGFXG437STXOQQ4UM4RTGZNRO7TKGDPQAANCMJCT2QUF7HMBCPQVVTFZG6AHNSP6W5ESC34Y5HE264TSZHQKCR6B2DVMQQSDIPFE4KCXLGCZVMGSN7GVJII
         * activity_now_client: 1665499105735
         * passport-sdk-version: 20353
         * X-Tt-Token: 00140a336dd81551eaa30bc0e9e8d336fd033efe6990f814c672b8aad9af6fff1d434b54377df5ec09628b8fb650416eb5a93ebb308a134f4d571c60a49d83d5394af0cdfdca3d70e20c168da5b9158f11e6ebc4fe282449dcf3ca1ac6165a643a9af-1.0.1
         * sdk-version: 2
         * X-SS-REQ-TICKET: 1665499105867
         * x-bd-client-key: 1fea6750bd5480b0f9c7e26e758639cbd4f6513a35cc7ba06f97e0f79bac532c84609486196db2930052c33d23c14e2f7ff21463cb983c77898fe100b34a3cd8
         * x-bd-kmsv: 1
         */
        Request request1 = builder.url(url1)
                .post(requestBody1)
                .addHeader("Cookie", ck)
                .addHeader("X-SS-STUB", X_SS_STUB1)
                .addHeader("x-tt-trace-id", tarceid1)
                .addHeader("User-Agent", "com.ss.android.article.news/8960 (Linux; U; Android 10; zh_CN; PACT00; Build/QP1A.190711.020; Cronet/TTNetVersion:68deaea9 2022-07-19 QuicVersion:12a1d5c5 2022-06-22)")
                .addHeader("X-Gorgon", x_gorgon1)
                .addHeader("X-Khronos", x_khronos1)
                .build();
        Response response1 = client.newCall(request1).execute();
        String bodyRes1 = response1.body().string();
        response1.close();
        if (bodyRes1.contains("order_id")) {
            log.info("放入数据库");
            log.info("放入设备号锁定数据库id:{}", deiviesId);
            DouyinDeviceIid build = DouyinDeviceIid.builder().id(deiviesId.intValue()).failReason(DateUtil.formatDateTime(new Date())).deviceId(device_id).iid(iid).build();
            jedis.set("抖音锁定设备:" + deiviesId, JSON.toJSONString(build));
            log.info("放入redis");
            log.info("查询用户信息");
            jedis.set("抖音和设备号关联:" + uid.trim(), JSON.toJSONString(build));
            log.info("uid：{}", uid);
            log.info("放入关系成功");
            String s = jedis.get("抖音和设备号关联:" + uid.trim());
            log.info("数据库查询管理关系为msg:{}", s);
            return true;
        }
        log.info("msg:{}", bodyRes1);
        return false;
    }
}
