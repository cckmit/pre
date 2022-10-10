package com.xd.pre.modules.px.jddj.orderdetail;

import lombok.Data;

@Data
public class OrderDetailBody {

    public OrderDetailBody(String orderId) {
        this.orderId = orderId;
    }

    /**
     * {
     * "orderId": "2214130700000086",
     * "pageSource": "orderDetail",
     * "ctp": "myorderdetail"
     * }
     */


    private String orderId;
    private String pageSource = "orderDetail";
    private String ctp = "myorderdetail";

}
