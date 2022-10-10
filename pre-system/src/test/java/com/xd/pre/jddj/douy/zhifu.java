package com.xd.pre.jddj.douy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.net.URLEncoder;

public class zhifu {
    public static void main(String[] args) {
        String bodyRes = "{\"st\":0,\"msg\":\"\",\"data\":{\"data\":{\"is_queued\":false,\"sdk_info\":{\"appid\":\"wx76fdd06dde311af3\",\"noncestr\":\"ovBhXCUc8xfktgx2M9DFlzRtayTgTxpy\",\"package\":\"Sign=WXPay\",\"partnerid\":\"1588790401\",\"prepayid\":\"up_wx29210007205651f7924f60958a289e0000\",\"sign\":\"XfrvYphe4aIoWC0x0d//70pB99rPNyJGMZYne3G5zgiZiji7Haj7Jv/szfDkX1zBpDkBfevu1v6+XQUXkeEkKqMcBUNcJgZ5hmgcJVbxIeyV8t8AW1rR85xgICnNWltPgandBCFz6S4tMRd/BWGuhsWDKSTwmPi2ZH55Q1rWUdJLhMYWIcmj4GdSyWK/x29OF/XgKJz5elTYT+O9q8jhjyU/yjy3yjrV742lhTfy3yQGvR8b6iwJu+edWz2UbsX49mDuDGB54JB8SAr/IyZ5JT+aRiD3Tid/XRuVaZ80vR4fI3U9ASeYULzk0pcXKrazTg1eYjN82RVAVFdz0IXJzg==\",\"timestamp\":\"1664456407\"},\"trade_info\":{\"tt_sign\":\"rUfyBg5/y3NYsR96GTKFHTe1eqAI/zkRY0lEY91WnkrR/5ZZuF0SLnFpi1qm9Ob4cNjSi/wgaUnTegI+07xH8r5FZdeNOfhKnBDTm0UuL79T3DPkwpOOMsgVxbp9RrYc6yI8G5IuEtBGk36Bhz4udkhgVj5L7V4NOYZwVVRpWc0=\",\"tt_sign_type\":\"RSA\",\"way\":\"1\"}},\"message\":\"success\"},\"log_pb\":{\"impr_id\":\"202209292100060101501300482001AED6\",\"env\":\"prod\"}}";
        String sdk_info = JSON.parseObject(JSON.parseObject(JSON.parseObject(bodyRes).getString("data")).getString("data")).getString("sdk_info");
        JSONObject parseObject = JSON.parseObject(sdk_info);
        String weixinPay = String.format("weixin://app/%s/pay/?timeStamp=%s&partnerId=1588790401&prepayId=%s&nonceStr=%s&sign=%s",
                parseObject.getString("appid"), parseObject.getString("timestamp")
                , parseObject.getString("prepayid"), parseObject.getString("noncestr"), URLEncoder.encode(parseObject.getString("sign")) + "&package=Sign%3dWXPay");
        System.out.println(weixinPay);
    }
}
