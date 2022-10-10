package com.xd.pre.modules.px.mendian.address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressVo {
    private String consignee;
    private String telephone;

    private String addressDetail;
    private int provinceId;
    private int cityId;
    private int countyId;
    private int townId=0;
    private boolean addressDefault=false;

}
