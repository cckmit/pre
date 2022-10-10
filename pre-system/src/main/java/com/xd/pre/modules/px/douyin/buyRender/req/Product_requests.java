package com.xd.pre.modules.px.douyin.buyRender.req;

import lombok.Data;

import java.util.List;

@Data
public class Product_requests {
    private String product_id;
    private String sku_id;
    private int sku_num;
    private String new_source_type;
    private List<String> select_privilege_properties;

}
