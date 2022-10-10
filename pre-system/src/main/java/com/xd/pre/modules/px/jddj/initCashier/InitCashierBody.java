package com.xd.pre.modules.px.jddj.initCashier;

import lombok.Data;

@Data
public class InitCashierBody {

    public InitCashierBody(String token) {
        this.token = token;
    }

    /**
     * {
     * "token": "k2EKRvZChKQzOqdLjygNzhXQEBKHIcziwANETkM+a2nAGepr7mq/HLwy8w1PCokt6RHvVXp3y00xEXVcN32mVDGzNWCtzLZHA1Jak6/B0EoAlsC2el8grVtRzjD3v1OCbb8yCbElti+hjI7T0zzTKeUkn2jfbO+P94Ms8zSA9G4=",
     * "pageSource": "newPay",
     * "ctp": "newpay"
     * }
     */
    private String token;
    private String pageSource = "newPay";
    private String ctp = "newpay";
    private String refPar;
}
