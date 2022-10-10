package com.xd.pre.modules.px.jddj;

import lombok.Data;
import org.omg.CORBA.INTERNAL;

@Data
public class Max5Dto {
    /**
     * SELECT
     * 	op.is_wx_success as isWxSuccess,
     * 	op.pt_pin as ptPin,
     * 	count( 1 ) as count
     * FROM
     * 	jd_ck_zhideng jcz
     * 	LEFT JOIN jd_order_pt op ON op.pt_pin = jcz.pt_pin
     * WHERE
     * 	jcz.is_enable = 1
     * GROUP BY
     * 	op.is_wx_success,
     * 	op.pt_pin
     * HAVING
     * 	count( 1 ) >= 5
     * 	AND op.is_wx_success =1
     */
    private Integer isWxSuccess;
    private String ptPin;
    private Integer count;
}
