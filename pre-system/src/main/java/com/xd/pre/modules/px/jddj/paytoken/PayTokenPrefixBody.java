package com.xd.pre.modules.px.jddj.paytoken;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

@Data
public class PayTokenPrefixBody {

    public PayTokenPrefixBody(String orderId, String paySource) {
        this.orderId = orderId;
        if (StrUtil.isBlank(paySource)) {
            this.paySource = 142;
        } else {
            this.paySource = Integer.valueOf(paySource);
        }
        this.successUrl = String.format("https://daojia.jd.com/html/app/giftCard/giftCardBuySuc?orderId=%s", orderId);
    }

    /**
     * {
     * "paySource": 142,
     * "orderId": "2214006516000074",
     * "successUrl": "https://daojia.jd.com/html/app/giftCard/giftCardBuySuc?orderId=2214006516000074",
     * "appId": 1,
     * "source": 2,
     * "os": "OS01",
     * "deviceType": "DT03"
     * }
     */
    private String orderId;
    private String successUrl;
    private int paySource;
    private int appId = 1;
    private int source = 2;
    private String os = "OS01";
    private String deviceType = "DT03";


}
