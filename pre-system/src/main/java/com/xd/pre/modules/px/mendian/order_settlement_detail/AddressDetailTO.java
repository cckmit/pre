package com.xd.pre.modules.px.mendian.order_settlement_detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressDetailTO {
    private double addressLat;
    private int addressType;
    private int cityId;
    private int townId;
    private int provinceId;
    private String areaDetail;
    private boolean addressDefault;
    private String addressDetail;
    private int countyId;
    private double addressLng;

}
