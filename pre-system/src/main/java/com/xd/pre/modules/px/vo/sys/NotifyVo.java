package com.xd.pre.modules.px.vo.sys;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotifyVo {
    private String mch_id;
    private String trade_no;
    private String out_trade_no;
    private String original_trade_no;
    private String money;
    private String notify_time;
    private String status;
}
