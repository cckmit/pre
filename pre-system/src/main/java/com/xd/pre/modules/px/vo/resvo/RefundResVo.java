package com.xd.pre.modules.px.vo.resvo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefundResVo {
    private String fileName;
    private Integer refundNum;
    private Integer successNum;
}
