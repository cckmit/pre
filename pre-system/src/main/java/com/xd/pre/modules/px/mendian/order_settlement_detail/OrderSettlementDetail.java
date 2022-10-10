package com.xd.pre.modules.px.mendian.order_settlement_detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderSettlementDetail {
    private SettlemnetSku settlemnetSku;
    private AddressDetailTO addressDetailTO;
    private DeliveryWayList deliveryWayList;
}
