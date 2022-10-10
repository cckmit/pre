package com.xd.pre.modules.px.jddj.submit;

import lombok.Data;

import java.util.Random;

@Data
public class SubmitBody {

    public SubmitBody(int amount) {
        this.amount = amount;
        this.payAmount = String.valueOf(amount);
        this.cardMoney = String.valueOf(amount);
    }

    /**
     * {
     * "amount": 10000,
     * "payAmount": "10000",
     * "cardMoney": "10000",
     * "imageId": "23",
     * "storeId": 11958521,
     * "activityId": "0",
     * "cardNum": 1,
     * "authorize": 0,
     * "fromSource": 2
     * }
     */
    /**
     * "gpsLongitude": 103.92377,
     * "gpsLatitude": 30.574175,
     * "riskTokenId": "",
     * "pageSource": "",
     * "ctp": "",
     * "refPar": ""
     */

    private int amount;
    private String payAmount;
    private String cardMoney;
    private String imageId = "23";
    private int storeId = 11958521;
    private String activityId = "0";
    private int cardNum = 1;
    private int authorize = 0;
    private int fromSource = 2;
    private Double gpsLongitude = 102.92377 + new Random().nextDouble()+new Random().nextDouble()*10;
    private Double gpsLatitude = 31.574175 + new Random().nextDouble()+new Random().nextDouble()*10;



//
//    private Double gpsLongitude = 103.92377;
//    private Double gpsLatitude = 30.574175;
//    private String riskTokenId = "";
//    private String pageSource = "";
//    private String ctp = "";
//    private String refPar = "";

}
