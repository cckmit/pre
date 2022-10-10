package com.xd.pre.modules.px.vo.tmpvo.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouPonParam {
    private String batchId;
    private String actId;
    private String ckey;
}
