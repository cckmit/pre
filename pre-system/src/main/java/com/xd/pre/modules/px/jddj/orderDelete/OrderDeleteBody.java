package com.xd.pre.modules.px.jddj.orderDelete;

import lombok.Data;

@Data
public class OrderDeleteBody {

    /**
     * {
     * "orderId": 2214027983000184,
     * "refPageSource": "orderList",
     * "pageSource": "orderDetail",
     * "ref": "myorderlist",
     * "ctp": "myorderdetail"
     * }
     */
    private long orderId;
    private String refPageSource = "orderList";
    private String pageSource = "orderDetail";
    private String ref = "myorderlist";
    private String ctp = "myorderdetail";

    public OrderDeleteBody(long orderId) {
        this.orderId = orderId;
    }
}
