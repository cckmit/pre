package com.xd.pre.modules.px.jddj.paytoken;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xd.pre.common.h5st.HMAC;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.common.utils.px.dto.UrlEntity;
import com.xd.pre.modules.px.jddj.cookie.JdDjCookie;
import com.xd.pre.modules.px.jddj.main.MainBody;
import com.xd.pre.modules.px.jddj.utils.JdDjSignUtils;
import com.xd.pre.modules.sys.domain.JdAppStoreConfig;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
@Component
@NoArgsConstructor
public class PayTokenPrefixCombine {

    private String encrypt;
    private MainBody mainBody;
    private PayTokenPrefixBody payTokenPrefixBody;
    private JdDjCookie jdDjCookie;
    private String orderId;
    private String payToken;
    private String payTokenUrl;
    private String tokenUrl;


    public PayTokenPrefixCombine(String encrypt, MainBody mainBody, PayTokenPrefixBody payTokenPrefixBody, JdDjCookie jdDjCookie, String orderId) {
        this.encrypt = encrypt;
        this.mainBody = mainBody;
        this.payTokenPrefixBody = payTokenPrefixBody;
        this.jdDjCookie = jdDjCookie;
        this.orderId = orderId;
    }

    /**
     * @param payTokenPrefixCombine 组合
     * @param client                代理
     * @return
     */
    public PayTokenPrefixCombine payTokenPrefixCombineRequst(PayTokenPrefixCombine payTokenPrefixCombine, OkHttpClient client,
                                                             Map<String, String> headerMap) {
        Response response = null;
        try {
            String url = "https://daojia.jd.com/client?functionId=tx/core/unifiedOrder";
    /*        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            if (ObjectUtil.isNotNull(proxy)) {
                log.debug("当前设置PayTokenPrefix,msg:{}", proxy.toString());
                builder.proxy(proxy);
            }
            OkHttpClient client = builder.followRedirects(false).build();*/
            RequestBody requestBody = new FormBody.Builder()
                    .add("functionId", "tx/core/unifiedOrder")
                    .add("djencrypt", URLDecoder.decode(payTokenPrefixCombine.encrypt))
                    .build();
            Request.Builder builder = new Request.Builder().url(url)
                    .post(requestBody)
                    .addHeader("Cookie", String.format("o2o_m_h5_sid=%s;", payTokenPrefixCombine.getJdDjCookie().getO2o_m_h5_sid()));

            if (CollUtil.isNotEmpty(headerMap)) {
                for (String key : headerMap.keySet()) {
                    builder.header(key, headerMap.get(key));
                }
            }
            response = client.newCall(builder.build()).execute();
            String unifiedOrderStr = response.body().string();
            log.info("当前获取token前缀结果为msg:{}", unifiedOrderStr);
            JSONObject parseObject = JSON.parseObject(unifiedOrderStr);
            if (response.code() == HttpStatus.HTTP_OK && parseObject.getBoolean("success")) {
                response.close();
                String payTokenUrl = parseObject.getString("result");
                payTokenPrefixCombine.setPayTokenUrl(payTokenUrl);
                UrlEntity result = PreUtils.parseUrl(parseObject.getString("result"));
                String payToken = result.getParams().get("payToken");
                payTokenPrefixCombine.setPayToken(payToken);
                return payTokenPrefixCombine;
            }
        } catch (Exception e) {
            log.error("当前获取token报错，msg:{}，e:{}", payTokenPrefixCombine, e.getMessage());
        } finally {
            if (ObjectUtil.isNotNull(response)) {
                response.close();
            }
        }
        log.error("获取token错误，请查看日志报错信息");
        return null;
    }

    /**
     * 签证
     *
     * @param jdDjCookie ck对象
     * @param orderId    订单编号
     * @return
     * @throws Exception
     */
    public static PayTokenPrefixCombine getDjencrypt(JdDjCookie jdDjCookie, String orderId, JdAppStoreConfig jdAppStoreConfig) throws Exception {
        PayTokenPrefixBody payTokenPrefixBody = new PayTokenPrefixBody(orderId, jdAppStoreConfig.getConfig());
        MainBody mainBody = new MainBody(jdDjCookie.getDeviceid_pdj_jd(), JSON.toJSONString(payTokenPrefixBody));
        HashMap hashMap = JSON.parseObject(JSON.toJSONString(mainBody), HashMap.class);
        hashMap.remove("signKeyV1");
        String sortStr = JdDjSignUtils.mapSortedByKey(hashMap);
        String hmacSHA256 = HMAC.HmacSHA(sortStr, JdDjSignUtils.H_MAC_HASH_KEY, "HmacSHA256");
        hashMap.put("signKeyV1", hmacSHA256);
        String encrypt = JdDjSignUtils.Encrypt(JSON.toJSONString(hashMap));
        PayTokenPrefixCombine payTokenPrefixCombine = new PayTokenPrefixCombine(encrypt, mainBody, payTokenPrefixBody, jdDjCookie, orderId);
        return payTokenPrefixCombine;
    }

}
