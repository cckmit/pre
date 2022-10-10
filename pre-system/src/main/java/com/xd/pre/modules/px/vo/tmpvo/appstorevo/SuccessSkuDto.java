package com.xd.pre.modules.px.vo.tmpvo.appstorevo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuccessSkuDto {
    private String skuId;
    private String skuName;
    private String orderId;
}
