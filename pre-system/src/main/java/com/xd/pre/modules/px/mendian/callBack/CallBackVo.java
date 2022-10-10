package com.xd.pre.modules.px.mendian.callBack;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CallBackVo {
    private String orderId;
    private String tenantCode = "jgm";
    private String bizModelCode = "2";
    private String bizModeClientType = "M";
    private String bizModeFramework = "Taro";
    private int externalLoginType = 1;

    public CallBackVo(String orderId) {
        this.orderId = orderId;
    }
}
