package com.xd.pre.modules.px.weipinhui.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WphParamCaptchaTokenAndUUID {
    private String uuid;
    private String captchaToken;
    private String picUuid;
    private String pic;

}
