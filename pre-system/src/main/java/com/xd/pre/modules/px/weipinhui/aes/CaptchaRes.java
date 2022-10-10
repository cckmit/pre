package com.xd.pre.modules.px.weipinhui.aes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaptchaRes {
    private String captchaId;
    private String captchaType;
    private String sid;
    private String templateId;
    private MinaEdataDto minaEdataDto;
}
