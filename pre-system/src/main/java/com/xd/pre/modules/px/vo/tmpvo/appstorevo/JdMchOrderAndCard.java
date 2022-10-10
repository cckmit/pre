package com.xd.pre.modules.px.vo.tmpvo.appstorevo;

import com.xd.pre.modules.sys.domain.JdMchOrder;
import lombok.Data;

@Data
public class JdMchOrderAndCard extends JdMchOrder {
    private String createTimeStr;
    private String paySuccessTime;
    private String skuName;
    private String cardNumber;
    //    private String carMy;
    private Integer ptId;

    // 添加字段
    private String html;
    private Integer failTime;
    //一下日志信息
    private String userAgent;
    private String userIp;
    private String orgAppCk;
    private String ptPin;
}
