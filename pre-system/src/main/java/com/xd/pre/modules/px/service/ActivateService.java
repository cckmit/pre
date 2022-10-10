package com.xd.pre.modules.px.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xd.pre.common.constant.PreConstant;
import com.xd.pre.common.utils.R;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.modules.px.vo.reqvo.TokenKeyVo;
import com.xd.pre.modules.px.vo.resvo.TokenKeyResVo;
import com.xd.pre.modules.sys.domain.JdAppStoreConfig;
import com.xd.pre.modules.sys.domain.JdOrderPt;
import com.xd.pre.modules.sys.domain.JdProxyIpPort;
import com.xd.pre.modules.sys.mapper.JdAppStoreConfigMapper;
import com.xd.pre.modules.sys.mapper.JdCkMapper;
import com.xd.pre.modules.sys.mapper.JdOrderPtMapper;
import com.xd.pre.modules.sys.mapper.JdPayOrderPostAddressMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.Queue;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@Lazy
public class ActivateService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Autowired
    private ProxyProductService proxyProductService;

    @Autowired()
    private NewWeiXinPayUrl newWeiXinPayUrlService;

    @Resource
    private JdPayOrderPostAddressMapper jdPayOrderPostAddressMapper;

    @Resource
    private JdOrderPtMapper jdOrderPtMapper;
    @Resource
    private JdAppStoreConfigMapper jdAppStoreConfigMapper;

    @Autowired
    private TokenKeyService tokenKeyService;

    @Resource
    private JdCkMapper jdCkMapper;


    @Resource(name = "ios_product_queue")
    private Queue ios_product_queue;

    public JdProxyIpPort getIp() {
        JdProxyIpPort oneIp = null;
        try {
            oneIp = proxyProductService.getOneIp(0, 0, true);
        } catch (Exception e) {
            log.error("获取ip失败");
        }
        return oneIp;
    }

    public void productIosOrderMq(String skuId) {
        for (int i = 0; i < 50; i++) {
            redisTemplate.opsForValue().increment(PreConstant.生产订单IOS锁定 + skuId, 1);
            sendMessageNotTime(ios_product_queue, skuId);
        }
    }

    //    @Async("asyncPool")
    public Boolean productIosOrder(String skuId) {
        log.info("获取ck没有过期的，并且生产到没有退款的订单生产ios订单");
        List<String> cks = jdCkMapper.selectIosCk();
        if (CollUtil.isEmpty(cks) || cks.size() <= 10) {
            return null;
        }
        log.info("开始生产");
        JdAppStoreConfig jdAppStoreConfigProduct = this.jdAppStoreConfigMapper.selectOne(Wrappers.<JdAppStoreConfig>lambdaQuery().eq(JdAppStoreConfig::getSkuId, skuId));
        JdProxyIpPort oneIp = proxyProductService.getOneIp(0, 0, false);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
        int[] ints = PreUtils.randomCommon(0, cks.size(), 2);
        for (int i = 0; i < ints.length; i++) {
            String ck = cks.get(ints[i]);
            String lock = redisTemplate.opsForValue().get(PreConstant.生产订单IOS锁定 + skuId);
            if (StrUtil.isBlank(lock) || Integer.valueOf(lock) <= 0) {
                log.debug("当前不生产了");
            }
            if (!ck.contains("pin=") || !ck.contains("wskey=")) {
                log.debug("当前ck不存在appck");
                continue;
            }
            try {
                return productAsny(skuId, jdAppStoreConfigProduct, oneIp, proxy, ck);
            } catch (Exception e) {
                log.error("生产订单报错");
            }
        }
        return false;
    }


    public Boolean productAsny(String skuId, JdAppStoreConfig jdAppStoreConfigProduct, JdProxyIpPort oneIp, Proxy proxy, String ck) {
        String orderId = newWeiXinPayUrlService.getOrderId(ck, proxy, jdAppStoreConfigProduct);
        if (StrUtil.isBlank(orderId)) {
            return false;
        }
        TokenKeyVo build = TokenKeyVo.builder().cookie(ck.trim()).build();
        TokenKeyResVo tokenKey = tokenKeyService.getTokenKey(build, oneIp, "");
        String payId = newWeiXinPayUrlService.getPayId(ck, orderId, proxy, jdAppStoreConfigProduct.getSkuPrice().intValue() + ".00");
        if (StrUtil.isBlank(payId)) {
            return false;
        }
        String mck = newWeiXinPayUrlService.getMck(proxy, tokenKey.getTokenKey());
        if (StrUtil.isBlank(mck)) {
            return false;
        }
        Boolean check = newWeiXinPayUrlService.check(payId, mck, proxy);
        if (!check) {
            log.info("mck获取失败");
            return false;
        }
        String iosPayData = newWeiXinPayUrlService.payUrlIos(proxy, payId);
        String pt_pin = PreUtils.get_pt_pin(ck);
        if (StrUtil.isNotBlank(iosPayData)) {
            JdOrderPt ios = JdOrderPt.builder().orderId(orderId).ptPin(pt_pin).expireTime(DateUtil.offsetHour(new Date(), 12)).createTime(new Date())
                    .prerId(payId).isWxSuccess(PreConstant.ZERO).currentCk(mck).port(oneIp.getPort()).ip(oneIp.getIp()).orgAppCk(ck).isEnable(PreConstant.ONE)
                    .skuPrice(jdAppStoreConfigProduct.getSkuPrice()).skuId(jdAppStoreConfigProduct.getSkuId()).skuName(jdAppStoreConfigProduct.getSkuName())
                    .failTime(1000)
                    .retryTime(1000)
                    .payData(iosPayData)
                    .build();
            redisTemplate.opsForValue().increment(PreConstant.生产订单IOS锁定 + skuId, -1);
            if (jdAppStoreConfigProduct.getSkuPrice().intValue() == PreConstant.HUNDRED) {
                redisTemplate.opsForValue().set(PreConstant.IOS订单_100 + orderId, JSON.toJSONString(ios), 10, TimeUnit.HOURS);
                return true;
            }
            if (jdAppStoreConfigProduct.getSkuPrice().intValue() == PreConstant.HUNDRED_2) {
                redisTemplate.opsForValue().set(PreConstant.IOS订单_200 + orderId, JSON.toJSONString(ios), 10, TimeUnit.HOURS);
                return true;
            }
        }
        return false;
    }

    public R reSetNoAsync(JdOrderPt jdOrderPtDb, JdProxyIpPort oneIp, List<JdOrderPt> jdOrderPtsByPin) {
        jdOrderPtDb = this.jdOrderPtMapper.selectById(jdOrderPtDb.getId());
        JdAppStoreConfig jdAppStoreConfig = jdAppStoreConfigMapper.selectOne(Wrappers.<JdAppStoreConfig>lambdaQuery().eq(JdAppStoreConfig::getSkuId, jdOrderPtDb.getSkuId()));
        try {
            log.debug("当前失效，但是可以尝试一下。这个数据在不同当前ck下面点击支付是否有支付能力");
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
            String payId = "";
            if (jdAppStoreConfig.getProductNum() == PreConstant.ONE) {
                payId = newWeiXinPayUrlService.getPayId(jdOrderPtDb.getOrgAppCk(), jdOrderPtDb.getOrderId(), proxy, jdOrderPtDb.getSkuPrice().intValue() + ".00");
            }
            if (jdAppStoreConfig.getGroupNum() == PreConstant.TWO) {
//                payId = getPayIdMeiTuan(mck, orderId, proxy, jdAppStoreConfigProduct);
                payId = newWeiXinPayUrlService.getPayIdMeiTuan(jdOrderPtDb.getCurrentCk(), jdOrderPtDb.getOrderId(), proxy, jdAppStoreConfig);
            }
            if (StrUtil.isNotBlank(payId)) {
                jdOrderPtDb.setPrerId(payId);
                Boolean check = newWeiXinPayUrlService.check(payId, jdOrderPtDb.getCurrentCk(), proxy);
                String payIdTimeOut = redisTemplate.opsForValue().get("订单支付已超时1分钟:" + payId);
                if (StrUtil.isNotBlank(payIdTimeOut)) {
                    payId = newWeiXinPayUrlService.getPayIdMeiTuan(jdOrderPtDb.getOrgAppCk(), jdOrderPtDb.getOrderId(), proxy, jdAppStoreConfig);
                    jdOrderPtDb.setPrerId(payId);
                }
                if (!check) {
                    TokenKeyVo build = TokenKeyVo.builder().cookie(jdOrderPtDb.getOrgAppCk()).build();
                    TokenKeyResVo tokenKey = tokenKeyService.getTokenKey(build, oneIp, "");
                    String mck = newWeiXinPayUrlService.getMck(proxy, tokenKey.getTokenKey());
                    jdOrderPtDb.setCurrentCk(mck);
                    newWeiXinPayUrlService.check(payId, jdOrderPtDb.getCurrentCk(), proxy);
/*                    payIdTimeOut = redisTemplate.opsForValue().get("订单支付已超时1分钟:" + payId);
                    if (StrUtil.isNotBlank(payIdTimeOut)) {
                        payId = newWeiXinPayUrlService.getPayId(jdOrderPtDb.getOrgAppCk(), jdOrderPtDb.getOrderId(), proxy, jdOrderPtDb.getSkuPrice().intValue() + ".00");
                        jdOrderPtDb.setPrerId(payId);
                    }*/
                }
                String payUrl = newWeiXinPayUrlService.payUrl(payId, jdOrderPtDb.getCurrentCk(), proxy, jdOrderPtDb.getPtPin(), jdOrderPtDb.getOrgAppCk(), PreConstant.ZERO,
                        jdAppStoreConfig.getGroupNum(), jdOrderPtDb.getOrderId(), jdAppStoreConfig);
                if (StrUtil.isBlank(payUrl)) {
                    log.debug("当前订单重试也没有了");
                    if (ObjectUtil.isNull(jdOrderPtDb.getRetryTime())) {
                        jdOrderPtDb.setRetryTime(PreConstant.ZERO);
                    }
                    jdOrderPtDb.setRetryTime(jdOrderPtDb.getRetryTime() + 1);
                    this.jdOrderPtMapper.updateById(jdOrderPtDb);
                    return R.error("当前订单重试也没有了");
                } else {
                    //  redisTemplate.opsForValue().set(PreConstant.有用的payId + orderId, payId, 5, TimeUnit.MINUTES);
                    String payIdRedis = redisTemplate.opsForValue().get(PreConstant.有用的payId + jdOrderPtDb.getOrderId());
                    if (StrUtil.isNotBlank(payIdRedis) && !jdOrderPtDb.getPrerId().equals(payIdRedis)) {
                        jdOrderPtDb.setPrerId(payIdRedis);
                    }
                    if (CollUtil.isNotEmpty(jdOrderPtsByPin)) {
                        for (JdOrderPt jdOrderPtByPin : jdOrderPtsByPin) {
                            redisTemplate.opsForValue().set(PreConstant.订单管理微信链接 + jdOrderPtByPin.getOrderId(), payUrl, 4, TimeUnit.MINUTES);
                            jdOrderPtByPin.setIsWxSuccess(PreConstant.ONE);
                            jdOrderPtByPin.setRetryTime(PreConstant.ZERO);
                            jdOrderPtByPin.setFailTime(PreConstant.ZERO);
                            this.jdOrderPtMapper.updateById(jdOrderPtByPin);
                        }
                    }
                    redisTemplate.opsForValue().set(PreConstant.订单管理微信链接 + jdOrderPtDb.getOrderId(), payUrl, 4, TimeUnit.MINUTES);
                    jdOrderPtDb.setFailTime(PreConstant.ZERO);
                    jdOrderPtDb.setIsWxSuccess(PreConstant.ONE);
                    jdOrderPtDb.setRetryTime(PreConstant.ZERO);
                    this.jdOrderPtMapper.updateById(jdOrderPtDb);
                    return R.ok();
                }
            }
        } catch (Exception e) {
            log.error("当前重置订单报错");
        }
        return R.error();
    }

    /**
     * 检查美团订单
     *
     * @param jdOrderPtDb
     */
    public void reSetMeiTuan(JdOrderPt jdOrderPtDb) {
        JdProxyIpPort oneIp = getIp();
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
        JdAppStoreConfig jdAppStoreConfig = jdAppStoreConfigMapper.selectOne(Wrappers.<JdAppStoreConfig>lambdaQuery().eq(JdAppStoreConfig::getSkuId, jdOrderPtDb.getSkuId()));
        String payId = newWeiXinPayUrlService.getPayIdMeiTuan(jdOrderPtDb.getCurrentCk(), jdOrderPtDb.getOrderId(), null, jdAppStoreConfig);
        if (StrUtil.isBlank(payId)) {
            log.error("获取美团激活失败payId失败，删除美团激活，重新验证");
            redisTemplate.delete(PreConstant.美团激活 + jdOrderPtDb.getPtPin());
        }
        Boolean check = newWeiXinPayUrlService.check(payId, jdOrderPtDb.getCurrentCk(), null);
        if (!check) {
            check = newWeiXinPayUrlService.check(payId, jdOrderPtDb.getCurrentCk(), null);
            if (!check) {
                log.error("检查失败，请查看日志");
                return;
            }
        }
        log.info("检查通过，获取微信链接");
        String payUrl = newWeiXinPayUrlService.payUrlMatchMeiTuan(payId, jdOrderPtDb.getCurrentCk(), proxy);
        if (StrUtil.isNotBlank(payUrl)) {
            log.info("当前pin有效，msg:{}", jdOrderPtDb.getPtPin());
            return;
        }
        log.info("当前pin已经失效，或者获取失败,请查看日志msg:{}", jdOrderPtDb.getPtPin());
    }

    @Async("asyncPoolRet")
    public R reSet(JdOrderPt jdOrderPtDb, JdProxyIpPort oneIp, List<JdOrderPt> jdOrderPtsByPin) {
        return this.reSetNoAsync(jdOrderPtDb, oneIp, jdOrderPtsByPin);
    }

    public R consumption(JdOrderPt jdOrderPtDb, Boolean isMatch, JdProxyIpPort oneIp) {

        //判断是否执行
        LambdaQueryWrapper<JdOrderPt> wrapper = Wrappers.<JdOrderPt>lambdaQuery();
        wrapper.eq(JdOrderPt::getPtPin, jdOrderPtDb.getPtPin());
        wrapper.ne(JdOrderPt::getFailTime, PreConstant.HUNDRED);
        wrapper.ge(JdOrderPt::getExpireTime, new Date());
        List<JdOrderPt> jdOrderPtsByPin = jdOrderPtMapper.selectList(wrapper);
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        jdOrderPtDb = this.jdOrderPtMapper.selectById(jdOrderPtDb.getId());
        log.debug("开始执行激活消息");
        try {
            String skuId = jdOrderPtDb.getSkuId();
            JdAppStoreConfig jdAppStoreConfig = jdAppStoreConfigMapper.selectOne(Wrappers.<JdAppStoreConfig>lambdaQuery().eq(JdAppStoreConfig::getSkuId, skuId));
//            newWeiXinPayUrlService.check(jdOrderPtDb.getPrerId(), jdOrderPtDb.getCurrentCk(), null);
            if (isMatch) {
                log.info("从新获取新的payId");
                if (jdAppStoreConfig.getGroupNum() == PreConstant.TWO) {
//                payId = getPayIdMeiTuan(mck, orderId, proxy, jdAppStoreConfigProduct);
                    String payId = newWeiXinPayUrlService.getPayIdMeiTuan(jdOrderPtDb.getCurrentCk(), jdOrderPtDb.getOrderId(), null, jdAppStoreConfig);
                    Boolean check = newWeiXinPayUrlService.check(payId, jdOrderPtDb.getCurrentCk(), null);
                    if (!check) {
                        return R.error("执行失败");
                    }
                    jdOrderPtDb.setPrerId(payId);
                    this.jdOrderPtMapper.updateById(jdOrderPtDb);
                }
            }
            String body = "";
            if (ObjectUtil.isNotNull(oneIp)) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
                body = newWeiXinPayUrlService.payUrl(jdOrderPtDb.getPrerId(), jdOrderPtDb.getCurrentCk(), proxy, jdOrderPtDb.getPtPin(), jdOrderPtDb.getOrgAppCk(), PreConstant.ZERO,
                        jdAppStoreConfig.getGroupNum(), jdOrderPtDb.getOrderId(), jdAppStoreConfig);
            } else {
                body = newWeiXinPayUrlService.payUrl(jdOrderPtDb.getPrerId(), jdOrderPtDb.getCurrentCk(), null, jdOrderPtDb.getPtPin(), jdOrderPtDb.getOrgAppCk(), PreConstant.ZERO,
                        jdAppStoreConfig.getGroupNum(), jdOrderPtDb.getOrderId(), jdAppStoreConfig);
            }
            if (StrUtil.isNotBlank(body) && body.contains("wx.tenpay.com")) {
                String payId = redisTemplate.opsForValue().get(PreConstant.有用的payId + jdOrderPtDb.getOrderId());
                if (StrUtil.isNotBlank(payId) && !payId.equals(jdOrderPtDb.getPrerId())) {
                    jdOrderPtDb.setPrerId(payId);
                    this.jdOrderPtMapper.updateById(jdOrderPtDb);
                }
                if (CollUtil.isNotEmpty(jdOrderPtsByPin)) {
                    for (JdOrderPt jdOrderPtByPin : jdOrderPtsByPin) {
                        redisTemplate.opsForValue().set(PreConstant.订单管理微信链接 + jdOrderPtByPin.getOrderId(), body, 4, TimeUnit.MINUTES);
                        jdOrderPtByPin.setIsWxSuccess(PreConstant.ONE);
                        jdOrderPtByPin.setFailTime(PreConstant.ZERO);
                        jdOrderPtByPin.setRetryTime(PreConstant.ZERO);
                        this.jdOrderPtMapper.updateById(jdOrderPtByPin);
                    }
                }
                redisTemplate.opsForValue().set(PreConstant.订单管理微信链接 + jdOrderPtDb.getOrderId(), body, 4, TimeUnit.MINUTES);
                return R.ok();
            }
            OkHttpClient client = builder.build();
            String url = String.format("https://pay.m.jd.com/index.action?functionId=wapWeiXinPay&body={\"appId\":\"jd_m_pay\",\"payId\":\"%s\",\"eid\":\"%s\"}&appId=jd_m_pay",
                    jdOrderPtDb.getPrerId(), "eidAf7ec812217sc2unIhDbfRC2vyIiCyWCfp9rpygQ1n05pH+F1dg0Jdhd0vcmUDK5s/mtSTjOeIOzXUO1lnWYQ/J491OJXOd6I2dnstXCXFGiREnBu");
            Request.Builder header = new Request.Builder()
                    .url(url)
                    .get()
                    .header("cookie", jdOrderPtDb.getCurrentCk())
                    .header("referer", "https://pay.m.jd.com/cpay/newPay-index.html");
            Request request = header.build();
            Response response = client.newCall(request).execute();
            body = response.body().string();
            response.close();
            if (StrUtil.isNotBlank(body) && body.contains("wx.tenpay.com")) {
                log.info("获取微信值付的链接msg:{}", body);
//                {"code":"0","mweb_url":"https://wx.tenpay.com/cgi-bin/mmpayweb-bin/checkmweb?prepay_id=wx15195806688647a40836b7ee0651210000&package=3513215870&redirect_url=https%3A%2F%2Fpay.m.jd.com%2FwapWeiXinPay%2FweiXinH5PayQuery.action%3FappId%3Djd_m_pay%26payId%3Dc81aee63147949cca0bcd638379f3af7"}
                JSONObject mweb_url_object = JSON.parseObject(body);
                if (mweb_url_object.containsKey("mweb_url")) {
                    log.info("设置微信链接管理关系");
                    for (JdOrderPt jdOrderPtByPin : jdOrderPtsByPin) {
                        jdOrderPtByPin.setIsWxSuccess(PreConstant.ONE);
                        jdOrderPtByPin.setFailTime(PreConstant.ZERO);
                        jdOrderPtByPin.setRetryTime(PreConstant.ZERO);
                        this.jdOrderPtMapper.updateById(jdOrderPtByPin);
                        redisTemplate.opsForValue().set(PreConstant.订单管理微信链接 + jdOrderPtByPin.getOrderId(), mweb_url_object.get("mweb_url").toString(), 4, TimeUnit.MINUTES);
                    }
                    redisTemplate.opsForValue().set(PreConstant.订单管理微信链接 + jdOrderPtDb.getOrderId(), mweb_url_object.get("mweb_url").toString(), 4, TimeUnit.MINUTES);
                }
                return R.ok();
            } else {
                JdProxyIpPort oneIp1 = getIp();
                R r = this.reSetNoAsync(jdOrderPtDb, oneIp1, jdOrderPtsByPin);
                if (ObjectUtil.isNotNull(r) && r.getCode() == HttpStatus.OK.value()) {
                    return R.ok();
                }
            }
            return R.error("执行失败");
        } catch (Exception e) {
            log.error("当前获取失败msgL{}", jdOrderPtDb);
        }
        return R.error("当前执行失败msg:{}");
    }

    // 发送消息，destination是发送到的队列，message是待发送的消息
    private void sendMessageNotTime(Destination destination, final String message) {
        jmsMessagingTemplate.convertAndSend(destination, message);
    }


}
