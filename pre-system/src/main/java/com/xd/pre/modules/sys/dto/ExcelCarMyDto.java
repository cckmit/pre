package com.xd.pre.modules.sys.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelCarMyDto {
    /**
     * writer.addHeaderAlias("skuName", "商品名称");
     * writer.addHeaderAlias("skuPrice", "价格");
     * writer.addHeaderAlias("cardNumber", "账号");
     * writer.addHeaderAlias("carMy", "卡密");
     * writer.addHeaderAlias("paySuccessTime", "支付成功时间");
     */
    private String skuName;
    private BigDecimal skuPrice;
    private String paySuccessTime;
    private String cardNumber;
    private String carMy;
}
