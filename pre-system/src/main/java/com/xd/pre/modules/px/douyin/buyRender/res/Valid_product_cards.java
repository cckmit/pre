/**
 * Copyright 2022 bejson.com
 */
package com.xd.pre.modules.px.douyin.buyRender.res;

import lombok.Data;

import java.util.List;


@Data
public class Valid_product_cards {

    private String product_id;
    private String title;
    private String pic;
    private String sku_id;
    private List<Spec_info> spec_info;
    private int biz_kind;
    private String cart_id;
    private int price;
    private int pay_type;
    private boolean is_virtual;
    private boolean is_presell;
    private boolean is_topup;
    private boolean is_multi_phase;
    private String multi_phase;
    private boolean is_cross_board;
    private int cross_board_type;
    private int buy_num;
    private Stock_info stock_info;
    private Limit_info limit_info;
    private boolean is_limited;
    private String limit_reason;
    private String limit_reason_code;
    private String expect_ship_time;
    private Delivery_info delivery_info;
    private List<Service_tags> service_tags;
    private List<String> stock_tags;
    private int render_biz;
    private String extra;
    private Product_category product_category;
    private String render_sku_extra;
    private String store_info;
    private Product_biz_identity product_biz_identity;
    private List<String> privilege_tag_keys;
    private List<String> select_privilege_properties;


}