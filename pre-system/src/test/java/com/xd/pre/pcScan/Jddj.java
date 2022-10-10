package com.xd.pre.pcScan;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.xd.pre.common.sign.JdSgin;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.common.utils.px.dto.SignVoAndDto;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

@Slf4j

public class Jddj {
    public static void main(String[] args)throws Exception {
        //当前获取token前缀结果为msg:{"code":"0","msg":"成功","result":"https://daojia.jd.com/html/index.html?payToken=x1uHEqb3DHWCtMHBFhO1sEJ1Evcj7eihj0Z2mQbCvcrVkiHt9f9SswEgSmOtajsdjygz3ofG59fcN7g02y0AHDIpG3M6lJRzPy4gMH6sDdNc+y8anLH2Gd3DyMLrt1OQP8kCk+7UXlCEPPE/lZxu0mK3gNnnFxZT/YjstHmzVrg=&from=daojia&paySource=142&h5hash=cashier#cashier","success":true}
        Map<String, String> ipAndPort = Connect.getIpAndPort();
        OkHttpClient client = Demo.getOkHttpClient(ipAndPort.get("ip"), Integer.valueOf(ipAndPort.get("port")));
        Map<String, String> headerMap = PreUtils.buildIpMap("223.104.251.19");
        geturl(headerMap,client,"https://daojia.jd.com/html/index.html?payToken=cEbaW1dyXmFhzij++W4lMwnCGz6UdqyFwPEbb7IAfshNxtbg9ah/N2R6aP2yt1OBy8Rm2+BDpykRvNLnwn3MIgG1yOuQLoxcc8/ZVNopiO0gTPzTGrkpDhC//j8J0ik/zikCuMfyd2sGSjoig5xQSUV4dBk/Yq+B1/lR9pRfOaw=&from=daojia&paySource=142&h5hash=cashier#cashier");

    }

    private static void geturl(Map<String, String> headerMap, OkHttpClient client, String payToken) throws IOException {
        String appck = "pin=jd_542a0da49a690;wskey=AAJivcVbAECXQyhcTW7IN04wVcP269_e5PgOaGNlRMQgHc3ksJw-ScebeVxeKycxjdZB_CzznS1EZmQUfeCCOVSDGTuxgA9Y;";
        String returnUrl = String.format("{\"returnLink\":\"%s\"}", payToken);
        String returnUrldecode = URLEncoder.encode(returnUrl);
        String decode = URLEncoder.encode("https://daojia.jd.com/client?functionId=login/passport&platCode=H5&appName=paidaojia&appVersion=7.2.0&body=" + returnUrldecode);
        String bodyData = String.format("{\"action\":\"to\",\"to\":\"%s\"}", decode);
        SignVoAndDto signVoAndDto = new SignVoAndDto("genToken", bodyData);
        signVoAndDto = JdSgin.newSign(signVoAndDto);
        String url = String.format("https://api.m.jd.com/client.action?functionId=genToken&clientVersion=9.4.4&client=android&uuid=%s&st=%s&sign=%s&sv=120", signVoAndDto.getUuid(),
                signVoAndDto.getSt(), signVoAndDto.getSign());

        RequestBody requestBody = new FormBody.Builder()
                .add("body", bodyData)
                .build();
        Request.Builder builder1 = new Request.Builder().url(url)
                .post(requestBody)
                .addHeader("Cookie", appck);
        setHeader(headerMap, builder1);
        log.debug("设置请求头");
        Response response = client.newCall(builder1.build()).execute();
        String resStr = response.body().string();
        String tokenKey = JSON.parseObject(resStr).getString("tokenKey");
        String t = "https://un.m.jd.com/cgi-bin/app/appjmp?tokenKey=" + tokenKey;
        log.info("跳转页面数据msg:{}", t);
        log.info("==================");
    }
    private static void setHeader(Map<String, String> headerMap, Request.Builder builder) {
        if (CollUtil.isNotEmpty(headerMap)) {
            for (String key : headerMap.keySet()) {
                builder.header(key, headerMap.get(key));
            }
        }
    }

}
