package com.xd.pre.modules.px.vo.tmpvo.appstorevo;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppStoreVo {
    private String xpath;
    private Integer sleepTime;
    private Integer loopTime;
    private String mark;

    public static void main(String[] args) {
        List<AppStoreVo> appStoreVos = new ArrayList<>();
        //确定按钮
        AppStoreVo appStoreVo1 = new AppStoreVo("simplePopBtnSure", 500, 1, "点击确认");
        AppStoreVo appStoreVo2 = new AppStoreVo("submitOrder", 500, 1, "提交订单");

        AppStoreVo appStoreVo3 = new AppStoreVo("payList", 500, 1, "选择支付方式");
        AppStoreVo appStoreVo4 = new AppStoreVo("pay_confirm", 500, 1, "确认支付1");
        AppStoreVo appStoreVo5 = new AppStoreVo("payBtn", 500, 1, "确认支付2");
        AppStoreVo appStoreVo6 = new AppStoreVo("logTime", 1000, 1, "获取日志");



        AppStoreVo appStoreVo7 = new AppStoreVo("orderlist_jdm", 500, 1, "https://wqs.jd.com/order/orderlist_jdm.shtml#/");
        AppStoreVo appStoreVo8 = new AppStoreVo("waitPay", 500, 1, "等待支付");
        AppStoreVo appStoreVo9 = new AppStoreVo("payNormal", 500, 1, "支付订单列表元素");
        AppStoreVo appStoreVo10 = new AppStoreVo("payByOrderId", 500, 1, "根据订单号来支付");



        appStoreVos.add(appStoreVo1);
        appStoreVos.add(appStoreVo2);
        appStoreVos.add(appStoreVo3);
        String a = "";

        appStoreVos.add(appStoreVo4);
        appStoreVos.add(appStoreVo5);
        appStoreVos.add(appStoreVo6);

        appStoreVos.add(appStoreVo7);
        appStoreVos.add(appStoreVo8);
        appStoreVos.add(appStoreVo9);
        appStoreVos.add(appStoreVo10);
        System.out.println(JSON.toJSONString(appStoreVos));
    }

}
