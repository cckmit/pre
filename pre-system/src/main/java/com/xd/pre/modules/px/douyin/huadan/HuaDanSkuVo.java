package com.xd.pre.modules.px.douyin.huadan;

import lombok.Data;

@Data
public class HuaDanSkuVo {

    /*{
        "isDiscount": false,
            "isSp": false,
            "originalPrice": 4998,
            "platCouponId": "",
            "price": 4998,
            "priceSpec": "50元",
            "priceSubSpec": "",
            "productId": "3573011058937438936",
            "shopId": "8683547",
            "skuId": "1744634302261299",
            "stock": 1,
            "validDesc": "手机充值"
    }*/

    private boolean isDiscount;
    private boolean isSp;
    private int originalPrice;
    private String platCouponId;
    private int price;
    private String priceSpec;
    private String priceSubSpec;
    private String productId;
    private String shopId;
    private String skuId;
    private int stock;
    private String validDesc;
}
