package com.xd.pre.modules.px.jddj.orderdetail;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xd.pre.common.constant.PreConstant;
import com.xd.pre.common.h5st.HMAC;
import com.xd.pre.modules.px.jddj.cookie.JdDjCookie;
import com.xd.pre.modules.px.jddj.main.MainBody;
import com.xd.pre.modules.px.jddj.utils.JdDjSignUtils;
import com.xd.pre.modules.sys.mapper.JdCkZhidengMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.util.HashMap;


@Data
@Slf4j
@Component
@NoArgsConstructor
public class OrderDetailCombine {

    private String encrypt;
    private OrderDetailBody orderDetailBody;
    private MainBody mainBody;
    private JdDjCookie jdDjCookie;
    private String orderId;
    private Integer orderStatus;
    private String html;

    public OrderDetailCombine(String encrypt, OrderDetailBody orderDetailBody, MainBody mainBody, JdDjCookie jdDjCookie, String orderId) {
        this.encrypt = encrypt;
        this.orderDetailBody = orderDetailBody;
        this.mainBody = mainBody;
        this.jdDjCookie = jdDjCookie;
        this.orderId = orderId;
    }

    /**
     * 签证
     *
     * @param jdDjCookie ck对象
     * @return
     * @throws Exception
     */
    public static OrderDetailCombine getDjencrypt(JdDjCookie jdDjCookie, String orderId) {
        try {
            OrderDetailBody orderDetailBody = new OrderDetailBody(orderId);
            MainBody mainBody = new MainBody(jdDjCookie.getDeviceid_pdj_jd(), JSON.toJSONString(orderDetailBody));
            HashMap hashMap = JSON.parseObject(JSON.toJSONString(mainBody), HashMap.class);
            hashMap.remove("signKeyV1");
            String sortStr = JdDjSignUtils.mapSortedByKey(hashMap);
            String hmacSHA256 = HMAC.HmacSHA(sortStr, JdDjSignUtils.H_MAC_HASH_KEY, "HmacSHA256");
            hashMap.put("signKeyV1", hmacSHA256);
            String encrypt = JdDjSignUtils.Encrypt(JSON.toJSONString(hashMap));
            OrderDetailCombine cancelCombine = new OrderDetailCombine(encrypt, orderDetailBody, mainBody, jdDjCookie, orderId);
            return cancelCombine;
        } catch (Exception e) {
            log.error("组合订单详情查询报错msg:{}", e.getMessage());
            return null;
        }

    }

    public OrderDetailCombine orderDetailCombineRequst(OrderDetailCombine orderDetailCombine, OkHttpClient client, JdCkZhidengMapper zhidengMapper) {
        try {
            String url = String.format("https://daojia.jd.com/client?&functionId=order/infoNew&djencrypt=%s", orderDetailCombine.getEncrypt());
/*            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            if (ObjectUtil.isNotNull(proxy)) {
                log.debug("当前下单设置代理msg:{}", proxy.toString());
                builder.proxy(proxy);
            }
            OkHttpClient client = builder.followRedirects(false).build();*/
            Request request = new Request.Builder().url(url)
                    .get()
                    .addHeader("Cookie", String.format("o2o_m_h5_sid=%s;", orderDetailCombine.getJdDjCookie().getO2o_m_h5_sid()))
                    .build();
            Response response = client.newCall(request).execute();
            String orderStr = response.body().string();
            log.info("下单结果查询结果msg:{}", orderStr);
            if (StrUtil.isNotBlank(orderStr) && orderStr.contains("为了您的账号安全，请重新登录")) {
                
            }
            JSONObject parseObject = JSON.parseObject(orderStr);
            if (response.code() == HttpStatus.HTTP_OK && parseObject.getBoolean("success")) {
                JSONObject orderDetailData = JSON.parseObject(JSON.parseObject(parseObject.getString("result")).getString("orderStateMap"));
                orderDetailCombine.setHtml(JSON.toJSONString(orderDetailData));
                if (JSON.toJSONString(orderDetailData).contains("订单已完成") && JSON.toJSONString(orderDetailData).contains("礼品卡制卡成功")) {
                    log.info("当前订单支付完成msg:{}", orderDetailCombine.getOrderId());
                    orderDetailCombine.setOrderStatus(PreConstant.TWO);
                    response.close();
                    return orderDetailCombine;
                }
                if (JSON.toJSONString(orderDetailData).contains("待支付")) {
                    log.info("订单待支付msg:{},", orderDetailCombine.getOrderId());
                    orderDetailCombine.setOrderStatus(PreConstant.ONE);
                    response.close();
                    return orderDetailCombine;
                }
                if (JSON.toJSONString(orderDetailData).contains("订单已锁定") || JSON.toJSONString(orderDetailData).contains("受系统影响您的订单正在原路返回中")) {
                    orderDetailCombine.setOrderStatus(PreConstant.THREE);
                    response.close();
                    return orderDetailCombine;
                }
            }
        } catch (Exception e) {
            log.error("查询订单报错，msg:{}，e:{}", orderId, e.getMessage());
        }
        log.error("查询订单报错，请查看日志报错信息");
        return null;
    }

}
