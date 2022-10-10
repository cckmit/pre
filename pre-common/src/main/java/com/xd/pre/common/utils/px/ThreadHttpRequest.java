package com.xd.pre.common.utils.px;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
public class ThreadHttpRequest implements Callable<Integer> {

    private String url;
    private String skuId;
    private String tokenKey;
    private String skuPrice;

    public ThreadHttpRequest(String url, String skuId, String skuPrice, String tokenKey) {
        this.url = url;
        this.skuId = skuId;
        this.tokenKey = tokenKey;
        this.skuPrice = skuPrice;
    }

    @Override
    public Integer call() {
        try {
            log.info("----- 执行第二部tokenKey");
            JSON paramMap = new JSONObject();
            ((JSONObject) paramMap).put("tokenKey", tokenKey);
            ((JSONObject) paramMap).put("skuId", skuId);
            ((JSONObject) paramMap).put("skuPrice", skuPrice);
            String genTokenBody = HttpRequest.post(url)
                    .header(Header.USER_AGENT, "okhttp/3.12.1")
                    .body(paramMap)//表单内容
                    .timeout(5000)//超时，毫秒 2分钟
                    .execute().body();
        } catch (Exception e) {
            log.error("超时:msg:[]");
        }
        return 1;
    }

}
