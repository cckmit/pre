package com.xd.pre.modules.px.weipinhui.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCaptchaFlow {

    /**
     * "captchaId": "1658327143665_xxxxxx16588178958737",
     * "captchaType": 0,
     * "templateId": "",
     * "extend": ""
     */
    private String captchaId;
    private Integer captchaType;
    private String templateId;
    private String extend;
}
