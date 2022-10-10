package com.xd.pre.modules.px.douyin.huadan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.OkHttpClient;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HuaDanDto {
    private String iid;
    private String device_id;
    private OkHttpClient client;
    private String ck;
    private Integer price;
    private String ip;
    private Integer port;
    private String rechargePhone;
    private HuaDanSkuVo huaDanSkuVo;
    private Boolean check;
    private Integer discountPrice;
    private Integer originalPrice;
    private String orderId;
}
