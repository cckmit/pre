package com.xd.pre.modules.px.weipinhui;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xd.pre.common.utils.px.PreUtils;

import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApiSign {
    public static void main(String[] args) {
        JSONObject parseObject = JSON.parseObject("{\n" +
                "    \"app_name\": \"shop_wap\",\n" +
                "    \"app_version\": \"4.0\",\n" +
                "    \"api_key\": \"8cec5243ade04ed3a02c5972bcda0d3f\",\n" +
                "    \"mobile_platform\": \"2\",\n" +
                "    \"source_app\": \"yd_wap\",\n" +
                "    \"warehouse\": \"VIP_CD\",\n" +
                "    \"fdc_area_id\": \"105101101\",\n" +
                "    \"province_id\": \"105101\",\n" +
                "    \"mars_cid\": \"1658589393300_1d2da58767953dc0e149a93513499ef6\",\n" +
                "    \"mobile_channel\": \"mobiles-||\",\n" +
                "    \"standby_id\": \"nature\",\n" +
                "    \"union_mark\": \"mobiles-nature||\",\n" +
                "    \"productIds\": \"6919473850979693192,6919701488950461064,6919907273577961928,6919805800928474766,6919873654157521544,6919656617938359314,6919581227153234056,6919472555080803090,6919863530261992072,6919553427972827598,6919862177356657288,6919886990885356808,6919533377896487624,6919454763017034825,6919378011105216094,6919386875479365390,6919702951192470794,6919835967397681616,6919419955480150600,6919533377879685832\",\n" +
                "    \"scene\": \"brand\",\n" +
                "    \"extParams\": \"{\\\"preheatTipsVer\\\":\\\"3\\\",\\\"subjectId\\\":\\\"101340388\\\",\\\"brandId\\\":\\\"101340388\\\",\\\"couponVer\\\":\\\"v2\\\",\\\"ic2label\\\":\\\"1\\\",\\\"multiBrandStore\\\":\\\"1\\\",\\\"exclusivePrice\\\":\\\"1\\\",\\\"iconSpec\\\":\\\"3x\\\",\\\"wxk\\\":\\\"0\\\"}\",\n" +
                "    \"context\": \"{\\\"615\\\":\\\"0\\\",\\\"872\\\":\\\"0\\\"}\",\n" +
                "    \"priceScene\": \"future\",\n" +
                "    \"_\": 1658740061\n" +
                "}");
        String s = hashParam(parseObject, "https://www.xx/vips-mobile/rest/shopping/wap2/product/module/list/v2");
        System.out.println(s);
        String s1 = replaceHost("https://mlogin-api.vip.com/xxx/ajaxapi/user/ticketLogin?_=1658723364932");
        System.out.println(s1);
        String sha1 = getSha1("/vips-mobile/rest/shopping/wap2/product/module/list/v2a6a26e0480091029760af27da51cf806901e487a1658589393300_1d2da58767953dc0e149a93513499ef68cae1f2c2dc97ed008ab280464a05f54d97f3d38280d4c64a55c48a8f589907a");
        System.out.println(sha1);

    }

    public static String getSecret(){
        return "d97f3d38280d4c64a55c48a8f589907a";
    }
    public static String replaceHost(String url) {
        String baseUrl = PreUtils.parseUrl(url).getBaseUrl();
        baseUrl = baseUrl.replace("https://", "");
        int i = baseUrl.indexOf("/");
        baseUrl =baseUrl.substring(i);
        return baseUrl;
    }

    public static String hashParam(JSONObject jsonparam,String url){
        Map<String, String> urlParams = PreUtils.parseUrl(url).getParams();
        if(CollUtil.isNotEmpty(urlParams)){
            for (String urlParamKey : urlParams.keySet()) {
                jsonparam.put(urlParamKey,urlParams.get(urlParamKey));
            }
        }
        List<String> keysortd = jsonparam.keySet().stream().sorted().collect(Collectors.toList());
        StringBuilder sb  = new StringBuilder();
        for (String key : keysortd) {
            if(key.equals("api_key")){
                continue;
            }
            sb.append(key+"="+jsonparam.getString(key)+"&");
        }
        String s = sb.toString();
        return getSha1(s.substring(0,s.length()-1));
    }
    public static String getSha1(String str) {

        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f' };
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
            mdTemp.update(str.getBytes("UTF-8"));
            byte[] md = mdTemp.digest();
            int j = md.length;
            char buf[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
                buf[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(buf);
        } catch (Exception e) {
            return null;
        }
    }

}
