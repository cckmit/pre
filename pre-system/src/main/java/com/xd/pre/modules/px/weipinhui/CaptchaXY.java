package com.xd.pre.modules.px.weipinhui;

import com.xd.pre.modules.px.weipinhui.aes.CaptchaRes;
import com.xd.pre.modules.px.weipinhui.aes.CheckmobileV1Dto;
import com.xd.pre.modules.px.weipinhui.token.VipTank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaptchaXY {
    private Integer x1;
    private Integer y1;

    private Integer x2;
    private Integer y2;

    private Integer x3;
    private Integer y3;

    private CaptchaRes captchaRes;
    private String ticket;
//    private String ticket2;
    private String phone;
    private CheckmobileV1Dto checkmobileV1Dto;
    private Boolean postMsg;
    private String captchaCode;
    private VipTank vipTank;


}
