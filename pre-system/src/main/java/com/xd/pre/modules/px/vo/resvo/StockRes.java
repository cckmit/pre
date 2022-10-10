package com.xd.pre.modules.px.vo.resvo;

import lombok.Data;

@Data
public class StockRes {
    private String skuName;
    private String skuId;
    private Integer stock;
    private Integer productStock;
    private Integer realTimeStock;
    private Integer lockStock;
    private Integer surplusStock;
}
