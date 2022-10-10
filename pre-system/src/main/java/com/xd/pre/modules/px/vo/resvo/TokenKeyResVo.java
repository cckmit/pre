package com.xd.pre.modules.px.vo.resvo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TokenKeyResVo {
    private String code;
    private String tokenKey;
    private String url;
    private String Cookie;
    //token的标识符
    private String pt_pin;
}
