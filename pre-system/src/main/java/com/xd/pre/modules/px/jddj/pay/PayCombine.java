package com.xd.pre.modules.px.jddj.pay;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xd.pre.common.constant.PreConstant;
import com.xd.pre.common.h5st.HMAC;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.modules.px.jddj.cookie.JdDjCookie;
import com.xd.pre.modules.px.jddj.main.MainBody;
import com.xd.pre.modules.px.jddj.paytoken.PayTokenPrefixCombine;
import com.xd.pre.modules.px.jddj.utils.JdDjSignUtils;
import com.xd.pre.modules.px.service.ProxyProductService;
import com.xd.pre.modules.sys.domain.JdProxyIpPort;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Data
@Component
@NoArgsConstructor
public class PayCombine {
    private String encrypt;
    private MainBody mainBody;
    private PayBody payBody;
    private JdDjCookie jdDjCookie;
    /**
     * 获取得到
     */
    private PayData payData;
    private String orderId;

    public PayCombine(String encrypt, MainBody mainBody, PayBody payBody, JdDjCookie jdDjCookie) {
        this.encrypt = encrypt;
        this.mainBody = mainBody;
        this.payBody = payBody;
        this.jdDjCookie = jdDjCookie;
    }

    /**
     * token自带的
     *
     * @param payTokenPrefixCombine
     * @return
     * @throws Exception
     */
    public static PayCombine getDjencrypt(PayTokenPrefixCombine payTokenPrefixCombine) throws Exception {
        PayBody payBody = new PayBody(payTokenPrefixCombine.getPayToken(), Long.valueOf(payTokenPrefixCombine.getOrderId()));
        MainBody mainBody = new MainBody(payTokenPrefixCombine.getJdDjCookie().getDeviceid_pdj_jd(), JSON.toJSONString(payBody));
        //好像这里需要添加为0
        mainBody.setSignNeedBody(0);
        HashMap hashMap = JSON.parseObject(JSON.toJSONString(mainBody), HashMap.class);
        hashMap.remove("signKeyV1");
        String sortStr = JdDjSignUtils.mapSortedByKey(hashMap);
        String hmacSHA256 = HMAC.HmacSHA(sortStr, JdDjSignUtils.H_MAC_HASH_KEY, "HmacSHA256");
        hashMap.put("signKeyV1", hmacSHA256);
        String encrypt = JdDjSignUtils.Encrypt(JSON.toJSONString(hashMap));
        PayCombine payCombine = new PayCombine(encrypt, mainBody, payBody, payTokenPrefixCombine.getJdDjCookie());
        payCombine.setOrderId(payTokenPrefixCombine.getOrderId());
        return payCombine;
    }

    public static void main(String[] args) {
        String a = "{\n" +
                "    \"code\": \"0\",\n" +
                "    \"msg\": \"成功\",\n" +
                "    \"result\": {\n" +
                "        \"appid\": \"wxe9aee36de8c7cb82\",\n" +
                "        \"partnerid\": \"1584023561\",\n" +
                "        \"prepayid\": \"wx150506576395850b5a1e921a145f9a0000\",\n" +
                "        \"noncestr\": \"7680527135fa4e6390187e0621c6a030\",\n" +
                "        \"timestamp\": \"1655240817\",\n" +
                "        \"sign\": \"05CBB281504244350BBD242E249B5412\",\n" +
                "        \"signType\": \"MD5\",\n" +
                "        \"mweburl\": \"https://wx.tenpay.com/cgi-bin/mmpayweb-bin/checkmweb?prepay_id=wx150506576395850b5a1e921a145f9a0000&package=4016968220\",\n" +
                "        \"package\": \"WAP\"\n" +
                "    },\n" +
                "    \"success\": true\n" +
                "}";
        //weixin://wap/pay?appid=wxae3e8056daea8727&noncestr=d7657583058394c828ee150fada65345&package=WAP&prepayid=wx15163907659762a1b7fe7c123279267263&timestamp=1557909547&sign=E722AA742F6E8F853C22B00F4220B246
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("weixin://wap/pay?");
        HashMap<String, String> result = JSON.parseObject(JSON.parseObject(a).getString("result"), HashMap.class);
        for (String s : result.keySet()) {
            if (s.contains("mweburl")) {
                continue;
            }
            stringBuilder.append(s + "=" + result.get(s) + "&");
        }
        System.out.println(stringBuilder.toString());
    }

    /**
     * @param payCombine 组合
     * @param client     代理
     * @return
     */
    public PayCombine PayCombineRequst(PayCombine payCombine, OkHttpClient client, String ip) {
        try {
            String url = "https://daojia.jd.com/client?functionId=tx/core/unifiedPay";
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
/*
            if (ObjectUtil.isNotNull(proxy)) {
                log.debug("当前设置payTokenPrefixCombineRequst,msg:{}", proxy.toString());
                builder.proxy(proxy);
            }
            OkHttpClient client = builder.followRedirects(false).build();
*/
            Map<String, String> headerMap = PreUtils.buildIpMap(ip);
            RequestBody requestBody = new FormBody.Builder()
                    .add("functionId", "tx/core/unifiedPay")
                    .add("djencrypt", URLDecoder.decode(payCombine.encrypt))
                    .build();
            Request.Builder requstBuild = new Request.Builder();
            requstBuild.post(requestBody);
            for (String key : headerMap.keySet()) {
                requstBuild.header(key, headerMap.get(key));
            }
            Request request = requstBuild.url(url).build();
            Response response = client.newCall(request).execute();
            String padataStr = response.body().string();
            int code = response.code();
            log.info("当前获取支付链接为msg:{}", padataStr);
            JSONObject parseObject = JSON.parseObject(padataStr);
            response.close();
            if (code == HttpStatus.HTTP_OK && parseObject.getBoolean("success")) {
                String result = parseObject.getString("result");
                if (StrUtil.isNotBlank(result) && result.contains("prepayid")) {
                    PayData payData = JSON.parseObject(result, PayData.class);
                    payCombine.setPayData(payData);
                    String hrefUrl = null;
                    TimeInterval timer = DateUtil.timer();
                    for (int i = 0; i < 4; i++) {
                        hrefUrl = weixinUrl(payData.getMweburl(), headerMap, "https://daojia.jd.com");
                        if (StrUtil.isNotBlank(hrefUrl)) {
                            break;
                        }
                    }
                    log.info("mweb_url消费时间:" + timer.interval());
                    if (StrUtil.isBlank(hrefUrl)) {
                        log.error("当前订单失败msg:请查看日志");
                        return null;
                    }
                    payCombine.getPayData().setHrefUrl(hrefUrl);
                    return payCombine;
                }
            }
        } catch (Exception e) {
            log.error("当前获取token报错，msg:{}，e:{}", payCombine, e.getMessage());
        }
        log.error("获取token错误，请查看日志报错信息");
        return null;
    }
//    /182.47.215.172:4214
//

    @Autowired
    private ProxyProductService proxyProductService;

    public String weixinUrl(String mweb_url, Map<String, String> headerMap, String referer) {
        try {
            if (ObjectUtil.isNull(mweb_url)) {
                return null;
            }
            JdProxyIpPort oneIp = this.proxyProductService.getOneIp(PreConstant.ZERO, PreConstant.ZERO, false);
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
            OkHttpClient client = builder.proxy(proxy).connectTimeout(2, TimeUnit.SECONDS).readTimeout(2, TimeUnit.SECONDS).build();
            Request.Builder header = new Request.Builder()
                    .url(mweb_url)
                    .get()
                    .addHeader("Referer", referer);
            if (CollUtil.isNotEmpty(headerMap)) {
                for (String key : headerMap.keySet()) {
                    header.header(key, headerMap.get(key));
                }
            }
            Request request = header.build();
            Response response = client.newCall(request).execute();
            String jingdonghtml = response.body().string();
            log.debug("请求微信接口为msg:{}", jingdonghtml);
            response.close();
            String P_COMM = "[a-zA-z]+://[^\\s]*";
            Pattern pattern = Pattern.compile(P_COMM);
            Matcher matcher = pattern.matcher(jingdonghtml);
            if (matcher.find()) {
                String group = matcher.group();
                String replace = group.replace("\"", "");
                return replace;
            }
        } catch (Exception e) {
            log.error("请求微信跳转链接为报错msg:{}", e.getMessage());
        }
        return null;
    }
}
