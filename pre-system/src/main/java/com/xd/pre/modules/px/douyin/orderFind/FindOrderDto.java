package com.xd.pre.modules.px.douyin.orderFind;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindOrderDto {
    private String ck;
    private String orderId;
    private String device_id;
    private String iid;
}
