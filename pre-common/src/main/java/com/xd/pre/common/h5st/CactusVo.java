package com.xd.pre.common.h5st;

import cn.hutool.core.date.DateUtil;
import com.xd.pre.common.utils.px.PreUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CactusVo {

    private String version = "3.0";
    private String fp = PreUtils.getRandomNum(16);
    private String appId = "8108f";
    private Long timestamp = new Date().getTime();
    private String platform = "web";
    private String expandParams = "";
    private String tk;
    private String rd;
    private String hmacSHA;
    private String ts = DateUtil.format(new Date(),"yyyyMMddHHmmssSSS");
    private String algo;
    private String bodyStr;
    private String bodySign;
    private String lastMd;
    private String h5st;
    private String functionId;
}
