package com.xd.pre.modules.px.jddj.main;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.util.Date;
import java.util.Random;

@Data
public class MainBody {


    /**
     * {
     * "platCode": "H5",
     * "appName": "paidaojia",
     * "channel": "",
     * "appVersion": "8.20.0",
     * "body": "{\"paySource\":142,\"orderId\":\"2214261779000074\",\"successUrl\":\"https://daojia.jd.com/html/app/giftCard/giftCardBuySuc?orderId=2214261779000074\",\"openId\":\"\",\"appId\":1,\"source\":2,\"os\":\"OS01\",\"osVersion\":\"\",\"deviceType\":\"DT03\",\"payStageType\":\"\",\"pageSource\":\"\",\"ctp\":\"\",\"refPar\":\"\"}",
     * "pageId": "af2f0f5e2894cbfaf84411e65cf6d5a4",
     * "lng": 103.92377,
     * "lat": 30.574175,
     * "city_id": 1930,
     * "poi": "成都市双流区政府",
     * "jda": "122270672.16551708838371127154807.1655170883.1655191840.1655228139.3",
     * "traceId": "H5_DEV_B2D78A89-C9C4-4EC6-8F11-CE0B78B8E68A1655228179703",
     * "globalPlat": "2",
     * "deviceId": "H5_DEV_B2D78A89-C9C4-4EC6-8F11-CE0B78B8E68A",
     * "signNeedBody": 1,
     * "_jdrandom": 1655228179703,
     * "signKeyV1": "5de73bf4cd18947d605bfb0b2582c930e03c0308c6363be5f85c5eb1ac7e35f7"
     * }
     *
     * @param deviceId
     * @param body
     */
    public MainBody(String deviceId, String body/*, String poi, Integer city_id*/) {
        this.deviceId = deviceId;
        this.body = body;
       /* if (StrUtil.isBlank(poi)) {
            this.poi = "成都市双流区政府";
        } else {
            this.poi = poi;
        }
        if (ObjectUtil.isNotNull(city_id)) {
            this.city_id = city_id;
        } else {
            this.city_id = 1930;
        }*/
    }

    private Double lng = 102.92377 + new Random().nextDouble() + new Random().nextDouble() * 10;
    private Double lat = 31.574175 + new Random().nextDouble() + new Random().nextDouble() * 10;
    private Integer city_id = 1930;
    private String deviceId;
    private String body;
    private String poi;
    private String platCode = "H5";
    private String appName = "paidaojia";
    private String appVersion = "8.20.0";
    private String globalPlat = "2";
    private int signNeedBody = 1;
    private long _jdrandom = new Date().getTime();
    private String signKeyV1;

}
