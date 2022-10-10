package com.xd.pre.modules.px.weipinhui.findOrder;

import com.xd.pre.modules.px.weipinhui.CaptchaXY;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateWphAccountSyn {
    private Map<String, String> headerMap;
    private CaptchaXY postMsgLogin;
    private String phone;
    private String code;
    private Date createDate;
}
