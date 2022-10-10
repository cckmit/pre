package com.xd.pre.modules.px.vo.reqvo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KaMiVo {
    private String ptPin;
    private String orderId;
    private Integer groupNum;
}
