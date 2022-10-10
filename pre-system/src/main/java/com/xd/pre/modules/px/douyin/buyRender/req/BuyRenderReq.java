package com.xd.pre.modules.px.douyin.buyRender.req;

import lombok.Data;

import java.util.List;

@Data
public class BuyRenderReq {

    private String address;
    private String display_scene="buy_again";
    private String platform_coupon_id;
    private String kol_coupon_id;
    private boolean auto_select_best_coupons=true;
    private String customize_pay_type;
    private boolean first_enter;
    private String source_type;
    private int shape;
    private String marketing_channel;
    private boolean forbid_redpack;
    private boolean support_redpack;
    private boolean use_marketing_combo;
    private String entrance_params;
    private List<Shop_requests> shop_requests;
}
