package com.xd.pre.modules.px.douyin.submit;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xd.pre.modules.px.douyin.buyRender.BuyRenderParamDto;
import com.xd.pre.modules.px.douyin.buyRender.res.BuyRenderRoot;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubmitUtils {

    public static String buildBuyRenderParamData(BuyRenderParamDto buyRenderParamDto) {
        String data = String.format("{" +
                        "\"address\": null," +
                        "\"platform_coupon_id\": null," +
                        "\"kol_coupon_id\": null," +
                        "\"auto_select_best_coupons\": true," +
                        "\"customize_pay_type\": \"{\\\"checkout_id\\\":1,\\\"bio_type\\\":\\\"1\\\"}\"," +
                        "\"first_enter\": true," +
                        "\"source_type\": \"1\"," +
                        "\"shape\": 0," +
                        "\"marketing_channel\": \"\"," +
                        "\"forbid_redpack\": false," +
                        "\"support_redpack\": true," +
                        "\"use_marketing_combo\": false," +
                        "\"entrance_params\": \"{\\\"product_source_page\\\":\\\"h5_order_detail\\\",\\\"carrier_source\\\":\\\"store_page\\\",\\\"source_method\\\":\\\"featured\\\",\\\"ecom_group_type\\\":\\\"video\\\",\\\"tab_label\\\":\\\"all_product\\\",\\\"follow_status\\\":\\\"1\\\",\\\"temp_id\\\":\\\"7107102390825795875-0-0-0\\\",\\\"store_type\\\":\\\"shop\\\",\\\"search_params\\\":{},\\\"ecom_scene_id\\\":\\\"1003\\\",\\\"request_id\\\":\\\"2022092919562801021010101831054056\\\",\\\"discount_type\\\":\\\"{\\\\\\\"1\\\\\\\":\\\\\\\"7148734019755082018\\\\\\\"}\\\",\\\"recommend_info\\\":\\\"{\\\\\\\"uid\\\\\\\":659356656346136,\\\\\\\"gid\\\\\\\":3561751789252519688,\\\\\\\"prd\\\\\\\":{\\\\\\\"pnum\\\\\\\":1,\\\\\\\"pname\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"rsn\\\\\\\":\\\\\\\"vk_store:7:0.359714,s2i:7:1664371329.699567\\\\\\\",\\\\\\\"new\\\\\\\":1,\\\\\\\"idx\\\\\\\":0,\\\\\\\"impr\\\\\\\":0,\\\\\\\"vk_store\\\\\\\":\\\\\\\"0.359714\\\\\\\",\\\\\\\"simid\\\\\\\":3561751789252519688,\\\\\\\"spu_simid\\\\\\\":7091155644287287565,\\\\\\\"lctg\\\\\\\":28660,\\\\\\\"ctg1\\\\\\\":20118,\\\\\\\"ai_ctg\\\\\\\":20118,\\\\\\\"dprice\\\\\\\":9400,\\\\\\\"ai_lctg\\\\\\\":28660,\\\\\\\"price_comp\\\\\\\":4,\\\\\\\"price_comp_v2\\\\\\\":-1,\\\\\\\"price_comp_sc\\\\\\\":\\\\\\\"0.000000\\\\\\\",\\\\\\\"camp\\\\\\\":1,\\\\\\\"pred_sc\\\\\\\":\\\\\\\"1000.216553\\\\\\\",\\\\\\\"r_dw_sc\\\\\\\":0.0,\\\\\\\"prd_clk\\\\\\\":\\\\\\\"0.325629\\\\\\\",\\\\\\\"prd_buy\\\\\\\":0.8034974336624146,\\\\\\\"ord_sub\\\\\\\":\\\\\\\"0.724957\\\\\\\",\\\\\\\"bst2\\\\\\\":\\\\\\\"1.000000\\\\\\\",\\\\\\\"ori_ctr\\\\\\\":\\\\\\\"0.325629\\\\\\\",\\\\\\\"ori_cvr\\\\\\\":\\\\\\\"0.724957\\\\\\\",\\\\\\\"ori_slide\\\\\\\":\\\\\\\"0.000000\\\\\\\",\\\\\\\"ori_cu\\\\\\\":\\\\\\\"1000.216553\\\\\\\"},\\\\\\\"chnid\\\\\\\":1111836,\\\\\\\"ent\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"appid\\\\\\\":1128,\\\\\\\"req_id\\\\\\\":\\\\\\\"2022092919562801021010101831054056\\\\\\\",\\\\\\\"loc\\\\\\\":620602,\\\\\\\"tab\\\\\\\":1,\\\\\\\"cs_pid\\\\\\\":0,\\\\\\\"cs_tid\\\\\\\":0,\\\\\\\"cs_s\\\\\\\":0,\\\\\\\"spw\\\\\\\":{\\\\\\\"enable\\\\\\\":0},\\\\\\\"mix\\\\\\\":{\\\\\\\"ctr\\\\\\\":0.0,\\\\\\\"cvr\\\\\\\":0.0,\\\\\\\"pos\\\\\\\":0,\\\\\\\"tabid\\\\\\\":1,\\\\\\\"pnum\\\\\\\":1,\\\\\\\"price\\\\\\\":0,\\\\\\\"type\\\\\\\":1003,\\\\\\\"sig\\\\\\\":0},\\\\\\\"mrc\\\\\\\":4690070}\\\",\\\"insurance_commodity_flag\\\":0,\\\"card_status\\\":\\\"\\\",\\\"anchor_id\\\":\\\"4051040200033531\\\",\\\"full_return\\\":\\\"0\\\",\\\"is_exist_size_tab\\\":\\\"0\\\",\\\"rank_id_source\\\":\\\"\\\",\\\"show_rank\\\":\\\"not_in_rank\\\",\\\"warm_up_status\\\":\\\"0\\\",\\\"auto_coupon\\\":1,\\\"coupon_id\\\":\\\"7148769248709460237\\\",\\\"brand_verified\\\":\\\"0\\\",\\\"label_name\\\":\\\"\\\",\\\"with_sku\\\":\\\"0\\\",\\\"is_replay\\\":\\\"0\\\",\\\"is_package_sale\\\":\\\"0\\\",\\\"is_groupbuying\\\":\\\"0\\\"}\"," +
                        "\"shop_requests\": [" +
                        "{" +
                        "\"shop_id\": \"%s\"," +
                        "\"product_requests\": [" +
                        "{" +
                        "\"product_id\": \"%s\"," +
                        "\"sku_id\": \"%s\"," +
                        "\"sku_num\": 1," +
                        "\"author_id\": \"%s\"," +
                        "\"ecom_scene_id\": \"%s\"," +
                        "\"origin_id\": \"%s\"," +
                        "\"origin_type\": \"%s\"," +
                        "\"new_source_type\": \"product_detail\"," +
                        "\"select_privilege_properties\": []" +
                        "}" +
                        "]" +
                        "}" +
                        "]" +
                        "}",
                buyRenderParamDto.getShop_id(), buyRenderParamDto.getProduct_id(), buyRenderParamDto.getSku_id(), buyRenderParamDto.getAuthor_id(), buyRenderParamDto.getEcom_scene_id(),
                buyRenderParamDto.getOrigin_id(), buyRenderParamDto.getOrigin_type());
        JSONObject parseObject = JSON.parseObject(data);
        return JSON.toJSONString(parseObject);

    }


    public static String buildBuyRenderYongHui(BuyRenderParamDto buyRenderParamDto) {
        String data = String.format("{ " +
                        " \"address\": null, " +
                        " \"platform_coupon_id\": null, " +
                        " \"kol_coupon_id\": null, " +
                        " \"auto_select_best_coupons\": true, " +
                        " \"customize_pay_type\": \"{\\\"checkout_id\\\":1,\\\"bio_type\\\":\\\"1\\\"}\", " +
                        " \"first_enter\": true, " +
                        " \"source_type\": \"1\", " +
                        " \"shape\": 0, " +
                        " \"marketing_channel\": \"\", " +
                        " \"forbid_redpack\": false, " +
                        " \"support_redpack\": true, " +
                        " \"use_marketing_combo\": false, " +
                        " \"entrance_params\": \"{\\\"ecom_scene_id\\\":\\\"1082\\\",\\\"carrier_source\\\":\\\"search_order_center\\\",\\\"source_method\\\":\\\"product_card\\\",\\\"ecom_group_type\\\":\\\"video\\\",\\\"search_params\\\":\\\"{\\\\\\\"search_id\\\\\\\":\\\\\\\"202210101249590102120810881501C937\\\\\\\",\\\\\\\"search_result_id\\\\\\\":\\\\\\\"%s\\\\\\\"}\\\",\\\"card_status\\\":\\\"\\\",\\\"discount_type\\\":\\\"\\\",\\\"full_return\\\":\\\"0\\\",\\\"is_exist_size_tab\\\":\\\"0\\\",\\\"rank_id_source\\\":\\\"\\\",\\\"show_rank\\\":\\\"not_in_rank\\\",\\\"warm_up_status\\\":\\\"0\\\",\\\"coupon_id\\\":\\\"\\\",\\\"brand_verified\\\":\\\"2\\\",\\\"label_name\\\":\\\"\\\",\\\"with_sku\\\":\\\"0\\\",\\\"is_replay\\\":\\\"0\\\",\\\"is_package_sale\\\":\\\"0\\\",\\\"is_groupbuying\\\":\\\"0\\\"}\", " +
                        " \"shop_requests\": [ " +
                        "  { " +
                        "   \"shop_id\": \"%s\", " +
                        "   \"product_requests\": [ " +
                        "    { " +
                        "     \"product_id\": \"%s\", " +
                        "     \"sku_id\": \"%s\", " +
                        "     \"sku_num\": 1, " +
                        "     \"ecom_scene_id\": \"%s\", " +
                        "     \"origin_id\": \"%s\", " +
                        "     \"origin_type\": \"%s\", " +
                        "     \"new_source_type\": \"product_detail\", " +
                        "     \"select_privilege_properties\": [] " +
                        "    } " +
                        "   ] " +
                        "  } " +
                        " ] " +
                        "}",
                buyRenderParamDto.getProduct_id(),
                buyRenderParamDto.getShop_id(), buyRenderParamDto.getProduct_id(),
                buyRenderParamDto.getSku_id(),buyRenderParamDto.getEcom_scene_id(),
                buyRenderParamDto.getOrigin_id(), buyRenderParamDto.getOrigin_type());
        log.info(data);
        JSONObject parseObject = JSON.parseObject(data);
        return JSON.toJSONString(parseObject);

    }

    public static String buildSubmitData(BuyRenderRoot buyRenderRoot, String ip, String phone, String skuId, String productId
            , Integer payType) {
        String format = String.format("{" +
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
                        "\"extra\": \"{\\\"page_type\\\":\\\"lynx\\\",\\\"render_track_id\\\":\\\"%s\\\",\\\"risk_info\\\":\\\"{\\\\\\\"biometric_params\\\\\\\":\\\\\\\"1\\\\\\\",\\\\\\\"is_jailbreak\\\\\\\":\\\\\\\"2\\\\\\\",\\\\\\\"openudid\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"order_page_style\\\\\\\":0,\\\\\\\"checkout_id\\\\\\\":1,\\\\\\\"ecom_payapi\\\\\\\":true,\\\\\\\"ip\\\\\\\":\\\\\\\"%s\\\\\\\",\\\\\\\"sub_order_info\\\\\\\":[]}\\\"}\"," +
                        "\"marketing_plan_id\": \"%s\"," +
                        "\"s_type\": \"\"," +
                        "\"entrance_params\": \"{\\\"previous_page\\\":\\\"toutiao_mytab\\\",\\\"new_source_type\\\":\\\"order_list_page\\\",\\\"carrier_source\\\":\\\"order_list_page\\\",\\\"source_method\\\":\\\"order_buy_once\\\",\\\"is_groupbuying\\\":0,\\\"extra_campaign_type\\\":\\\"\\\"}\"," +
                        "\"sub_b_type\": \"3\"," +
                        "\"gray_feature\": \"PlatformFullDiscount\"," +
                        "\"sub_way\": 0," +
                        "\"pay_type\": %s," +
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
                        "\"post_tel\": \"%s\"," +
                        "\"address_id\": \"0\"," +
                        "\"price_info\": {" +
                        "\"origin\": 1000," +
                        "\"freight\": 0," +
                        "\"coupon\": 0," +
                        "\"pay\": 1000" +
                        "}," +
                        "\"pay_info\": \"{\\\"sdk_version\\\":\\\"v2\\\",\\\"dev_info\\\":{\\\"reqIp\\\":\\\"%s\\\",\\\"os\\\":\\\"android\\\",\\\"isH5\\\":false,\\\"cjSdkVersion\\\":\\\"6.3.5\\\",\\\"aid\\\":\\\"13\\\",\\\"ua\\\":\\\"com.ss.android.article.news/8960 (Linux; U; Android 10; zh_CN; PACT00; Build/QP1A.190711.020; Cronet/TTNetVersion:68deaea9 2022-07-19 QuicVersion:12a1d5c5 2022-06-27)\\\",\\\"riskUa\\\":\\\"\\\",\\\"lang\\\":\\\"zh-Hans\\\",\\\"deviceId\\\":\\\"%s\\\",\\\"osVersion\\\":\\\"10\\\",\\\"vendor\\\":\\\"\\\",\\\"model\\\":\\\"\\\",\\\"netType\\\":\\\"\\\",\\\"appVersion\\\":\\\"8.9.6\\\",\\\"appName\\\":\\\"news_article\\\",\\\"devicePlatform\\\":\\\"android\\\",\\\"deviceType\\\":\\\"PACT00\\\",\\\"channel\\\":\\\"oppo_13_64\\\",\\\"openudid\\\":\\\"\\\",\\\"versionCode\\\":\\\"896\\\",\\\"ac\\\":\\\"wifi\\\",\\\"brand\\\":\\\"OPPO\\\",\\\"iid\\\":\\\"%s\\\",\\\"bioType\\\":\\\"1\\\"},\\\"credit_pay_info\\\":{\\\"installment\\\":\\\"1\\\"},\\\"bank_card_info\\\":{},\\\"voucher_no_list\\\":[],\\\"zg_ext_param\\\":\\\"{\\\\\\\"decision_id\\\\\\\":\\\\\\\"%s\\\\\\\",\\\\\\\"qt_c_pay_url\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"retain_c_pay_url\\\\\\\":\\\\\\\"\\\\\\\"}\\\",\\\"jh_ext_info\\\":\\\"{\\\\\\\"payapi_cache_id\\\\\\\":\\\\\\\"%s\\\\\\\"}\\\",\\\"sub_ext\\\":\\\"\\\",\\\"biometric_params\\\":\\\"1\\\",\\\"is_jailbreak\\\":\\\"2\\\",\\\"order_page_style\\\":0,\\\"checkout_id\\\":1,\\\"pay_amount_composition\\\":[]}\"," +
                        "\"render_token\": \"%s\"," +
                        "\"win_record_id\": \"\"," +
                        "\"marketing_channel\": \"\"," +
                        "\"identity_card_id\": \"\"," +
                        "\"pay_amount_composition\": []," +
                        "\"user_account\": {}," +
                        "\"queue_count\": 0," +
                        "\"store_id\": \"\"," +
                        "\"shop_id\": \"GceCTPIk\"," +
                        "\"combo_id\": \"%s\"," +
                        "\"combo_num\": 1," +
                        "\"product_id\": \"%s\"," +
                        "\"buyer_words\": \"\"," +
                        "\"stock_info\": [" +
                        "{" +
                        "\"stock_type\": 1," +
                        "\"stock_num\": 1," +
                        "\"sku_id\": \"%s\"," +
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
                        "}", buyRenderRoot.getRender_track_id(), ip, buyRenderRoot.getTotal_price_result().getMarketing_plan_id(), payType, phone, ip, "321506381413262", "119169286681983",
                buyRenderRoot.getPay_method().getDecision_id(), buyRenderRoot.getPay_method().getPayapi_cache_id(), buyRenderRoot.getRender_token(),
                skuId, productId, skuId
        );
        log.info("下单数据msg:{}", format);
        return format;
    }
}
