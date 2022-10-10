package com.xd.pre.modules.px.jddj.orderDelete;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xd.pre.common.h5st.HMAC;
import com.xd.pre.modules.px.jddj.cookie.JdDjCookie;
import com.xd.pre.modules.px.jddj.main.MainBody;
import com.xd.pre.modules.px.jddj.utils.JdDjSignUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.util.HashMap;

@Data
@Slf4j
@Component
@NoArgsConstructor
public class OrderDeleteCombine {
    private String encrypt;
    private MainBody mainBody;
    private OrderDeleteBody orderDeleteBody;
    private JdDjCookie jdDjCookie;

    public OrderDeleteCombine(String encrypt, MainBody mainBody, OrderDeleteBody orderDeleteBody, JdDjCookie jdDjCookie) {
        this.encrypt = encrypt;
        this.mainBody = mainBody;
        this.orderDeleteBody = orderDeleteBody;
        this.jdDjCookie = jdDjCookie;
    }

    /**
     * 签证
     *
     * @param jdDjCookie ck对象
     * @return
     * @throws Exception
     */
    public static OrderDeleteCombine getDjencrypt(JdDjCookie jdDjCookie, String orderId) throws Exception {
        OrderDeleteBody orderDeleteBody = new OrderDeleteBody(Long.valueOf(orderId));
        MainBody mainBody = new MainBody(jdDjCookie.getDeviceid_pdj_jd(), JSON.toJSONString(orderDeleteBody));
        HashMap hashMap = JSON.parseObject(JSON.toJSONString(mainBody), HashMap.class);
        hashMap.remove("signKeyV1");
        String sortStr = JdDjSignUtils.mapSortedByKey(hashMap);
        String hmacSHA256 = HMAC.HmacSHA(sortStr, JdDjSignUtils.H_MAC_HASH_KEY, "HmacSHA256");
        hashMap.put("signKeyV1", hmacSHA256);
        String encrypt = JdDjSignUtils.Encrypt(JSON.toJSONString(hashMap));
        OrderDeleteCombine orderDeleteCombine = new OrderDeleteCombine(encrypt, mainBody, orderDeleteBody, jdDjCookie);
        return orderDeleteCombine;
    }


    /**
     * @param orderDeleteCombine 组合
     * @param client             代理
     * @return
     */
    public OrderDeleteCombine orderDeleteCombineRequst(OrderDeleteCombine orderDeleteCombine, OkHttpClient client) {
        Response response = null;
        try {
            String url = "https://daojia.jd.com/client?functionId=order/orderDelete";
/*            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            if (ObjectUtil.isNotNull(proxy)) {
                log.debug("当前设置orderDeleteCombineRequst,msg:{}", proxy.toString());
                builder.proxy(proxy);
            }

            OkHttpClient client = builder.followRedirects(false).build();*/
            RequestBody requestBody = new FormBody.Builder()
                    .add("djencrypt", URLDecoder.decode(orderDeleteCombine.encrypt))
                    .add("functionId", "order/orderDelete")
                    .build();
            Request request = new Request.Builder().url(url).post(requestBody)
                    .addHeader("Cookie", String.format("o2o_m_h5_sid=%s;", orderDeleteCombine.getJdDjCookie().getO2o_m_h5_sid()))
                    .build();
            response = client.newCall(request).execute();
            String cancelStr = response.body().string();
            log.info("orderDeleteCombineRequst删除订单msg:{}", cancelStr);
            JSONObject parseObject = JSON.parseObject(cancelStr);
            if (response.code() == HttpStatus.HTTP_OK && parseObject.getBoolean("success")) {
                return orderDeleteCombine;
            }
            if (cancelStr.contains("您的订单已删除")) {
                return orderDeleteCombine;
            }
        } catch (Exception e) {
            log.error("删除订单错误，msg:{}，e:{}", orderDeleteCombine, e.getMessage());
        } finally {
            if (ObjectUtil.isNotNull(response)) {
                response.close();
            }
        }
        log.error("删除订单错误，请查看日志报错信息");
        return null;
    }
}
