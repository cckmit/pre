package com.xd.pre.modules.px.jddj.initCashier;

import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xd.pre.common.h5st.HMAC;
import com.xd.pre.modules.px.jddj.cookie.JdDjCookie;
import com.xd.pre.modules.px.jddj.main.MainBody;
import com.xd.pre.modules.px.jddj.paytoken.PayTokenPrefixCombine;
import com.xd.pre.modules.px.jddj.utils.JdDjSignUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.util.HashMap;

@Data
@Slf4j
@Component
@NoArgsConstructor
public class InitCashierCombine {

    private String encrypt;
    private MainBody mainBody;
    private InitCashierBody initCashierBody;
    private JdDjCookie jdDjCookie;
    private StringRedisTemplate redisTemplate;

    public InitCashierCombine(String encrypt, MainBody mainBody, InitCashierBody initCashierBody, JdDjCookie jdDjCookie) {
        this.encrypt = encrypt;
        this.mainBody = mainBody;
        this.initCashierBody = initCashierBody;
        this.jdDjCookie = jdDjCookie;
    }

    /**
     * 签证
     *
     * @param payTokenPrefixCombine ck对象
     * @return
     * @throws Exception
     */
    public  InitCashierCombine getDjencrypt(PayTokenPrefixCombine payTokenPrefixCombine) throws Exception {
        InitCashierBody initCashierBody = new InitCashierBody(payTokenPrefixCombine.getPayToken());
        MainBody mainBody = new MainBody(payTokenPrefixCombine.getJdDjCookie().getDeviceid_pdj_jd(), JSON.toJSONString(initCashierBody));
        mainBody.setSignNeedBody(0);
        HashMap hashMap = JSON.parseObject(JSON.toJSONString(mainBody), HashMap.class);
        hashMap.remove("signKeyV1");
        String sortStr = JdDjSignUtils.mapSortedByKey(hashMap);
        String hmacSHA256 = HMAC.HmacSHA(sortStr, JdDjSignUtils.H_MAC_HASH_KEY, "HmacSHA256");
        hashMap.put("signKeyV1", hmacSHA256);
        String encrypt = JdDjSignUtils.Encrypt(JSON.toJSONString(hashMap));
        InitCashierCombine initCashierCombine = new InitCashierCombine(encrypt, mainBody, initCashierBody, payTokenPrefixCombine.getJdDjCookie());
        return initCashierCombine;
    }

    /**
     * @param initCashierCombine 组合
     * @param client             代理
     * @return
     */
    public static InitCashierCombine InitCashierCombineRequst(InitCashierCombine initCashierCombine, OkHttpClient client) {
        try {
            String url = "https://daojia.jd.com/client?functionId=biz/initCashierPost";
     /*       OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            if (ObjectUtil.isNotNull(proxy)) {
                log.debug("当前设置payTokenPrefixCombineRequst,msg:{}", proxy.toString());
                builder.proxy(proxy);
            }
            OkHttpClient client = builder.followRedirects(false).build();*/

            RequestBody requestBody = new FormBody.Builder()
                    .add("functionId", "biz/initCashierPost")
                    .add("djencrypt", URLDecoder.decode(initCashierCombine.encrypt))
                    .build();

            Request request = new Request.Builder().url(url).post(requestBody)
                    .addHeader("Cookie", String.format("o2o_m_h5_sid=%s;", initCashierCombine.getJdDjCookie().getO2o_m_h5_sid()))
                    .build();
            log.debug("InitCashierCombineRequst当前组装请求成功");
            Response response = client.newCall(request).execute();
            String inidStr = response.body().string();
            log.info("InitCashierCombineRequst当前检查的结果为前缀结果为msg:{}", inidStr);
            JSONObject parseObject = JSON.parseObject(inidStr);
            if (response.code() == HttpStatus.HTTP_OK && parseObject.getBoolean("success")) {
                if (inidStr.contains("orderId")) {
                    return initCashierCombine;
                }
            }
        } catch (Exception e) {
            log.error("当前获取token报错，msg:{}，e:{}", initCashierCombine, e.getMessage());
        }
        log.error("获取token错误，请查看日志报错信息");
        return null;
    }


}
