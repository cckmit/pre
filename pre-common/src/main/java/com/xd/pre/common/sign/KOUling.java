package com.xd.pre.common.sign;


public class KOUling {


    public static void main(String[] args) {
        String body = "{\"appKey\":\"android\",\"brandId\":\"999440\",\"buyNum\":1,\"payMode\":\"0\",\"rechargeversion\":\"10.9\",\"skuId\":\"200148732995\",\"totalPrice\":\"10000\",\"type\":1,\"version\":\"1.10\"}";
        String jComExchange = JdSgin.getJdSgin("submitGPOrder", body);
        System.out.println(jComExchange);
    }

}
