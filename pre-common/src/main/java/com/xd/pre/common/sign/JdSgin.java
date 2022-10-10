package com.xd.pre.common.sign;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.lang.UUID;
import cn.hutool.crypto.SecureUtil;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.common.utils.px.dto.SignVoAndDto;
import com.xd.pre.common.utils.px.dto.UrlEntity;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JdSgin {

    public static final char[] SALT = new char[]{'8', '0', '3', '0', '6', 'f', '4', '3', '7', '0', 'b', '3', '9', 'f', 'd', '5', '6', '3', '0', 'a', 'd', '0', '5', '2', '9', 'f', '7', '7', 'a', 'd', 'b', '6'};
    public static final int[] SUANHAO = new int[]{55, 146, 68, 104, 165, 61, 204, 127, 187, 15, 217, 136, 238, 154, 233, 90};
    public static final String UUIDSTR = UUID.fastUUID().toString().replace("-", "").substring(0, 16);
    public static final String CLIENT_VERSION = "9.4.4";
    public static final String CLIENT = "android";


    /**
     * 口令加密
     *
     * @param str
     * @return
     */
    public static String ase(String str) {
        try {
            byte[] raw = "5yKhoqodQjuHGlKZ".getBytes(StandardCharsets.UTF_8);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//"算法/模式/补码方式"
            IvParameterSpec iv = new IvParameterSpec("7WwXmH2TKSCIEJQ3".getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(str.getBytes());
            return URLEncoder.encode(Base64.encode(encrypted), "utf-8");//此处使用BASE64做转码功能，同时能起到2次加密的作用。
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 京东加密计算
     *
     * @param functionId 函数
     * @param body       body
     */
    public static String getJdSgin(String functionId, String body) {
        String st = System.currentTimeMillis() + "";
        String signData = "functionId=" + functionId + "&body=" + body + "&uuid=" + UUIDSTR + "&client=" + CLIENT + "&clientVersion=" + CLIENT_VERSION + "&st=" + st + "&sv=120";
        List<Byte> thumbList = new ArrayList<>();
//        System.out.println(signData);
        char[] arg2 = signData.toCharArray();
        for (int i = 0; i < arg2.length; i++) {
            byte b = Integer.valueOf(((SUANHAO[i & 15] ^ (((SUANHAO[i & 15] ^ arg2[i]) ^ ((int) (SALT[i & 7]))) + SUANHAO[i & 15])) ^ (int) (SALT[i & 7])) & 0xff).byteValue();
            thumbList.add(b);
        }
        byte[] bytes = new byte[thumbList.size()];
        for (int i = 0; i < thumbList.size(); i++) {
            bytes[i] = thumbList.get(i);
        }
        String sign = SecureUtil.md5(Base64.encode(bytes));
 /*       String url = "https://api.m.jd.com/client.action?functionId=" + functionId;
        String mbody = "&clientVersion=" + CLIENT_VERSION + "&build=88136&client=" + CLIENT +
                "&&d_brand=apple&d_model=iPhone12,1&osVersion=14.1.2&screen=2392*1440&uuid=" + UUIDSTR + "&aid=" + UUIDSTR + "&networkType=wifi" +
                "&wifiBssid=b2ff0b2b441458771cec8df33057db68&st=" + st + "&sign=" + sign + "&sv=120" + "&body=" + URLUtil.encode(body);
        String result = HttpRequest.post(url)
                .header("charset", "UTF-8")
                .header("User-Agent", "JD4iPhone/167945%20(iPhone;%20iOS;%20Scale/2.00)")
                .header("content-type", "application/x-www-form-urlencoded")
                .header("cookie", cookies).body(mbody)
                .execute().body();*/
        return signData + "&sign=" + sign;
    }

    public static void main(String[] args) {
        String genAppPayId = getJdSgin("genToken", "{\"action\":\"to\",\"to\":\"https%3A%2F%2Fmpay.m.jd.com%2Fmpay.1ec8f70280318a51e2bb.html%3FappId%3Dd_m_mdbang%26payId%3Dd88cb94aa86b47e683a2948e78d4a023\"}");
        System.out.println(genAppPayId);
    }

    public static SignVoAndDto newSign(SignVoAndDto signVoAndDto) {
        String jdSgin = getJdSgin(signVoAndDto.getFunctionId(), signVoAndDto.getBody());
        UrlEntity urlEntity = PreUtils.parseUrl("https://www.baidu.com?" + jdSgin);
        Map<String, String> params = urlEntity.getParams();
        String st = params.get("st");
        String sv = params.get("sv");
        String sign = params.get("sign");
        String uuid = params.get("uuid");
        String clientVersion = params.get("clientVersion");
        signVoAndDto.setSv(sv);
        signVoAndDto.setSign(sign);
        signVoAndDto.setSt(st);
        signVoAndDto.setUuid(uuid);
        //android
        signVoAndDto.setAndroidOrIos(CLIENT);
        signVoAndDto.setClientVersion(clientVersion);
        return signVoAndDto;
    }


}
