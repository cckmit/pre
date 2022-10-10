package com.xd.pre.modules.px.jddj.submit;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xd.pre.common.constant.PreConstant;
import com.xd.pre.common.h5st.HMAC;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.modules.px.jddj.cookie.JdDjCookie;
import com.xd.pre.modules.px.jddj.main.MainBody;
import com.xd.pre.modules.px.jddj.utils.JdDjSignUtils;
import com.xd.pre.modules.sys.domain.JdCkZhideng;
import com.xd.pre.modules.sys.mapper.JdCkZhidengMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
@Component
@NoArgsConstructor
public class SubmitCombine {


    private String encrypt;
    private SubmitBody submitBody;
    private MainBody mainBody;
    private JdDjCookie jdDjCookie;
    private String orderId;
    private Integer isAccountCode=PreConstant.ONE;


    public SubmitCombine(String encrypt, SubmitBody submitBody, MainBody mainBody, JdDjCookie jdDjCookie) {
        this.encrypt = encrypt;
        this.submitBody = submitBody;
        this.mainBody = mainBody;
        this.jdDjCookie = jdDjCookie;
    }

    public static SubmitCombine getDjencrypt(JdDjCookie jdDjCookie, Integer amount) throws Exception {
        SubmitBody submitBody = new SubmitBody(amount);
        MainBody submitOrder = new MainBody(jdDjCookie.getDeviceid_pdj_jd(), JSON.toJSONString(submitBody));
        HashMap hashMap = JSON.parseObject(JSON.toJSONString(submitOrder), HashMap.class);
        hashMap.remove("signKeyV1");
        String sortStr = JdDjSignUtils.mapSortedByKey(hashMap);
        String hmacSHA256 = HMAC.HmacSHA(sortStr, JdDjSignUtils.H_MAC_HASH_KEY, "HmacSHA256");
        hashMap.put("signKeyV1", hmacSHA256);
        String encrypt = JdDjSignUtils.Encrypt(JSON.toJSONString(hashMap));
        SubmitCombine submitCombine = new SubmitCombine(encrypt, submitBody, submitOrder, jdDjCookie);
        return submitCombine;
    }


    public SubmitCombine submitOrderRequst(SubmitCombine submitCombine, OkHttpClient client, JdCkZhidengMapper zhidengMapper, Map<String, String> headerMap) {
        Response response = null;
        try {
            String url = String.format("https://daojia.jd.com/client?functionId=giftCardApp/submitOrder&djencrypt=%s", submitCombine.getEncrypt());
/*            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            if (ObjectUtil.isNotNull(proxy)) {
                log.debug("当前下单设置代理msg:{}", proxy.toString());
                builder.proxy(proxy);
            }
            OkHttpClient client = builder.followRedirects(false).build();*/
            Request.Builder builder = new Request.Builder().url(url)
                    .get()
                    .addHeader("Cookie", String.format("o2o_m_h5_sid=%s;", submitCombine.getJdDjCookie().getO2o_m_h5_sid()));
            if(CollUtil.isNotEmpty(headerMap)){
                for (String key : headerMap.keySet()) {
                    builder.header(key, headerMap.get(key));
                }
            }
            response = client.newCall(builder.build()).execute();
            String orderStr = response.body().string();
            log.info("下单结果msg:{}", orderStr);
            if(orderStr.contains("为了您的账号安全，请重新登录")){
                submitCombine.setIsAccountCode(PreConstant.ZERO);
                return submitCombine;
            }
            JSONObject parseObject = JSON.parseObject(orderStr);
            JdCkZhideng jdCkZhideng = zhidengMapper.selectOne(Wrappers.<JdCkZhideng>lambdaQuery().eq(JdCkZhideng::getPtPin, PreUtils.get_pt_pin(submitCombine.getJdDjCookie().getMck())));
            if (ObjectUtil.isNotNull(jdCkZhideng)) {
                jdCkZhideng.setFailReason(orderStr);
                zhidengMapper.updateById(jdCkZhideng);
                log.info("下单原因，不管啥原因。我这边都记录一下");
            }
            if (response.code() == HttpStatus.HTTP_OK && parseObject.getBoolean("success")) {
                submitCombine.setOrderId(parseObject.getString("result"));
                return submitCombine;
            }
            if (orderStr.contains("前方系统拥堵，给您带来的不便敬请")) {
                if (ObjectUtil.isNotNull(jdCkZhideng)) {
                    log.info("如果这个账号出现了订单。不管满不满，我这边都生成核销码");
                    jdCkZhideng.setIsEnable(PreConstant.FUYI_1);
                    zhidengMapper.updateById(jdCkZhideng);
                    return null;
                }
                log.info("当前账号出现了前方系统拥堵，给您带来的不便敬请msg:{}", submitCombine.getJdDjCookie().getMck());
            }
        } catch (Exception e) {
            log.error("当前下单报错，msg:{}，e:{}", submitCombine, e.getMessage());
        } finally {
            if (ObjectUtil.isNotNull(response)) {
                response.close();
            }
        }

        log.error("下单错误，请查看日志报错信息");
        return null;
    }

}
