package com.xd.pre.modules.px.cotroller;

import cn.hutool.crypto.SecureUtil;

public class PaySign {
    public static void main(String[] args) {
        getPaySign("247982124420","100.00");
    }

    public static String getPaySign(String orderId,String price) {
        /**
         *             string appid = "jd_android_app4";
         *             string order_id = "245861173069";
         *             string order_type = "34";
         *             string price = "10.00";//直接取值，和数据包一致，不需要补0
         *
         *             string str = appid + ";" + order_id + ";" + order_type + ";" + price + ";" + "e53jfgRgd7Hk";
         *             str = Pub.md5(str).ToLower();
         *             Console.WriteLine("paysign->"+str);
         */
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("jd_android_app4;");
        stringBuilder.append(orderId+";");
        stringBuilder.append("34;");
        stringBuilder.append(price+";");
        //固定值
        stringBuilder.append("e53jfgRgd7Hk");
        String s = SecureUtil.md5((stringBuilder.toString()));
        System.out.println(s);
        return s;
    }
}
