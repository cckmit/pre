package com.xd.pre.modules.px.vo.reqvo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JdClientReqVo {
    private String startTime;
    private String endTime;
    private String orderStatus;
}
