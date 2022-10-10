package com.xd.pre.modules.px.vo.tmpvo.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IOSMatchDto {
    private String orderId;
    private String appck;
    private String ptPin;
    private String prerId;
    private Date createTime;
    private String mck;

    private String payData;
}
