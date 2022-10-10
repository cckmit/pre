package com.xd.pre.modules.px.douyin.buyRender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuyRenderParamDto {

//					"product_id": "3561751789252519688",
//                            "sku_id": "1739136614382624",
//                            "sku_num": 1,
//                            "author_id": "4051040200033531",
//                            "ecom_scene_id": "1003",
//                            "origin_id": "4051040200033531_3561751789252519688",
//                            "origin_type": "3002002002",
//                            "new_source_type": "product_detail",

    private String product_id;
    private String sku_id;
    private String author_id;
    private String ecom_scene_id;
    private String origin_id;
    private String origin_type;
    private String new_source_type;
    private String price;
    private String shop_id;

}
