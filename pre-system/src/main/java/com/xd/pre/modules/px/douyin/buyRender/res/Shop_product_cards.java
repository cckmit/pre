/**
  * Copyright 2022 bejson.com 
  */
package com.xd.pre.modules.px.douyin.buyRender.res;
import lombok.Data;

import java.util.List;

@Data
public class Shop_product_cards {

    private String shop_id;
    private String shop_insurance;
    private List<Valid_product_cards> valid_product_cards;
    private List<String> invalid_product_cards;
    private String given_product_list;
    private String given_product_view_info;
    private List<String> select_privilege_properties;
    private int valid_num;
    private String shop_add_on_info;
    private String tip;


}