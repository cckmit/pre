package com.xd.pre.modules.px.douyin.pay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayDto {
    private String ck;
    private String orderId;
    private String device_id;
    private String iid;
    private String userIp;
    private String pay_type;//1微信，2支付宝，10抖音
}
