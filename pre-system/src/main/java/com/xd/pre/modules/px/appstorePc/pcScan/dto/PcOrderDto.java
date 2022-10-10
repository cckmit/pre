package com.xd.pre.modules.px.appstorePc.pcScan.dto;

import com.xd.pre.modules.px.appstorePc.pcScan.PcThorDto;
import com.xd.pre.modules.sys.domain.JdCk;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PcOrderDto {

    private String orderId;
    private String payUrl301_1;
    private String payUrl301_2;
    private String payUrl301_3;
    private String reqInfo;
    private String sign;
    private String deviceId;
    private String fingerprint;
    private String pageId;
    private String paySign;
    private String channelSign;
    private String shouldPay;
    private String bankcode;
    private String agencyCode;
    private String weixinConfirm;
    private String qrCodeSign;
    private String tokenKey;
    private JdCk jdCk;
    private String weixinImageUrl;
    private Boolean isYouka = false;
    private String bizpayurl;
    private PcThorDto pcThorDto;
    private Integer orderStatus;
    private String orderInfo;
}
