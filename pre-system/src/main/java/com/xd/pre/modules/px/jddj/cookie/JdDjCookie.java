package com.xd.pre.modules.px.jddj.cookie;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.xd.pre.common.constant.PreConstant;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.modules.px.jddj.orderDelete.OrderDeleteCombine;
import com.xd.pre.modules.sys.mapper.JdCkZhidengMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
@Component
public class JdDjCookie {
    private String deviceid_pdj_jd;
    private String o2o_m_h5_sid;
    private String PDJ_H5_JDPIN;
    private String PDJ_H5_PIN;
    private String mck;
//    private String appCk;

    public static void main(String[] args) throws Exception {
        String a = "alipay_sdk=alipay-sdk-java-dynamicVersionNo&app_id=2021001196653833&biz_content=%7B%22body%22%3A%22AppStore%E5%85%85%E5%80%BC%E5%8D%A110%E5%85%83%E7%94%B5%E5%AD%90%E5%8D%A1-AppleID%E5%85%85%E5%80%BCiOS%E5%85%85%E5%80%BC%22%2C%22business_params%22%3A%22%7B%5C%22outTradeRiskInfo%5C%22%3A%5C%22%7B%7D%5C%22%7D%22%2C%22extend_params%22%3A%7B%22specified_seller_name%22%3A%22%22%7D%2C%22out_trade_no%22%3A%2212206230030140502713%22%2C%22product_code%22%3A%22QUICK_MSECURITY_PAY%22%2C%22seller_id%22%3A%222088931779499260%22%2C%22subject%22%3A%22%E6%8A%96%E9%9F%B3%E7%94%B5%E5%95%86-4947073743926410819%22%2C%22time_expire%22%3A%222022-06-23+05%3A00%3A02%22%2C%22total_amount%22%3A%2210.00%22%7D&charset=utf-8&format=json&method=alipay.trade.app.pay&notify_url=https%3A%2F%2Fapi-cpc.snssdk.com%2Fgateway%2Fpayment%2Fcallback%2Falipay%2Fnotify%2Fpay&sign=BFnXab4V1Qs8TUAZCofTW3%2B5OVYRHWxTEd2EHE5CnJdmHfY90Bs6uGiHHcLN18Z788Pnf8ax8b7%2BJqxc7FMk7Bf6URzumUQR9JH1%2BYw2ZlvxwJtEqRBSQJ09vf%2BOzJIKxR68zkoRdKfyrmwKfvEojDCLdirc86V2zCqMGW%2FTNfvgZxUnFsTGkEHLbL%2B%2BVP%2B0mI2oSD%2FSEb4TktqT1CBtQfKJScwcfrywwsfxvkXwCC%2FQVej8f%2BJdDRJwHukmwKsq16DdSgJ7NZxIuACNhDc1roB7rRUB2ob0oBgYlU%2BV1LvVrCz56Q78j%2Bvs4iRbNPPSmAEe3GCl7tGUPnny0WHsGg%3D%3D&sign_type=RSA2&timestamp=2022-06-23+04%3A30%3A03&version=1.0";
        System.out.println(a);
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        OkHttpClient client = builder.connectTimeout(2, TimeUnit.SECONDS).readTimeout(2, TimeUnit.SECONDS).followRedirects(false).build();
        JdDjCookie jdDjCookieSpring = new JdDjCookie();
        JdDjCookie jdDjCookie = jdDjCookieSpring.jdDjCookieBuild("pt_key=AAJiseISADAFRRptUl4i5zsj1m10PDSNkWjeBLU1FUXmRqzcmDI6u2LasNfLeeeOELp_9t7po0g; pt_pin=jd_bh59nw3vJtmI;",
                client, null, null,null);
//        PayTokenPrefixCombine payTokenPrefixCombine = PayTokenPrefixCombine.getDjencrypt(jdDjCookie, "2214910275000083");
//        payTokenPrefixCombine = payTokenPrefixCombine.payTokenPrefixCombineRequst(payTokenPrefixCombine, client);
//        System.out.println(payTokenPrefixCombine);
        OrderDeleteCombine orderDeleteCombine = OrderDeleteCombine.getDjencrypt(jdDjCookie, "2214910275000083");
        OrderDeleteCombine orderDeleteCombine1 = orderDeleteCombine.orderDeleteCombineRequst(orderDeleteCombine, client);
        System.out.println(JSON.toJSONString(orderDeleteCombine1));
    }


    public String getJdDjCookie() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("deviceid_pdj_jd=" + this.deviceid_pdj_jd + ";");
        stringBuilder.append("o2o_m_h5_sid=" + this.o2o_m_h5_sid + ";");
        stringBuilder.append("PDJ_H5_JDPIN=" + this.PDJ_H5_JDPIN + ";");
        stringBuilder.append("PDJ_H5_PIN=" + this.PDJ_H5_PIN + ";");
        if (StrUtil.isBlank(this.o2o_m_h5_sid) || StrUtil.isBlank(this.getDeviceid_pdj_jd())) {
            return null;
        }
        return stringBuilder.toString();
    }

    public JdDjCookie jdDjCookieBuild(String mck, OkHttpClient client, StringRedisTemplate redisTemplate, JdCkZhidengMapper jdCkZhidengMapper,
                                      Map<String, String> headerMap) {
        Response response = null;
        try {
            JdDjCookie jdDjCookie = new JdDjCookie();
            jdDjCookie.setMck(mck);
            String h5set = null;
            if (mck.contains("app_openAA")) {
                h5set = "https://daojia.jd.com/client?functionId=login/passport&platCode=H5&appName=paidaojia&appVersion=7.2.0&body={\"returnLink\":\"https://daojia.jd.com/html/index/giftCardBuy\"}";
            } else {
                log.debug("mAA端授权");
                h5set = "https://daojia.jd.com/client?functionId=login/passport&platCode=H5&appName=paidaojia&appVersion=8.22.0&body=%7B%22returnLink%22%3A%20%22https%3A%2F%2Fdaojia.jd.com%2Fhtml%2Findex%2Fuser%3Fchannel%3Djdapp%22%7D";
            }
//            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
//            if (ObjectUtil.isNotNull(proxy)) {
//                log.debug("设置获取根据mck获取jddj的ck代理设置:{}", proxy.toString());
//                builder.proxy(proxy);
//            }
//            OkHttpClient client = builder.followRedirects(false).build();
            Request.Builder builder = new Request.Builder().url(h5set)
                    .get()
                    .addHeader("Cookie", mck);
            if(CollUtil.isNotEmpty(headerMap)){
                for (String key : headerMap.keySet()) {
                    builder.header(key, headerMap.get(key));
                }
            }
            response = client.newCall(builder.build()).execute();
            int code = response.code();
            if (code == org.springframework.http.HttpStatus.FOUND.value()) {
                //https://daojia.jd.com/html/index.html#loginError/code:LG0103/returnLink:https%3A%2F%2Fdaojia.jd.com%2Fhtml%2Findex%2FgiftCardBuy
                List<String> headers = response.headers("Set-Cookie");
                Boolean aBoolean = jdDjCookie.pasreHeadList(headers);
                if (!aBoolean) {
                    String pt_pin = PreUtils.get_pt_pin(mck);
                    jdCkZhidengMapper.deleteByPtPin(pt_pin);
                    return null;
                }
            }
            String mckJdDj = jdDjCookie.getJdDjCookie();
            if (StrUtil.isBlank(mckJdDj)) {
                log.error("当前获取失败");
                return null;
            }
            return jdDjCookie;
        } catch (Exception e) {
            if (StrUtil.isNotBlank(e.getMessage()) && (e.getMessage().contains("Failed to connect to /") || e.getMessage().contains("connect timed out"))) {
                if (ObjectUtil.isNotNull(redisTemplate)) {
                    String msg = e.getMessage().replace("Failed to connect to /", "");
                    redisTemplate.delete(PreConstant.直连IP + msg.split(":")[0]);
                }
            }
            log.error("当前订单获取token报错msg:{}", e.getMessage());
        } finally {
            if (ObjectUtil.isNotNull(response)) {
                response.close();
            }
        }
        return null;
    }

    /**
     * 0 = "deviceid_pdj_jd=H5_DEV_2F6F7BEE-EA45-4C3E-BF97-96A19F57E562; Version=1; Max-Age=7776000; Expires=Fri, 09-Sep-2022 16:01:40 GMT; Path=/"
     * 1 = "h5_coords_obj=""; Version=1; Max-Age=7776000; Expires=Fri, 09-Sep-2022 16:01:40 GMT; Path=/"
     * 2 = "o2o_m_h5_sid=0182f6d8-52da-48d2-b353-938e8343399f; Version=1; Max-Age=7776000; Expires=Fri, 09-Sep-2022 16:01:40 GMT; Path=/"
     * 3 = "PDJ_H5_JDPIN=jd_AJfWANv; Version=1; Max-Age=7776000; Expires=Fri, 09-Sep-2022 16:01:40 GMT; Path=/"
     * 4 = "PDJ_H5_PIN=JD_dab8d2220d67000; Version=1; Max-Age=7776000; Expires=Fri, 09-Sep-2022 16:01:40 GMT; Path=/"
     */
    public Boolean pasreHeadList(List<String> headers) {
        if (CollUtil.isEmpty(headers)) {
            return false;
        }
        for (String header : headers) {
            setHead(header, "deviceid_pdj_jd");
            setHead(header, "o2o_m_h5_sid");
            setHead(header, "PDJ_H5_JDPIN");
            setHead(header, "PDJ_H5_PIN");
        }
        if (StrUtil.isNotBlank(this.o2o_m_h5_sid) && StrUtil.isNotBlank(this.getDeviceid_pdj_jd())) {
            return true;
        }
        return false;
    }

    private void setHead(String header, String headName) {
        if (StrUtil.isNotBlank(header) && header.contains(headName)) {
            String[] splits = header.split(";");
            if (splits.length >= 1) {
                for (String splitOne : splits) {
                    if (StrUtil.isNotBlank(splitOne) && splitOne.contains(headName)) {
                        String value = splitOne.split("=")[1];
                        if (headName.equals("deviceid_pdj_jd")) {
                            this.deviceid_pdj_jd = value;
                        }
                        if (headName.equals("o2o_m_h5_sid")) {
                            this.o2o_m_h5_sid = value;
                        }
                        if (headName.equals("PDJ_H5_JDPIN")) {
                            this.PDJ_H5_JDPIN = value;
                        }
                        if (headName.equals("PDJ_H5_PIN")) {
                            this.PDJ_H5_PIN = value;
                        }
                    }
                }
            }
        }
    }

}
