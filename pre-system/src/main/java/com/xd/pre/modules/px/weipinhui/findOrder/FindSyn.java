package com.xd.pre.modules.px.weipinhui.findOrder;

import com.xd.pre.modules.px.weipinhui.create.WphParamCaptchaTokenAndUUID;
import com.xd.pre.modules.sys.domain.JdOrderPt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindSyn {
    private JdOrderPt jdOrderPt;
    private WphParamCaptchaTokenAndUUID wphParamCaptchaTokenAndUUID;
    private String code;
    private String ckCidAndSidAndTank;
    private Date createTime;
}
