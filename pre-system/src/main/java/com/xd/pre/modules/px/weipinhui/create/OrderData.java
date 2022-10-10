package com.xd.pre.modules.px.weipinhui.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderData {
    private String orderId;
    private String orderNo;
}
