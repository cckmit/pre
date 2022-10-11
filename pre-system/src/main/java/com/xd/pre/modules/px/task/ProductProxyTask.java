package com.xd.pre.modules.px.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xd.pre.common.constant.PreConstant;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.modules.data.tenant.PreTenantContextHolder;
import com.xd.pre.modules.px.service.NewWeiXinPayUrl;
import com.xd.pre.modules.px.service.ProxyProductService;
import com.xd.pre.modules.px.vo.sys.NotifyVo;
import com.xd.pre.modules.sys.domain.*;
import com.xd.pre.modules.sys.mapper.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.activemq.ScheduledMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.Queue;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ProductProxyTask {

    @Autowired
    private ProxyProductService proxyProductService;


    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource
    private JdAppStoreConfigMapper jdAppStoreConfigMapper;

    @Autowired
    private NewWeiXinPayUrl newWeiXinPayUrl;

    @Resource(name = "product_stock_queue")
    private Queue product_stock_queue;


    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Resource
    private JdMchOrderMapper jdMchOrderMapper;

    @Resource
    private JdProxyIpPortMapper jdProxyIpPortMapper;


    @Autowired
    private NewWeiXinPayUrl weiXinPayUrl;


    @Resource
    private JdOrderPtMapper jdOrderPtMapper;

    /**
     * 激活队列
     */
    @Resource(name = "activate_meituan_queue")
    private Queue activate_meituan_queue;
    /**
     * 激活队列
     */
    @Resource(name = "activate_queue")
    private Queue activate_queue;

    @Resource
    private JdCkMapper jdCkMapper;


    // 发送消息，destination是发送到的队列，message是待发送的消息
    private void sendMessage(Destination destination, final String message) {
        //发送延迟队列，延迟10秒,单位毫秒
        jmsMessagingTemplate.convertAndSend(destination, message);
    }


    public void productWxNumMeiTuan(JdAppStoreConfig jdAppStoreConfig) {
        log.info("执行生产美团生产任务");
        List<JdAppStoreConfig> jdAppStoreConfigs = jdAppStoreConfigMapper.selectList(Wrappers.<JdAppStoreConfig>lambdaQuery()
                .eq(JdAppStoreConfig::getIsProduct, 1).eq(JdAppStoreConfig::getSkuId, jdAppStoreConfig.getSkuId()));
        if (CollUtil.isEmpty(jdAppStoreConfigs)) {
            log.info("当前信息不生产");
            return;
        }
        for (int i = 0; i < jdAppStoreConfig.getProductStockNum(); i++) {
            redisTemplate.opsForValue().increment("stock:" + jdAppStoreConfig.getSkuId(), 1);
            sendMessage(this.product_stock_queue, JSON.toJSONString(jdAppStoreConfig));
        }
        log.info("美团定时任务生产任务已经开启");
    }

    @Resource
    private AreaIpMapper areaIpMapper;


    @Scheduled(cron = "0 0/1 * * * ? ")
    @Async("asyncPool")
    public void productAll() {
        String ipLock = redisTemplate.opsForValue().get("锁定生产IP全部");
        if (StrUtil.isNotBlank(ipLock)) {
            return;
        }
        log.info("定时任务生产ip50");
        redisTemplate.opsForValue().set("锁定生产IP全部", "锁定IP全部", 3, TimeUnit.MINUTES);
        proxyProductService.productIpAndPort1();
        proxyProductService.productIpAndPort2();
    }


    //    @Scheduled(cron = "0 0/1 * * * ? ")
    @Async("asyncPool")
    public void productfindMaxOrder() {
        log.info("开始执行获取库存里面大于2的订单数据");
        JdAppStoreConfig jdAppStoreConfig = this.jdAppStoreConfigMapper.selectOne(Wrappers.<JdAppStoreConfig>lambdaQuery().eq(JdAppStoreConfig::getSkuId, "11183343342"));
        List<String> pins = jdOrderPtMapper.selectMax2Data(jdAppStoreConfig.getProductNum());
        //查出所有的订单。不管支付不支付的过期时间大于现在的
        List<JdOrderPt> jdOrderPts = jdOrderPtMapper.selectList(Wrappers.<JdOrderPt>lambdaQuery()
                .in(JdOrderPt::getPtPin, pins).gt(JdOrderPt::getExpireTime, new Date()));
        if (CollUtil.isNotEmpty(pins)) {
            Map<String, List<JdOrderPt>> orders = jdOrderPts.stream().collect(Collectors.groupingBy(JdOrderPt::getPtPin));
            for (String pin : pins) {
                //看情况如果出现2单。并且都是支付了的。就删除。如果有一单没有支付。就删除
                List<JdOrderPt> jdOrderPtsPay = orders.get(pin);
                if (CollUtil.isNotEmpty(jdOrderPtsPay)) {
                    //有未支付的订单。添加lock
                    List<JdOrderPt> notPayOrders = jdOrderPtsPay.stream().filter(it -> ObjectUtil.isNull(it.getCarMy())).collect(Collectors.toList());
                    if (CollUtil.isNotEmpty(notPayOrders) && notPayOrders.size() >= jdAppStoreConfig.getProductNum()) {
                        redisTemplate.opsForValue().set("锁定大于2当前的CK:" + URLDecoder.decode(pin), pin, 5, TimeUnit.MINUTES);
                    } else {
                        String lockpinRe = redisTemplate.opsForValue().get("释放时间限制:" + pin);
                        if (StrUtil.isNotBlank(lockpinRe)) {
                            continue;
                        }
                        redisTemplate.opsForValue().set("释放时间限制:" + pin, pin, 2, TimeUnit.MINUTES);
                        redisTemplate.delete("锁定大于2当前的CK:" + URLDecoder.decode(pin));
                        jdCkMapper.updateByPin(pin);
                    }
                }
            }
        }
//        List<String> pinPays = jdOrderPtMapper.selectMax2DataPay(jdAppStoreConfig.getProductNum());
        log.info("结束执行获取库存里面大于2的订单数据");
    }

    //    @Scheduled(cron = "0 0/1 * * * ? ")
    @Async("asyncPool")
    public void autoDeleteFailAccount() {
        List<JdOrderPt> orderPts = jdOrderPtMapper.selectList(Wrappers.<JdOrderPt>lambdaQuery()
                .gt(JdOrderPt::getCreateTime, DateUtil.offsetHour(new Date(), -24))
                .like(JdOrderPt::getHtml, "交易失败,退款处理中"));
        if (CollUtil.isEmpty(orderPts)) {
            return;
        }
        List<String> orderIds = orderPts.stream().map(it -> it.getOrderId()).distinct().collect(Collectors.toList());

        List<JdMchOrder> jdMchOrders = jdMchOrderMapper.selectList(Wrappers.<JdMchOrder>lambdaQuery().in(JdMchOrder::getOriginalTradeNo, orderIds)
                .notIn(JdMchOrder::getStatus, PreConstant.THREE));
        if (CollUtil.isNotEmpty(jdMchOrders)) {
            for (JdMchOrder jdMchOrder : jdMchOrders) {
                jdMchOrder.setStatus(PreConstant.THREE);
                jdMchOrderMapper.updateById(jdMchOrder);
            }
        }
        List<String> pins = orderPts.stream().map(JdOrderPt::getPtPin).distinct().collect(Collectors.toList());
        List<JdOrderPt> jdOrderPts = jdOrderPtMapper.selectList(Wrappers.<JdOrderPt>lambdaQuery().in(JdOrderPt::getPtPin, pins));
        for (JdOrderPt jdOrderPt : jdOrderPts) {
            jdOrderPt.setIsEnable(PreConstant.ZERO);
            jdOrderPt.setIsWxSuccess(PreConstant.ZERO);
            jdOrderPt.setFailTime(PreConstant.HUNDRED);
            jdOrderPt.setRetryTime(PreConstant.HUNDRED);
            redisTemplate.delete(PreConstant.订单管理微信链接 + jdOrderPt.getOrderId());
//            jdOrderPtMapper.deleteById(jdOrderPt.getId());
            jdOrderPtMapper.updateById(jdOrderPt);
        }
        List<JdCk> jdCks = this.jdCkMapper.selectList(Wrappers.<JdCk>lambdaQuery().in(JdCk::getPtPin, pins).in(JdCk::getIsEnable, Arrays.asList(1, 5)));
        if (CollUtil.isNotEmpty(jdCks)) {
            for (JdCk jdCk : jdCks) {
                jdCk.setIsEnable(PreConstant.ZERO);
                jdCk.setFailTime(PreConstant.HUNDRED);
                jdCkMapper.updateById(jdCk);
//                jdCkMapper.deleteById(jdCk.getId());
            }
        }
    }

    @Async("asyncPool")
//    @Scheduled(cron = "0/30 * * * * ?")
    public void activationMeiTuan() {
        List<JdAppStoreConfig> jdAppStoreConfigs = this.jdAppStoreConfigMapper.selectList(Wrappers.<JdAppStoreConfig>lambdaQuery().eq(JdAppStoreConfig::getGroupNum, PreConstant.TWO));
        List<String> meituanSkuIds = jdAppStoreConfigs.stream().map(it -> it.getSkuId()).distinct().collect(Collectors.toList());
        LambdaQueryWrapper<JdOrderPt> wrapper = Wrappers.<JdOrderPt>lambdaQuery()
                .in(JdOrderPt::getSkuId, meituanSkuIds)
                .gt(JdOrderPt::getExpireTime, new Date())
                .eq(JdOrderPt::getIsWxSuccess, PreConstant.ONE);
        List<JdOrderPt> jdOrderPts = this.jdOrderPtMapper.selectList(wrapper);
        if (CollUtil.isEmpty(jdOrderPts)) {
            return;
        }
        Map<String, List<JdOrderPt>> pinMaps = jdOrderPts.stream().collect(Collectors.groupingBy(JdOrderPt::getPtPin));
        for (String pin : pinMaps.keySet()) {
            List<JdOrderPt> jdOrderPtsIns = pinMaps.get(pin);
            int i = PreUtils.randomCommon(0, jdOrderPtsIns.size(), 1)[0];
            JdOrderPt jdOrderPtDb = jdOrderPtsIns.get(i);
            Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent(PreConstant.美团激活 + pin, JSON.toJSONString(jdOrderPtsIns), 3, TimeUnit.MINUTES);
            if (!ifAbsent) {
                continue;
            }
            this.sendMessageSenc(this.activate_meituan_queue, JSON.toJSONString(jdOrderPtDb), PreUtils.randomCommon(1, 150, 1)[0]);
        }
    }


    //    @Scheduled(cron = "0/30 * * * * ?")
    @Async("asyncPool")
    public void activation() {
        Set<String> keys = redisTemplate.keys("激活:*");
        LambdaQueryWrapper<JdOrderPt> wrapper = Wrappers.<JdOrderPt>lambdaQuery()
                .gt(JdOrderPt::getExpireTime, new Date())
                .le(JdOrderPt::getFailTime, PreConstant.TEN)
                .eq(JdOrderPt::getIsWxSuccess, PreConstant.ONE)
                .isNull(JdOrderPt::getCardNumber);
        if (CollUtil.isNotEmpty(keys)) {
            List<String> orderIds = new ArrayList<>();
            for (String key : keys) {
                String orderId = key.split(":")[1];
                orderIds.add(orderId);
            }
            wrapper.notIn(JdOrderPt::getOrderId, orderIds);
        }
//        keys = redisTemplate.keys(PreConstant.美团锁定CK到第二天结束 + "*);
//        if (CollUtil.isNotEmpty(keys) && jdAppStoreConfig.getGroupNum() == PreConstant.TWO) {
//            List<String> lockKeys = keys.stream().map(it -> it.split(":")[1]).map(it -> URLDecoder.decode(it)).collect(Collectors.toList());
//            wrapper.notIn(JdOrderPt::getPtPin, lockKeys);
//        }
        List<JdOrderPt> orderPts = this.jdOrderPtMapper.selectList(wrapper);
        mark:
        for (JdOrderPt jdOrderPtDb : orderPts) {
            String jihuo = redisTemplate.opsForValue().get("激活:" + jdOrderPtDb.getOrderId());
            if (StrUtil.isNotBlank(jihuo)) {
                continue;
            }
            String config = redisTemplate.opsForValue().get("配置文件:" + jdOrderPtDb.getSkuId());
            if (StrUtil.isBlank(config)) {
                List<JdAppStoreConfig> jdAppStoreConfigs = jdAppStoreConfigMapper.selectList(Wrappers.emptyWrapper());
                for (JdAppStoreConfig jdAppStoreConfig : jdAppStoreConfigs) {
                    redisTemplate.opsForValue().set("配置文件:" + jdAppStoreConfig.getSkuId(), JSON.toJSONString(jdAppStoreConfig), 1, TimeUnit.DAYS);
                }
                config = redisTemplate.opsForValue().get("配置文件:" + jdOrderPtDb.getSkuId());
            }
            JdAppStoreConfig jdAppStoreConfig = JSON.parseObject(config, JdAppStoreConfig.class);
            String lock = redisTemplate.opsForValue().get(PreConstant.美团锁定CK到第二天结束 + jdOrderPtDb.getPtPin());
            if (jdAppStoreConfig.getGroupNum() == PreConstant.TWO && StrUtil.isNotBlank(lock) && jdOrderPtDb.getIsWxSuccess() == PreConstant.ONE) {
             /*   Map<String, List<JdOrderPt>> mapKeys = orderPts.stream().collect(Collectors.groupingBy(JdOrderPt::getPtPin));
                List<JdOrderPt> jdOrderPtsByPin = mapKeys.get(jdOrderPtDb.getPtPin());
                for (JdOrderPt jdOrderPtByPin : jdOrderPtsByPin) {
                    String bypin = redisTemplate.opsForValue().get(PreConstant.订单管理微信链接 + jdOrderPtByPin.getOrderId());
                    if (StrUtil.isNotBlank(bypin)) {
                        continue mark;
                    }
                }
                R r = activateService.reSetNoAsync(jdOrderPtDb, activateService.getIp(), jdOrderPtsByPin);
                if (ObjectUtil.isNotNull(r) && r.getCode() == HttpStatus.HTTP_OK) {
                    if (CollUtil.isNotEmpty(jdOrderPtsByPin)) {
                        for (JdOrderPt jdOrderPtByPin : jdOrderPtsByPin) {
                            redisTemplate.opsForValue().set(PreConstant.订单管理微信链接 + jdOrderPtByPin.getOrderId(), "1111", 4, TimeUnit.MINUTES);
                            jdOrderPtByPin.setRetryTime(PreConstant.ZERO);
                            jdOrderPtByPin.setIsWxSuccess(PreConstant.ONE);
                            jdOrderPtByPin.setFailTime(PreConstant.ZERO);
                            this.jdOrderPtMapper.updateById(jdOrderPtByPin);
                        }
                    }
                    redisTemplate.delete(PreConstant.美团锁定CK到第二天结束 + jdOrderPtDb.getPtPin());
                    continue;
                }*/
                redisTemplate.opsForValue().set("当前失效订单:" + jdOrderPtDb.getOrderId(), jdOrderPtDb.getOrderId(), 12, TimeUnit.HOURS);
                jdOrderPtDb.setIsWxSuccess(PreConstant.ZERO);
                jdOrderPtDb.setFailTime(1200);
                this.jdOrderPtMapper.updateById(jdOrderPtDb);
                continue;
            }
            redisTemplate.opsForValue().set("激活:" + jdOrderPtDb.getOrderId(), jdOrderPtDb.getOrderId(), 3, TimeUnit.MINUTES);
            this.sendMessageSenc(this.activate_queue, JSON.toJSONString(jdOrderPtDb), PreUtils.randomCommon(1, 140, 1)[0]);
        }
    }

    // 发送消息，destination是发送到的队列，message是待发送的消息
    private void sendMessageSenc(Destination destination, final String message, Integer minit) {
        Map<String, Object> headers = new HashMap<>();
        //发送延迟队列，延迟10秒,单位毫秒
        headers.put(ScheduledMessage.AMQ_SCHEDULED_DELAY, minit * 1000);
        jmsMessagingTemplate.convertAndSend(destination, message, headers);
    }


    @Scheduled(cron = "0 0/5 * * * ? ")
    @Async("asyncPool")
    public void deleteIp() {
        log.info("执行删除不用的IP");
        jdProxyIpPortMapper.delete(Wrappers.<JdProxyIpPort>lambdaQuery().lt(JdProxyIpPort::getExpirationTime, new Date()));
    }

    public static Map<Integer, OkHttpClient> okClient = new HashMap();

    @Scheduled(cron = "0 0/1 * * * ? ")
    @Async("asyncPool")
    public void productOkHttpClient() {
        Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent("生产OkHttpClient", "1", 50, TimeUnit.SECONDS);
        if (!ifAbsent) {
            return;
        }
        if (CollUtil.isNotEmpty(okClient)) {
            Set<Integer> exs = okClient.keySet();
            ArrayList<Integer> removes = new ArrayList<>();
            for (Integer ex : exs) {
                String isAble = redisTemplate.opsForValue().get("IP缓存池:" + ex);
                if (StrUtil.isBlank(isAble)) {
                    removes.add(ex);
                }
            }
            if (CollUtil.isNotEmpty(removes)) {
                for (Integer removeId : removes) {
                    redisTemplate.delete("当前存在OkHttpClient:" + removeId);
                    okClient.remove(removeId);
                }
            }
            if (CollUtil.isNotEmpty(okClient)) {
                for (Integer ex : exs) {
                    redisTemplate.opsForValue().set("当前存在OkHttpClient:" + ex, JSON.toJSONString(okClient.get(ex)), 1, TimeUnit.MINUTES);
                }
            }
        }

        if (CollUtil.isNotEmpty(okClient) && okClient.size() > 20) {
            return;
        }
        Integer count = jdMchOrderMapper.selectCount(Wrappers.<JdMchOrder>lambdaQuery().gt(JdMchOrder::getCreateTime, DateUtil.offsetMinute(new Date(), -10)));
        if (count == 0) {
            return;
        }
        Integer forData = 5;
        int mi = count / 5;
        if (mi > forData) {
            forData = mi * 2;
        }
        for (int i = 0; i < forData; i++) {
            JdProxyIpPort oneIp = this.proxyProductService.getOneIp(PreConstant.ZERO, PreConstant.ZERO, false);
            try {
                log.info("10分钟之前有订单,开始创建OkHttpClinet");
                buildStaticIp(oneIp);
            } catch (Exception e) {
                log.error("当前生产okHttpClient:{}", e.getMessage());
            }
        }
    }

    @Async("asyncPool")
    public void buildStaticIp(JdProxyIpPort oneIp) throws IOException {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        String isAble = redisTemplate.opsForValue().get("是否使用代理");
        if (StrUtil.isNotBlank(isAble) && Integer.valueOf(isAble) == PreConstant.ONE) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
            builder.proxy(proxy);
        } else {
            redisTemplate.opsForValue().set("是否使用代理", "1");
        }
        OkHttpClient client = builder.connectTimeout(5, TimeUnit.SECONDS).readTimeout(5, TimeUnit.SECONDS)
                .callTimeout(5, TimeUnit.SECONDS).writeTimeout(5, TimeUnit.SECONDS)
                .followRedirects(false).build();
        Request request = new Request.Builder()
                .url("http://210.16.122.100")
                .get()
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Mobile Safari/537.36")
                .build();
        Response response = client.newCall(request).execute();
        String resStr = response.body().string();
        response.close();
        if (StrUtil.isNotBlank(resStr) && resStr.contains("立即支付")) {
            okClient.put(oneIp.getId(), client);
            redisTemplate.opsForValue().set("当前存在OkHttpClient:" + oneIp.getId(), JSON.toJSONString(oneIp), 3, TimeUnit.MINUTES);
        }
    }


    /*    @Scheduled(cron = "0/20 * * * * ?")
        @Async("asyncPool")*/
    public void callBack() {
        DateTime dateTime = DateUtil.offsetSecond(new Date(), -300);
        List<JdMchOrder> jdMchOrders = jdMchOrderMapper.selectList(Wrappers.<JdMchOrder>lambdaQuery().ge(JdMchOrder::getCreateTime, dateTime).isNotNull(JdMchOrder::getOriginalTradeNo));
        for (JdMchOrder jdMchOrder : jdMchOrders) {
            try {
                String data = redisTemplate.opsForValue().get("查询订单:" + jdMchOrder.getTradeNo());
                if (StrUtil.isNotBlank(data)) {
                    return;
                }
                redisTemplate.opsForValue().set("查询订单:" + jdMchOrder.getTradeNo(), jdMchOrder.getTradeNo(), 10, TimeUnit.SECONDS);
                if (jdMchOrder.getStatus() != 2) {
                    weiXinPayUrl.getCartNumAndMy(jdMchOrder);
                    JdMchOrder jdMchOrderIn = jdMchOrderMapper.selectById(jdMchOrder.getId());
                    if (jdMchOrderIn.getStatus() == 2) {
                        notifySuccess(jdMchOrder);
                    }
                } else {
                    if (jdMchOrder.getNotifySucc() != 1) {
                        notifySuccess(jdMchOrder);
                    }
                }
            } catch (Exception e) {
                log.info("出现未知情况msg:{}", e.getStackTrace());
            }
        }
    }

    /**
     * 提示
     *
     * @param jdMchOrder
     */
    public Boolean notifySuccess(JdMchOrder jdMchOrder) {
        try {
            JdMchOrder jdMchOrderDb = jdMchOrderMapper.selectById(jdMchOrder.getId());
            if (ObjectUtil.isNull(jdMchOrder) || jdMchOrder.getStatus() != PreConstant.TWO) {
                log.info("订单号:{},未成功，不用通知", jdMchOrder.getTradeNo());
                return false;
            }
            NotifyVo notifyVo = NotifyVo.builder()
                    .mch_id(jdMchOrderDb.getMchId())
                    .trade_no(jdMchOrderDb.getTradeNo())
                    .out_trade_no(jdMchOrderDb.getOutTradeNo())
                    .money(jdMchOrderDb.getMoney())
                    .notify_time(DateUtil.formatDateTime(new Date()))
                    .status(jdMchOrderDb.getStatus() + "").build();
            cn.hutool.json.JSON json = new JSONObject(notifyVo);
            String result = HttpRequest.post(jdMchOrderDb.getNotifyUrl())
                    .body(JSON.toJSONString(json))
                    .timeout(5000)
                    .execute().body();
            if (StrUtil.isNotBlank(result) && result.toLowerCase().equals("success")) {
                log.info("订单号:{}通知支付成功", jdMchOrder.getTradeNo());
                jdMchOrderDb.setNotifySucc(PreConstant.ONE);
                PreTenantContextHolder.setCurrentTenantId(jdMchOrder.getTenantId());
                log.info("订单号，{}通知结果后返回的数据:{},租户:{}", jdMchOrder.getTradeNo(), JSON.toJSONString(jdMchOrderDb), jdMchOrder.getTenantId());
                jdMchOrderMapper.updateByIdNotSuccess(jdMchOrder.getId(), PreConstant.ONE);
                return true;
            }
        } catch (Exception e) {
            log.error("报错了，通知失败");
        }
        return false;
    }


}
