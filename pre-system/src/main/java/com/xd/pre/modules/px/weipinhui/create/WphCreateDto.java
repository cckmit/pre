package com.xd.pre.modules.px.weipinhui.create;


import com.alibaba.fastjson.JSON;
import com.xd.pre.modules.px.weipinhui.CaptchaXY;
import com.xd.pre.modules.sys.domain.JdAppStoreConfig;
import com.xd.pre.modules.sys.domain.WphAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WphCreateDto {

    private WphParamCaptchaTokenAndUUID wphParamCaptchaTokenAndUUID;
    private String mars_cid;
    private String mars_sid;
    private String phone;
    private WphAccount wphAccount;
    private String code;
    private CreateCaptchaFlow createCaptchaFlow;
    //    private String productId;
    private OrderData orderData;
    private JdAppStoreConfig jdAppStoreConfig;

    public String getCidAndSidAndTank() {
        CaptchaXY captchaXY = JSON.parseObject(wphAccount.getLoginInfo(), CaptchaXY.class);
        String ckCidAndSidAndTank = String.format("%s;%s;VIP_TANK=%s;", this.mars_cid, this.mars_sid, captchaXY.getVipTank().getVIP_TANK());
        return ckCidAndSidAndTank;
    }
}
