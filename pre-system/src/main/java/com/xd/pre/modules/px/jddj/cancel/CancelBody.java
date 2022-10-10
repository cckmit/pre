package com.xd.pre.modules.px.jddj.cancel;

import lombok.Data;

@Data
public class CancelBody {
    /**
     * {
     * "orderId": "2214026496000285",
     * "refPageSource": "orderList",
     * "cancelReason": "3",
     * "pageSource": "orderDetail",
     * "ref": "myorderlist",
     * "ctp": "myorderdetail"
     * }
     */
    private String orderId;
    private String refPageSource = "orderList";
    private String cancelReason = "3";
    private String pageSource = "orderDetail";
    private String ref = "myorderlist";
    private String ctp = "myorderdetail";

    public CancelBody(String orderId) {
        this.orderId = orderId;
    }
}
