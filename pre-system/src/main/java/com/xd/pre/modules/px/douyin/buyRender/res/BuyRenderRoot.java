package com.xd.pre.modules.px.douyin.buyRender.res;

import lombok.Data;

import java.util.List;

@Data
public class BuyRenderRoot {
    private List<Shop_product_cards> shop_product_cards;
    private boolean all_forbidden;
    private String all_forbidden_reason;
    private Shop_info_map shop_info_map;
    private Sku_insurance_map sku_insurance_map;
    private Sku_campaign_info_map sku_campaign_info_map;
    private Top_alert_info top_alert_info;
    private String address;
    private String leave_sub_title;
    private Coupon_result coupon_result;
    private Marquee_info marquee_info;
    private Pay_method pay_method;
    private String cross_board_info;
    private Total_price_result total_price_result;
    private String tip;
    private String render_token;
    private String render_track_id;
    private Ab_test_info ab_test_info;
    private Identity_info identity_info;
    private String over_layer;
    private List<String> service_guarantee_infos;
    private String post_tel;
}
