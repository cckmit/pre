package com.xd.pre.modules.px.douyin.buyRender.req;

import lombok.Data;

import java.util.List;

@Data
public class Shop_requests {
    private String shop_id;
    private List<Product_requests> product_requests;
}
