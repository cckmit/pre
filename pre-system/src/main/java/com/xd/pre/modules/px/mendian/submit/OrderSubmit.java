package com.xd.pre.modules.px.mendian.submit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderSubmit {
    private int loginType=2;
    private String payLink;
    private String remark="";
    private int deliveryWay;
    private String consignee;
    private String telephone;
    private String addressDetail;
    private int code1;
    private int code2;
    private int code3;
    private int code4;
    private List<String> coupons = new ArrayList<>();
    private List<SkuList> skuList;
    private boolean JDwxapp=false;

}
