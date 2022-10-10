package com.xd.pre.common.utils.px.dto;

import lombok.Data;

import java.util.Map;

@Data
public class UrlEntity {
    /**
     * 基础url
     */
    public String baseUrl;
    /**
     * url参数
     */
    public Map<String, String> params;

    public String getParamStr() {
        StringBuilder param = new StringBuilder();
        for (String key : params.keySet()) {
            param.append(key + "=" + params.get(key) + "&");
        }
        return param.toString().substring(0,param.length()-1);
    }
}
