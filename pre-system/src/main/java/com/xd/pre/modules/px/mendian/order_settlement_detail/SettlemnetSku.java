package com.xd.pre.modules.px.mendian.order_settlement_detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SettlemnetSku {
    private String skuName;
    private String imgUrl;
    private String price;
    private boolean stockEnough;
    private int spuId;
    private long skuId;
    private int skuCount;
    private int status;


}
