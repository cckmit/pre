package com.xd.pre.modules.px.vo.resvo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlowingWaterResVo {
    private Integer createOrderNums;
    private Integer successOrderNums;
    private BigDecimal totalFlowingWater;

    private BigDecimal successFlowingWater;
    private BigDecimal failFlowingWater;

    private BigDecimal noMatchFlowingWater;

    private BigDecimal successRate;
}
