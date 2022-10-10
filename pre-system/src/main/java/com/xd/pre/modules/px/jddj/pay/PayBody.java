package com.xd.pre.modules.px.jddj.pay;

import lombok.Data;

@Data
public class PayBody {


    public PayBody(String token, long orderId) {
        this.token = token;
        this.orderId = orderId;
    }

    /**
     * {
     * "token": "k2EKRvZChKQzOqdLjygNzhXQEBKHIcziwANETkM+a2nAGepr7mq/HLwy8w1PCokt6RHvVXp3y00xEXVcN32mVDGzNWCtzLZHA1Jak6/B0EoAlsC2el8grVtRzjD3v1OCbb8yCbElti+hjI7T0zzTKeUkn2jfbO+P94Ms8zSA9G4=",
     * "orderId": 2214021782000184,
     * "payMode": 10,
     * "payModeType": 0,
     * "pageSource": "newPay",
     * "ctp": "newpay"
     * }
     */
    private String token;
    private long orderId;
    private int payMode = 10;
    private int payModeType = 0;
    private String pageSource = "newPay";
    private String ctp = "newpay";
}
