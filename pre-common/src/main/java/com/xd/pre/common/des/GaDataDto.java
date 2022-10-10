package com.xd.pre.common.des;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GaDataDto {
    private long orderId;
    private long skuId;
    private String logo;
    private String title;
    private int jdPrice;
    private int buyNum;
    private int orderStatus;
    private String orderStatusStr;
    private int totalPrice;
    private int couponPay;
    private int payMode;
    private int jBeanPay;
    private int onlinePay;
    private int chargeType;
    private String cardInfos;
    private Date created;
    private String payBackUrl;
    private long venderId;
    private String qualificationFileUrl;
    private String totalPriceStr;
    private String onlinePayStr;
    private String orderStatusName;
    private String payTypeShow;
    private boolean cancelFlag;
    private List<List<String>> showInfo;
    private List<CardNoDto> cardNoDtos;
}
