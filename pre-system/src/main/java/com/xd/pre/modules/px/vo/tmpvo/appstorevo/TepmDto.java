package com.xd.pre.modules.px.vo.tmpvo.appstorevo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TepmDto {
    private String currentCk;
    private String skuId;
    private String orderId;
    private String payId;
    private String ptPin;
    private String skuName;
    private Date expireTime;
    private BigDecimal skuPrice;
}
