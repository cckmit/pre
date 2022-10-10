package com.xd.pre.modules.px.jddj.utils;

import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JdDjSignUtils {
    public static final String salt = "J@NcRfUjXn2r5u8x";
    public static final String viStr = "t7w!z%C*F-JaNdRg";
    public static final String H_MAC_HASH_KEY = "923047ae3f8d11d8b19aeb9f3d1bc200";


    public static String mapSortedByKey(Map<String, Object> param) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> keyList = new ArrayList<>(param.keySet());
        Collections.sort(keyList);
        for (int i = 0; i < keyList.size(); i++) {
            String key = keyList.get(i);
//            System.out.println(key);
//         stringBuilder.append(key).append("=").append(param.getOrDefault(key, ""));
            if (i == keyList.size() - 1) {
                stringBuilder.append(param.get(key) + "");
            } else {
                stringBuilder.append(param.get(key) + "&");
            }
        }
        return stringBuilder.toString();
    }

    //AES加解密算法
    public static String Encrypt(String sSrc) throws Exception {
        byte[] raw = salt.getBytes("utf-8");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//"算法/模式/补码方式"
        IvParameterSpec iv = new IvParameterSpec(viStr.getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes());
        return URLEncoder.encode(new BASE64Encoder().encode(encrypted));//此处使用BASE64做转码功能，同时能起到2次加密的作用。
    }
}
