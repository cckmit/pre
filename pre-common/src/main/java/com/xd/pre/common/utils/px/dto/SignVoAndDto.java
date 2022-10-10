package com.xd.pre.common.utils.px.dto;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignVoAndDto {
    private String functionId;
    private String body;
    private String androidOrIos = "android";
    private String clientVersion;


    private String uuid;

    public SignVoAndDto(String functionId, String body) {
        this.functionId = functionId;
        this.body = body;
    }

    public SignVoAndDto(String functionId, String body, String clientVersion, String uuid) {
        this.functionId = functionId;
        this.body = body;
        this.clientVersion = clientVersion;
        this.uuid = uuid;
    }

    private String st;
    private String sign;
    private String sv;

    public static void main(String[] args) {
        System.out.println(JSON.toJSONString("{\"action\":\"to\",\"to\":\"https%3A%2F%2Fcard.m.jd.com%2F\"}"));
        System.out.println("AAEAMBeMH-hIbuCdvyPaHsIBHDrxX1TeUFb9khdtZ9Ki0wkABfjpJ7ZmsjVdbzarr3C-qg0".length());
    }
}
