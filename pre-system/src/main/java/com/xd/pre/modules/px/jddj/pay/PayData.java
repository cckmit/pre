package com.xd.pre.modules.px.jddj.pay;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * java 支付实体类
 */
public class PayData {

    private String appid;
    private String partnerid;
    private String prepayid;
    private String noncestr;
    private String timestamp;
    private String sign;
    private String signType;
    private String mweburl;
    private String packageStr = "WAP";
    private String hrefUrl;
}
