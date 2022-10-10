package com.xd.pre.modules.px.vo.resvo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckCkData {
    private Integer repeat;
    private Integer success;
    private Integer fail;
}
