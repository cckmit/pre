package com.xd.pre.modules.px.weipinhui.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VipTank {
    /**
     * {
     * "code": 1,
     * "data": {
     * "uid": 519401605,
     * "msg": "success",
     * "VIP_TANK": "2CC07B38D1A418CC06E94CEC00C0C81EF95F1F2A",
     * "TANK_EXPIRE": 25200,
     * "isGray": 1
     * },
     * "msg": "success"
     * }
     */
    private Long uid;
    private String msg;
    private String VIP_TANK;
    private Integer TANK_EXPIRE;
    private Integer isGray;
    private String  createDate;
}
