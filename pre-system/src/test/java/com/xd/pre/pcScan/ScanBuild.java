package com.xd.pre.pcScan;

import cn.hutool.core.util.URLUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import com.xd.pre.common.sign.JdSgin;
import com.xd.pre.common.utils.px.dto.SignVoAndDto;

import java.util.HashMap;
import java.util.Map;

public class ScanBuild {
    public static void main(String[] args) {
        //cccd9946731386efa1b49becdf1ca9b33b54488f1340075c5fe75f989c52095e814c137e02705461292b9f990a87fa9533fc899a5d002b39fa378e67b3b56f833b9487beaf0dcae568f49411ab2e64b3
/*        String qr = "9caf83554eee14dd7a740ba2f0aedef0026eb45a688a82bc6442a10d5dfb2814f6baf7f41a40bb809c14093ce33290d6528030510d763102f9c073405f54ab98";
        String appjmpUrl = "https://pcashier.jd.com/image/virtualH5Pay?sign=%s";
        Map<String, Object> paramMap = new HashMap<>();
        String bodyData = String.format("{\"action\":\"to\",\"to\":\"%s\"}", URLUtil.encode(String.format(appjmpUrl, qr)));
        paramMap.put("body", bodyData);
        SignVoAndDto signVoAndDto = new SignVoAndDto("genToken", bodyData);
        signVoAndDto = JdSgin.newSign(signVoAndDto);
        String url = String.format("api.m.jd.com/client.action?functionId=genToken&clientVersion=9.4.4&client=android&uuid=%s&st=%s&sign=%s&sv=120",
                signVoAndDto.getUuid(), signVoAndDto.getSt(), signVoAndDto.getSign());
        String result2 = HttpRequest.post(url)
                .header(Header.USER_AGENT, "okhttp/3.12.1")
                .header(Header.COOKIE, "pin=tofadz4757;wskey=AAJiiST_AED5I1UaxmFPvm8Kpz8Co0DpMXIstakbg0OJnJWe-QcgrMksYUfhMOrm1MFr_3QrR3sdevT_T9Li_yFeRNMLF27r;")
                .form(paramMap)//表单内容
                .timeout(20000)//超时，毫秒
                .execute().body();
        System.out.println(result2);*/
    }
}
