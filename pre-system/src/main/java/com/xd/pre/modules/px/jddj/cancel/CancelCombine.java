package com.xd.pre.modules.px.jddj.cancel;

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
public class CancelCombine {
    private String encrypt;
    private MainBody mainBody;
    private CancelBody cancelBody;
    private JdDjCookie jdDjCookie;

    public CancelCombine(String encrypt, MainBody mainBody, CancelBody cancelBody, JdDjCookie jdDjCookie) {
        this.encrypt = encrypt;
        this.mainBody = mainBody;
        this.cancelBody = cancelBody;
        this.jdDjCookie = jdDjCookie;
    }

    /**
     * 签证
     *
     * @param jdDjCookie ck对象
     * @return
     * @throws Exception
     */
    public static CancelCombine getDjencrypt(/*PayCombine payCombine,JdDjCookie */ JdDjCookie jdDjCookie, String orderId) throws Exception {
        CancelBody cancelBody = new CancelBody(orderId);
        MainBody mainBody = new MainBody(jdDjCookie.getDeviceid_pdj_jd(), JSON.toJSONString(cancelBody));
        HashMap hashMap = JSON.parseObject(JSON.toJSONString(mainBody), HashMap.class);
        hashMap.remove("signKeyV1");
        String sortStr = JdDjSignUtils.mapSortedByKey(hashMap);
        String hmacSHA256 = HMAC.HmacSHA(sortStr, JdDjSignUtils.H_MAC_HASH_KEY, "HmacSHA256");
        hashMap.put("signKeyV1", hmacSHA256);
        String encrypt = JdDjSignUtils.Encrypt(JSON.toJSONString(hashMap));
        CancelCombine cancelCombine = new CancelCombine(encrypt, mainBody, cancelBody, jdDjCookie);
        return cancelCombine;
    }

    /**
     * @param cancelCombine 组合
     * @param client        代理
     * @return
     */
    public CancelCombine cancelCombineRequst(CancelCombine cancelCombine, OkHttpClient client) {
        try {
            String url = "https://daojia.jd.com/client?functionId=order/orderCancel";
      /*      OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            if (ObjectUtil.isNotNull(proxy)) {
                log.debug("当前设置cancelCombineRequst,msg:{}", proxy.toString());
                builder.proxy(proxy);
            }
            OkHttpClient client = builder.followRedirects(false).build();*/

            RequestBody requestBody = new FormBody.Builder()
                    .add("djencrypt", URLDecoder.decode(cancelCombine.encrypt))
                    .add("functionId", "order/orderCancel")
                    .build();
            Request request = new Request.Builder().url(url).post(requestBody)
                    .addHeader("Cookie", String.format("o2o_m_h5_sid=%s;", cancelCombine.getJdDjCookie().getO2o_m_h5_sid()))
                    .build();
            log.debug("cancelCombineRequst当前组装请求成功");
            Response response = client.newCall(request).execute();
            String cancelStr = response.body().string();
            log.info("cancelCombineRequst取消订单msg:{}", cancelStr);
            JSONObject parseObject = JSON.parseObject(cancelStr);
            int code = response.code();
            response.close();
            if (code == HttpStatus.HTTP_OK && parseObject.getBoolean("success")) {
                return cancelCombine;
            }
            if (cancelStr.contains("订单状态有更新，请刷新后重试")) {
                return cancelCombine;
            }
        } catch (Exception e) {
            log.error("取消订单，msg:{}，e:{}", cancelCombine, e.getMessage());
        }
        log.error("取消订单错误，请查看日志报错信息");
        return null;
    }
}
