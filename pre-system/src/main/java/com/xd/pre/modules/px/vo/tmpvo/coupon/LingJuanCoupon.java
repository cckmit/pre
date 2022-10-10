package com.xd.pre.modules.px.vo.tmpvo.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LingJuanCoupon {
    private int type;
    private String batchId;
    private String title;
    private String encryptedKey;
    private long ruleId;
    private String activityId;
    private int grabStatus;
    private String receiveKey;
    private int couponType;
    private int couponKind;
    private String quota;
    private String discount;
    private String tagName;
    private String useUrl;
    private List<Object> skuList;
    private int from;
    private int ynPlus;
    private String biInfo;
    private String ckey;
    private String actId;
    private String labelName;
    private int showStyle;
    private int fansTag;
    private String followStatus;
    private String fansShopId;
    private String financeKey;
    private int timePart;
    private int remainTime;
    private int nextPartTime;
    private String tagImg;
    private String appointNum;
    private int coupon_Type;
    private int ynFamily;
    private int ynPromotion;
    private String modelFlag;
    private String ynMarketCoupon;
    private String couponKey;
    private int ynViolenceCoupon;
    private String userClass;
    private int ynNecklaceExchange;
    private String necklaceToUse;
    private Object buryingInfo;
    private int ynRecommendSimilar;
    private String useTimeDesc;
    private String feedInfo;
    private String shopInfo;
    private String source;
    private String compCouponList;
    private List<String> capsuleList;
    private String homeCoupon;
    private String encryptedRuleId;
    private String riskFlag;
    private String isBenefitReceiveCoupon;
    private String benefitReceiveCouponParam;
    private int sort;
    private int sortType;
    private int manualType;
    private int manualCouponCategory;
    private String skuIdList;
    private String skinTitle;
    private String skinTitleImg;
}
