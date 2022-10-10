package com.xd.pre.modules.px.vo.reqvo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xd.pre.common.utils.px.PreUtils;
import com.google.common.base.Joiner;
import com.xd.pre.common.utils.px.dto.SignVoAndDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class TokenKeyVo {

    private String cookie;

    private SignVoAndDto signVoAndDto;
    private String d_brand = "HUAWEI";
    private String d_model = "ANE-TL00";
    private String screen = "2060*1080";
    private String partner = "ks006";
    //1
    private String osVersion = "9";
    //36
    private String oaid = "ffff1ss111-fb12-cf12-6f12-9b9ff2222d";
    //116
    private String eid = "eidAdb7f812185s31ORI2222TTa4g1yndktXXd22225a1k/0mFpn5FMsiKJlf32222ttzGtYFdERe1uis11lVgaLrjIya/4bG0pDePi4Ln6FZLAE0BK";
    //19
    private String area = "22_1920_39324_39298";
    //32
    private String wifiBssid = "d855fdc796a56c892ea38fa0508411ff";
    //216
    private String uts = "0f31TVRjBSsvdnAEBxceDO5NXnpDGlDaU59lra2HFHR1HLOW9RwbyqMkYOymeVsUT/qNeIqly9k7bux+c7Q+6O6oBx0hFWK+eEfmkgJ2Oa3scCQKjf83MEQYQRoCl376qCxMerkNljbOJysXGtvaWU1FVDzb8XdnEdLpBI+ng5ZznI+CBKYHnAFnU9euXy/Z5slpSY+7ij02VOoKfMUgdg==";

    /**
     * 处理st等参数
     */
    private String client;

    private String st;
    private String sign;
    private String sv;
    private String functionId;
    private String uuid;
    private String body;
    private String clientVersion;
    //PreUtils.getRandomString()
    public static String getUrlParamTokenKey(SignVoAndDto signVoAndDto) {
        TokenKeyVo build = TokenKeyVo.builder().d_brand(PreUtils.getRandomString(5))
                .d_model(PreUtils.getRandomString(8))
                .partner(PreUtils.getRandomString(5))
                .osVersion(new Random().nextInt(9) + "")
                .oaid(PreUtils.getRandomString(36))
                .eid(PreUtils.getRandomString(116))
                .area(PreUtils.getRandomString(19))
                .wifiBssid(PreUtils.getRandomString(32))
                .uts(PreUtils.getRandomString(216))
                .client(signVoAndDto.getAndroidOrIos())
                .st(signVoAndDto.getSt())
                .sign(signVoAndDto.getSign())
                .sv(signVoAndDto.getSv())
                .functionId(signVoAndDto.getFunctionId())
                .uuid(signVoAndDto.getUuid())
                .body(signVoAndDto.getBody())
                .clientVersion(signVoAndDto.getClientVersion())
                .build();
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(build));
        if (jsonObject.containsKey("body")) {
            jsonObject.remove("body");
        }
        if (jsonObject.containsKey("signVoAndDto")) {
            jsonObject.remove("signVoAndDto");
        }
        if (jsonObject.containsKey("Cookie")) {
            jsonObject.remove("Cookie");
        }
        String join = Joiner.on("&")
                // 用指定符号代替空值,key 或者value 为null都会被替换
                .useForNull("")
                .withKeyValueSeparator("=")
                .join(jsonObject);
        return join;
    }

    public static void main(String[] args) throws Exception {
        SignVoAndDto signVoAndDto = JSON.parseObject("{\"androidOrIos\":\"android\",\"body\":\"{\\\"action\\\":\\\"to\\\",\\\"to\\\":\\\"https%3A%2F%2Fcard.m.jd.com%2F\\\"}\",\"clientVersion\":\"9.8.4\",\"functionId\":\"genToken\",\"sign\":\"9c8e337b8e80e67432acbcebaf4ec2d5\",\"st\":\"1606701201628\",\"sv\":\"111\",\"uuid\":\"55bs162e53b926e1\"}\n"
                , SignVoAndDto.class);
        System.out.println(getUrlParamTokenKey(signVoAndDto));
//        String result2 = HttpRequest.post(url)
//                .header(Header.USER_AGENT, "Hutool http")//头信息，多个头信息多次调用此方法即可
//                .header(Header.)
////                .form(paramMap)//表单内容
//                .timeout(20000)//超时，毫秒
//                .execute().body();
    }

}
