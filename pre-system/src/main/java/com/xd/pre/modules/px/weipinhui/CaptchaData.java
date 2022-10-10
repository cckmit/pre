package com.xd.pre.modules.px.weipinhui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaptchaData {
    private String qp;
    private String ap;
    private String captchaId;
}
