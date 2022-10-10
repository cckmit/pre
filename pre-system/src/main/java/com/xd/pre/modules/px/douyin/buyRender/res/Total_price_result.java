/**
  * Copyright 2022 bejson.com 
  */
package com.xd.pre.modules.px.douyin.buyRender.res;

import lombok.Data;


@Data
public class Total_price_result {

    private int total_amount;
    private int total_origin_amount;
    private int total_full_reduce_amount;
    private int total_coupon_amount;
    private int total_freight_amount;
    private int total_freight_after_deduction_amount;
    private int total_tax_amount;
    private int total_redpack_amount;
    private String total_deduction_amount_map;
    private String tip;
    private Total_shop_discount_detail total_shop_discount_detail;
    private String marketing_plan_id;
    private Shop_sku_map shop_sku_map;
    private String total_platform_discount_detail;
    private String total_kol_discount_detail;


}