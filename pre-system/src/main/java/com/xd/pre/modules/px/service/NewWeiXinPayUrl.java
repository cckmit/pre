package com.xd.pre.modules.px.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xd.pre.common.constant.PreConstant;
import com.xd.pre.common.h5st.HMAC;
import com.xd.pre.common.sign.JdSgin;
import com.xd.pre.common.utils.R;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.common.utils.px.dto.SignVoAndDto;
import com.xd.pre.common.utils.px.dto.UrlEntity;
import com.xd.pre.modules.px.appstorePc.PcAppStoreService;
import com.xd.pre.modules.px.cotroller.PaySign;
import com.xd.pre.modules.px.douyin.DouYinHuaDanService;
import com.xd.pre.modules.px.douyin.DouyinService;
import com.xd.pre.modules.px.douyin.YongHuiService;
import com.xd.pre.modules.px.vo.reqvo.TokenKeyVo;
import com.xd.pre.modules.px.vo.resvo.TokenKeyResVo;
import com.xd.pre.modules.px.vo.tmpvo.appstorevo.SuccessSkuDto;
import com.xd.pre.modules.px.vo.tmpvo.appstorevo.TepmDto;
import com.xd.pre.modules.px.weipinhui.service.WphService;
import com.xd.pre.modules.sys.domain.*;
import com.xd.pre.modules.sys.mapper.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.activemq.ScheduledMessage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.jms.Destination;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NewWeiXinPayUrl {

    @Autowired
    private TokenKeyService tokenKeyService;
    @Autowired
    private ProxyProductService proxyProductService;
    @Resource
    private JdPayOrderPostAddressMapper jdPayOrderPostAddressMapper;
    @Resource
    private JdAppStoreConfigMapper jdAppStoreConfigMapper;
    @Resource
    private JdOrderPtMapper jdOrderPtMapper;
    @Resource
    private JdCkMapper jdCkMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource
    private JdMchOrderMapper jdMchOrderMapper;
    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;
    @Resource
    private JdExKamiMapper jdExKamiMapper;

    @Autowired
    private ActivateService activateService;

    @Resource
    private JdLogMapper jdLogMapper;

    @Autowired
    @Lazy(value = true)
    private JdDjService jdDjService;

    @Autowired
    @Lazy(value = true)
    private DouYinService douYinService;

    @Autowired
    @Lazy(value = true)
    private MenDianService menDianService;

    @Autowired
    @Lazy(value = true)
    private WphService wphService;

    @Autowired
    @Lazy(value = true)
    private PcAppStoreService pcAppStoreService;

    @Autowired
    @Lazy(value = true)
    private DouyinService douyinService;

    @Autowired
    @Lazy(value = true)
    private DouYinHuaDanService douYinHuaDanService;

    @Autowired
    @Lazy(value = true)
    private YongHuiService yongHuiService;


    public R match(JdMchOrder jdMchOrder) {
        try {
            JdAppStoreConfig jdAppStoreConfig = jdAppStoreConfigMapper.selectOne(Wrappers.<JdAppStoreConfig>lambdaQuery()
                    .eq(JdAppStoreConfig::getSkuId, jdMchOrder.getSkuId()).eq(JdAppStoreConfig::getGroupNum, Integer.valueOf(jdMchOrder.getPassCode())));
            Integer userAgentType = null;
            List<JdLog> jdLogs = jdLogMapper.selectList(Wrappers.<JdLog>lambdaQuery().eq(JdLog::getOrderId, jdMchOrder.getTradeNo()));
            if (CollUtil.isNotEmpty(jdLogs)) {
                log.error("当前订单出现了日志的订单");
                String userAgent = jdLogs.get(0).getUserAgent().toLowerCase();
                if (userAgent.contains("iphone")) {
                    userAgentType = PreConstant.IOS;
                } else {
                    userAgentType = PreConstant.安卓;
                }
            }
            if (CollUtil.isEmpty(jdLogs)) {
                log.error("当前订单出现没有日志");
                return null;
            }
            if (userAgentType == PreConstant.IOS && jdAppStoreConfig.getGroupNum() == PreConstant.ONE) {
                boolean b = mathIos(jdMchOrder);
                if (b) {
                    redisTemplate.opsForValue().set("匹配锁定成功:" + jdMchOrder.getTradeNo(), JSON.toJSONString(jdMchOrder), jdAppStoreConfig.getExpireTime(), TimeUnit.MINUTES);
                    return R.ok(jdMchOrder);
                }
            }
            if (jdAppStoreConfig.getGroupNum() == PreConstant.THREE) {
                return jdDjService.match(jdMchOrder, jdAppStoreConfig, jdLogs.get(0));
            }
            if (jdAppStoreConfig.getGroupNum() == PreConstant.FOUR) {
                return douYinService.match(jdMchOrder, jdAppStoreConfig, jdLogs.get(0));
            }
            if (jdAppStoreConfig.getGroupNum() == PreConstant.FIVE) {
                return menDianService.match(jdMchOrder, jdAppStoreConfig, jdLogs.get(0));
            }

            if (jdAppStoreConfig.getGroupNum() == PreConstant.SIX) {
                return wphService.match(jdMchOrder, jdAppStoreConfig, jdLogs.get(0));
            }
            if (jdAppStoreConfig.getGroupNum() == PreConstant.SEVEN) {
                return pcAppStoreService.match(jdMchOrder, jdAppStoreConfig, jdLogs.get(0));
            }
            if (jdAppStoreConfig.getGroupNum() == PreConstant.EIGHT) {
                return douyinService.match(jdMchOrder, jdAppStoreConfig, jdLogs.get(0));
            }
            if (jdAppStoreConfig.getGroupNum() == PreConstant.NINE) {
                return douYinHuaDanService.match(jdMchOrder, jdAppStoreConfig, jdLogs.get(0));
            }
            if (jdAppStoreConfig.getGroupNum() == PreConstant.NINE) {
                return douYinHuaDanService.match(jdMchOrder, jdAppStoreConfig, jdLogs.get(0));
            }
            if (jdAppStoreConfig.getGroupNum() == PreConstant.TEN) {
                return yongHuiService.match(jdMchOrder, jdAppStoreConfig, jdLogs.get(0));
            }

            if (userAgentType == PreConstant.安卓 || jdAppStoreConfig.getGroupNum() == PreConstant.TWO) {
                return matchAndroid(jdMchOrder, jdAppStoreConfig);
            }
        } catch (Exception e) {
            log.error("当前报错。msg:{}", e.getStackTrace());
        }
        return null;
    }

    /**
     * 匹配安卓
     *
     * @param jdMchOrder
     * @param jdAppStoreConfig
     * @return
     */
    private R matchAndroid(JdMchOrder jdMchOrder, JdAppStoreConfig jdAppStoreConfig) {

        TimeInterval timer = DateUtil.timer();
        jdMchOrder = jdMchOrderMapper.selectById(jdMchOrder.getId());
        LambdaQueryWrapper<JdOrderPt> wrapper = Wrappers.<JdOrderPt>lambdaQuery().
                eq(JdOrderPt::getSkuPrice, jdMchOrder.getMoney())
                .gt(JdOrderPt::getExpireTime, new Date())
                .eq(JdOrderPt::getIsWxSuccess, PreConstant.ONE)
                .eq(JdOrderPt::getIsMatch, PreConstant.ZERO);
        Set<String> keys = redisTemplate.keys("JD匹配锁定:*");
        if (CollUtil.isNotEmpty(keys)) {
            List<String> orderIds = keys.stream().map(it -> it.split(":")[1]).collect(Collectors.toList());
            wrapper.notIn(JdOrderPt::getOrderId, orderIds);
        }
       /* Set<String> readyData = redisTemplate.keys("订单管理微信链接:*");
        if (CollUtil.isNotEmpty(readyData)) {
            List<String> orderIds = readyData.stream().map(it -> it.split(":")[1]).collect(Collectors.toList());
            wrapper.in(JdOrderPt::getOrderId, orderIds);
        }*/
        Integer count = jdOrderPtMapper.selectCount(wrapper);
        if (count <= 5) {
            log.error("当前订单太小。不匹配");
            return null;
        }
        int i = PreUtils.randomCommon(1, count - 1, 1)[0];
        Page<JdOrderPt> jdOrderPtPage = new Page<>(i, 1);
        jdOrderPtPage = jdOrderPtMapper.selectPage(jdOrderPtPage, wrapper);
        JdOrderPt jdOrderPt = jdOrderPtPage.getRecords().get(0);
        String JDLock = redisTemplate.opsForValue().get("JD匹配锁定:" + jdOrderPt.getOrderId());
//            JdOrderPt jdOrderPt = jdOrderPtMapper.selectById(73366);
        log.info("订单匹配成功，开始获取微信链接msg:{}" + timer.interval());
        JdCk jdCkDb = jdCkMapper.selectOne(Wrappers.<JdCk>lambdaQuery().eq(JdCk::getPtPin, PreUtils.get_pt_pin(URLDecoder.decode(jdOrderPt.getPtPin()))));
        if (ObjectUtil.isNull(jdCkDb)) {
            jdOrderPt.setIsWxSuccess(0);
            jdOrderPt.setFailTime(200);
            this.jdOrderPtMapper.updateById(jdOrderPt);
            return null;
        }
        String arIpStr = redisTemplate.opsForValue().get("用户IP对应数据库的IP:" + jdMchOrder.getTradeNo());
        log.info("当前用户对应的ipmsg:{}", arIpStr);
        JdProxyIpPort oneIp = getJdProxyIpPort(null);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
        if (StrUtil.isNotBlank(JDLock)) {
            log.info("当前京东订单已经被锁定 msg:{}", timer.interval());
            return null;
        }
        redisTemplate.opsForValue().set("JD匹配锁定:" + jdOrderPt.getOrderId(), JSON.toJSONString(jdOrderPt), 5, TimeUnit.MINUTES);
        jdOrderPt.setWxPayExpireTime(DateUtil.offsetMinute(new Date(), jdAppStoreConfig.getPayIdExpireTime()));
        jdMchOrder.setOriginalTradeId(jdOrderPt.getId());
        jdMchOrder.setOriginalTradeNo(jdOrderPt.getOrderId());
        jdOrderPt.setIp(oneIp.getIp());
        jdOrderPt.setPort(oneIp.getPort());
        jdOrderPt.setIsMatch(PreConstant.ONE);
        log.info("匹配成功msg:{}", jdMchOrder);
        log.info("添加数据库关系,添加完成了，获取tokenkey的值，然后拼接到待支付页面msg:{}", timer.interval());
        //调用检查
        if (ObjectUtil.isNotNull(jdCkDb) && jdCkDb.getCk().contains("wskey=")) {
//            R consumption = activateService.consumption(jdOrderPt, true, oneIp);
//            if (consumption.getCode() != HttpStatus.HTTP_OK) {
//                log.info("当前订单可以重置一下msg:{}", timer.interval());
//                this.activateService.reSet(jdOrderPt, oneIp, null);
//                return null;
//            }
            log.info("检查订单是否有支付能力");
            String payId = "";
            if (jdAppStoreConfig.getProductNum() == PreConstant.ONE) {
                payId = getPayId(jdOrderPt.getOrgAppCk(), jdOrderPt.getOrderId(), proxy, jdOrderPt.getSkuPrice().intValue() + ".00");
            }
            if (jdAppStoreConfig.getGroupNum() == PreConstant.TWO) {
//                payId = getPayIdMeiTuan(mck, orderId, proxy, jdAppStoreConfigProduct);
                payId = getPayIdMeiTuan(jdOrderPt.getCurrentCk(), jdOrderPt.getOrderId(), null, jdAppStoreConfig);
            }
            if (StrUtil.isBlank(payId)) {
                log.info("获取订单payId失败msg:tradeNo{},orderId;{}", jdMchOrder.getTradeNo(), jdOrderPt.getOrderId());
                return null;
            }
            Boolean check = check(payId, jdOrderPt.getCurrentCk(), proxy);
            if (!check) {
                log.info("检查未通过msg:tradeNo{},orderId;{}", jdMchOrder.getTradeNo(), jdOrderPt.getOrderId());
                return null;
            }
            log.info("检查通过msg:tradeNo{},orderId;{}", jdMchOrder.getTradeNo(), jdOrderPt.getOrderId());
            String payUrl = payUrlMatchMeiTuan(payId, jdOrderPt.getCurrentCk(), proxy);
            if (StrUtil.isBlank(payUrl)) {
                log.info("当前订单获取微信链接失败，重新匹配msg;{},{}", jdMchOrder.getTradeNo(), jdOrderPt.getOrderId());
                return null;
            }
        }
        if (!buildHrefRed(jdOrderPt, oneIp, jdAppStoreConfig)) {
            return null;
        }
        log.info("重置其他订单开始msg:{}", timer.interval());
//        resetOrderPt(oneIp);
        log.info("重置其他订单完成msg:{}", timer.interval());
        JdOrderPt jdOrderPtCheck = this.jdOrderPtMapper.selectById(jdOrderPt.getId());
        if (jdOrderPtCheck.getIsMatch() == 1) {
            redisTemplate.opsForValue().set("匹配锁定成功:" + jdMchOrder.getTradeNo(), JSON.toJSONString(jdMchOrder), jdAppStoreConfig.getExpireTime(), TimeUnit.MINUTES);
            return null;
        }
        log.info("订单锁定成功msg:{},orderId:{}", timer.interval(), jdMchOrder.getTradeNo());
        redisTemplate.opsForValue().set("匹配锁定成功:" + jdMchOrder.getTradeNo(), JSON.toJSONString(jdMchOrder), jdAppStoreConfig.getExpireTime(), TimeUnit.MINUTES);
        long l = (System.currentTimeMillis() - jdMchOrder.getCreateTime().getTime()) / 1000;
        jdMchOrder.setMatchTime(l);
        jdMchOrderMapper.updateById(jdMchOrder);
        jdOrderPtMapper.updateById(jdOrderPt);
        return R.ok(jdMchOrder);
    }

    public String payUrlMatchMeiTuan(String payId, String mck, Proxy proxy) {
        try {
            String pin = PreUtils.get_pt_pin(mck);
            String bodyData = String.format("{\"source\":\"mcashier\",\"origin\":\"h5\",\"page\":\"pay\",\"mcashierTraceId\":1653762486838,\"appId\":\"jd_m_pay\",\"payId\":\"%s\",\"eid\":\"2XRK4PH7YTECS7DYZNDH764SWHELI2J2COCDRU357GLIV6TKL63PRAESJZVNTNB53M6BZABAON74E2QQEOCZO745CY\"}", payId);
            RequestBody requestBody = new FormBody.Builder()
                    .add("body", bodyData)
                    .build();
            OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy).build();
            Request request = new Request.Builder()
                    .url("https://api.m.jd.com/client.action?functionId=platWapWXPay&appid=mcashier")
                    .post(requestBody)
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .addHeader("cookie", mck)
                    .addHeader("Origin", "https://pay.m.jd.com")
                    .addHeader("User-Agent", "jdapp;android;9.4.4;10;7316266326835303-1663034366462326;network/wifi;model/PACT00;addressid/0;aid/7ab6b850a604fd2b;oaid/;osVer/29;appBuild/87076;psn/m 0Ddoh86M2Rp emACf77VJZ2BYiaC7o|91;psq/1;adk/;ads/;pap/JA2015_311210|9.4.4|ANDROID 10;osv/10;pv/34.23;installationId/5315bb3ac03f4341bde696c7fb2aaf28;jdv/0|kong|t_1000440933_|jingfen|efde4563d64c4e18ba131fd2e011f050|1653590049;ref/com.jd.lib.ordercenter.mygoodsorderlist.view.activity.MyOrderListActivity;partner/lc031;apprpd/OrderCenter_List;eufv/1;jdSupportDarkMode/0;hasUPPay/1;hasOCPay/0;supportHuaweiPay/0;supportBestPay/0;Mozilla/5.0 (Linux; Android 10; PACT00 Build/QP1A.190711.020; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/89.0.4389.72 MQQBrowser/6.2 TBS/046010 Mobile Safari/537.36")
                    .addHeader("Referer", "https://pay.m.jd.com/cpay/newPay-index.html?appId=jd_android_app4&needLoginSwitch=1&payId=5dbe72b4a9e643099019616912583543&sid=0fe98f15efa19a8d714fe2330bb7de7w&un_area=22_1930_0_0")
                    .build();
            Response response = client.newCall(request).execute();
            String returnStr = response.body().string();
            log.info("response:{},payId:{}", returnStr, payId);
            if (returnStr.contains("wx.tenpay.com")) {
                return returnStr;
            }
            if (StrUtil.isNotBlank(returnStr)) {
                if (returnStr.contains("当前支付方式不可用") || returnStr.contains("请更换其他支付方式")) {
                    log.info("删除不可以用的订单msg:{}", pin);
                    LambdaQueryWrapper<JdOrderPt> wrapper = Wrappers.<JdOrderPt>lambdaQuery()
                            .eq(JdOrderPt::getPtPin, pin)
                            .gt(JdOrderPt::getExpireTime, new Date())
                            .eq(JdOrderPt::getIsWxSuccess, PreConstant.ONE)
                            .isNull(JdOrderPt::getCardNumber);
                    List<JdOrderPt> deleteJdOrderPts = this.jdOrderPtMapper.selectList(wrapper);
                    if (CollUtil.isNotEmpty(deleteJdOrderPts)) {
                        for (JdOrderPt deleteJdOrderPt : deleteJdOrderPts) {
                            if (deleteJdOrderPt.getIsWxSuccess() == PreConstant.ZERO) {
                                continue;
                            }
                            deleteJdOrderPt.setFailTime(1200);
                            deleteJdOrderPt.setIsWxSuccess(PreConstant.ZERO);
                            jdOrderPtMapper.updateById(deleteJdOrderPt);
                        }
                    }
                }
            }

            return null;
        } catch (Exception e) {
            log.error("获取微信链接报错");
        }
        return null;
    }


    private boolean mathIos(JdMchOrder jdMchOrder) {
        try {
            String redisKeySuf = "";
            log.info("查询订单价格");
            if (ObjectUtil.isNotNull(jdMchOrder.getMoney())) {
                BigDecimal bigDecimal = new BigDecimal(jdMchOrder.getMoney());
                if (bigDecimal.intValue() == PreConstant.HUNDRED) {
                    redisKeySuf = PreConstant.IOS订单_100;
                }
                if (bigDecimal.intValue() == PreConstant.HUNDRED_2) {
                    redisKeySuf = PreConstant.IOS订单_200;
                }
            }
            if (StrUtil.isBlank(redisKeySuf)) {
                return false;
            }
            //找个订单不退款的账号。并且包含订单的
            log.info("当前匹配是IOS");
            Set<String> iosOrders = redisTemplate.keys(redisKeySuf + "*");
            if (iosOrders.size() <= 10) {
                BigDecimal bigDecimal = new BigDecimal(jdMchOrder.getMoney());
                if (bigDecimal.intValue() == PreConstant.HUNDRED) {
                    JdAppStoreConfig jdAppStoreConfig = this.jdAppStoreConfigMapper.selectOne(Wrappers.<JdAppStoreConfig>lambdaQuery().eq(JdAppStoreConfig::getSkuPrice, new BigDecimal(PreConstant.HUNDRED)));
                    log.info("通知生产100的IOS订单");
                    activateService.productIosOrderMq(jdAppStoreConfig.getSkuId());
                }
                if (bigDecimal.intValue() == PreConstant.HUNDRED_2) {
                    log.info("通知生产200的IOS订单");
                    JdAppStoreConfig jdAppStoreConfig = this.jdAppStoreConfigMapper.selectOne(Wrappers.<JdAppStoreConfig>lambdaQuery().eq(JdAppStoreConfig::getSkuPrice, new BigDecimal(PreConstant.HUNDRED_2)));
                    activateService.productIosOrderMq(jdAppStoreConfig.getSkuId());
                }
            }
            if (CollUtil.isNotEmpty(iosOrders)) {
                List<String> orderIds = iosOrders.stream().map(it -> it.split(":")[2]).collect(Collectors.toList());
                //随机获取订单匹配
                int[] ints = PreUtils.randomCommon(0, orderIds.size() - 1, 5);
                List<String> randomList = new ArrayList<>();
                for (int i = 0; i < ints.length; i++) {
                    String orderId = orderIds.get(ints[i]);
                    randomList.add(orderId);
                }
                orderIds = randomList;
                for (String orderId : orderIds) {
                    String JDLock = redisTemplate.opsForValue().get("JD匹配锁定:" + orderId);
                    if (StrUtil.isNotBlank(JDLock)) {
                        continue;
                    } else {
                        redisTemplate.opsForValue().set("JD匹配锁定:" + orderId, orderId, 5, TimeUnit.MINUTES);
                        String iOSMatchDtoStr = redisTemplate.opsForValue().get(redisKeySuf + orderId);
                        JdOrderPt iosMatchDto = JSON.parseObject(iOSMatchDtoStr, JdOrderPt.class);
                        long between = DateUtil.between(iosMatchDto.getCreateTime(), new Date(), DateUnit.HOUR);
                        if (between >= 10) {
                            redisTemplate.delete(redisKeySuf + orderId);
                            continue;
                        }
                        log.info("当前订单匹配的数据为msg:{}", iosMatchDto);
                        JdOrderPt jdOrderPt = jdOrderPtMapper.selectOne(Wrappers.<JdOrderPt>lambdaQuery().eq(JdOrderPt::getOrderId, orderId));
                        if (ObjectUtil.isNull(jdOrderPt)) {
                            this.jdOrderPtMapper.insert(iosMatchDto);
                        } else {
                            iosMatchDto = jdOrderPt;
                        }
                        log.info("更新IoSOrderPt");
                        log.info("更新jdMchOrder:{}", jdMchOrder.getTradeNo());
                        JdProxyIpPort oneIp = getJdProxyIpPort(null);
                        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
                        String payId = getPayId(iosMatchDto.getOrgAppCk(), iosMatchDto.getOrderId(), proxy, iosMatchDto.getSkuPrice().intValue() + ".00");
                        if (StrUtil.isBlank(payId)) {
//                            String iOSMatchDtoStr = redisTemplate.opsForValue().get(redisKeySuf + orderId);
                            redisTemplate.delete(redisKeySuf + orderId);
                            return false;
                        }
                        TokenKeyVo build = TokenKeyVo.builder().cookie(iosMatchDto.getOrgAppCk()).build();
                        TokenKeyResVo tokenKey = tokenKeyService.getTokenKey(build, oneIp, "");
                        if (ObjectUtil.isNull(tokenKey)) {
                            redisTemplate.delete(redisKeySuf + orderId);
                            return false;
                        }
                        String mck = getMck(proxy, tokenKey.getTokenKey());
                        Boolean check = check(payId, mck, proxy);
                        //redisTemplate.opsForValue().set("订单支付已超时1分钟:", payId);
                        String payIdTimeOut = redisTemplate.opsForValue().get("订单支付已超时1分钟:" + payId);
                        if (StrUtil.isNotBlank(payIdTimeOut)) {
                            payId = getPayId(iosMatchDto.getOrgAppCk(), iosMatchDto.getOrderId(), proxy, iosMatchDto.getSkuPrice().intValue() + ".00");
                        }
                        if (!check) {
                            mck = getMck(proxy, tokenKey.getTokenKey());
                            check(payId, mck, proxy);
                        }
                        String payData = payUrlIos(proxy, payId);
                        if (StrUtil.isBlank(payData)) {
                            redisTemplate.delete(redisKeySuf + orderId);
                            return false;
                        }
                        String hrefUrl = PreUtils.jumpIosHrefUrl(payData);
                        log.info("组装IOS的订单信息");
                        long l = (System.currentTimeMillis() - jdMchOrder.getCreateTime().getTime()) / 1000;
                        jdMchOrder.setOriginalTradeId(iosMatchDto.getId());
                        jdMchOrder.setMatchTime(l);
                        jdMchOrder.setOriginalTradeNo(iosMatchDto.getOrderId());
                        //========
                        JdAppStoreConfig jdAppStoreConfig = jdAppStoreConfigMapper.selectOne(Wrappers.<JdAppStoreConfig>lambdaQuery().eq(JdAppStoreConfig::getSkuId, iosMatchDto.getSkuId()));
                        iosMatchDto.setWxPayExpireTime(DateUtil.offsetMinute(new Date(), jdAppStoreConfig.getPayIdExpireTime()));
                        iosMatchDto.setIsMatch(PreConstant.ONE);
                        iosMatchDto.setPrerId(payId);
                        iosMatchDto.setCurrentCk(mck);
                        iosMatchDto.setHrefUrl(hrefUrl);
                        iosMatchDto.setWeixinUrl(URLDecoder.decode(hrefUrl));
                        jdMchOrderMapper.updateById(jdMchOrder);
                        jdOrderPtMapper.updateById(iosMatchDto);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.error("IOS匹配报错msg:{}", e);
        }
        return false;
    }

    private void resetOrderPt(JdProxyIpPort oneIp) {
        try {
            log.debug("随机调3个重置一下");
            LambdaQueryWrapper<JdOrderPt> resetCountWrapper = Wrappers.<JdOrderPt>lambdaQuery()
                    .ge(JdOrderPt::getExpireTime, new Date()).gt(JdOrderPt::getFailTime, PreConstant.TWO)
                    .le(JdOrderPt::getFailTime, PreConstant.TEN)
                    .le(JdOrderPt::getRetryTime, PreConstant.TEN);
            Integer resetCount = this.jdOrderPtMapper.selectCount(resetCountWrapper);
            if (resetCount <= 10) {
                return;
            }
            int[] ints = PreUtils.randomCommon(1, resetCount / 5, 1);
            Page<JdOrderPt> page = this.jdOrderPtMapper.selectPage(new Page<JdOrderPt>(ints[0], 5), resetCountWrapper);
            if (CollUtil.isNotEmpty(page.getRecords())) {
                List<JdOrderPt> records = page.getRecords();
                for (JdOrderPt record : records) {
//                    this.activateService.reSet(record, oneIp, null);
                }
            }

        } catch (Exception e) {
            log.error("订单号重置报错，msg:{}", e.getMessage());
        }
    }

    public JdProxyIpPort getJdProxyIpPort(JdProxyIpPort oneIp) {
        try {
            if (ObjectUtil.isNull(oneIp)) {
                oneIp = proxyProductService.getOneIp(1, 0, false);
            }
            if (ObjectUtil.isNull(oneIp)) {
                oneIp = proxyProductService.getOneIp(1, 0, false);
            }
            if (ObjectUtil.isNull(oneIp)) {
                oneIp = proxyProductService.getOneIp(1, 0, false);
            }
        } catch (Exception e) {
            log.error("代理获取失败msg:{}", e.getMessage());
        }
        return oneIp;
    }


    private boolean buildHrefRed(JdOrderPt jdOrderPt, JdProxyIpPort oneIp, JdAppStoreConfig jdAppStoreConfig) {
        try {
            JdCk jdCk = this.jdCkMapper.selectOne(Wrappers.<JdCk>lambdaQuery().eq(JdCk::getPtPin, jdOrderPt.getPtPin()));
            TokenKeyVo build = TokenKeyVo.builder().cookie(jdCk.getCk().trim()).build();
            TokenKeyResVo tokenKeyVO = tokenKeyService.getTokenKey(build, oneIp, jdCk.getFileName());
            if (ObjectUtil.isNull(tokenKeyVO) || StrUtil.isBlank(tokenKeyVO.getTokenKey())) {
                oneIp = getJdProxyIpPort(null);
                tokenKeyVO = tokenKeyService.getTokenKey(build, oneIp, jdCk.getFileName());
            }
            if (ObjectUtil.isNull(tokenKeyVO) || StrUtil.isBlank(tokenKeyVO.getTokenKey())) {
                oneIp = getJdProxyIpPort(null);
                tokenKeyVO = tokenKeyService.getTokenKey(build, oneIp, jdCk.getFileName());
            }
            if (ObjectUtil.isNull(tokenKeyVO) || StrUtil.isBlank(tokenKeyVO.getTokenKey())) {
                log.info("当前是m端的ck,只能直接拉起");
                String href = getcheckweb(jdOrderPt);
                jdOrderPt.setHrefUrl(URLEncoder.encode(href));
                jdOrderPt.setWxPayUrl(URLEncoder.encode(href));
                jdOrderPt.setWeixinUrl(href);
                return false;
            } else {
                String tokenKey = tokenKeyVO.getTokenKey();
                if (jdAppStoreConfig.getGroupNum() == PreConstant.ONE) {
                    String href = "https://un.m.jd.com/cgi-bin/app/appjmp?tokenKey=" + tokenKey + "&to=https%3A%2F%2Fwqs.jd.com%2Forder%2Fn_detail_jdm.shtml%3Fdeal_id%3D" + jdOrderPt.getOrderId();
                    jdOrderPt.setHrefUrl(URLEncoder.encode(href));
                }
                if (jdAppStoreConfig.getGroupNum() == PreConstant.TWO) {
                    String href = "https://un.m.jd.com/cgi-bin/app/appjmp?tokenKey=" + tokenKey + "&to=https%3A%2F%2Fxmlya.m.jd.com%2FgetDetail%3ForderId%3D" + jdOrderPt.getOrderId();
                    jdOrderPt.setHrefUrl(URLEncoder.encode(href));
                }
//                String getcheckweb = getcheckweb(jdOrderPt);
//                jdOrderPt.setWxPayUrl(URLEncoder.encode(getcheckweb));
//                jdOrderPt.setWeixinUrl(URLEncoder.encode(getcheckweb));
                return true;
            }
        } catch (Exception e) {
            log.error("当前设置ck");
        }
        return false;
    }

    /**
     * 代理微信支付链接
     *
     * @param jdOrderPt
     * @return
     */
    private String getcheckweb(JdOrderPt jdOrderPt) {
        String checkmweb = redisTemplate.opsForValue().get("订单管理微信链接:" + jdOrderPt.getOrderId());
        UrlEntity urlEntity = PreUtils.parseUrl(checkmweb);
        String baseUrl = urlEntity.getBaseUrl();
        //http://192.168.2.149:7890/jd/proxy/cgi-bin/mmpayweb-bin/checkmweb?prepay_id=wx2820183236964376d85dfc8a7e12df0000&package=976073049&redirect_url=https%3A%2F%2Fpay.m.jd.com%2FwapWeiXinPay%2FweiXinH5PayQuery.action%3FappId%3Djd_m_pay%26payId%3D2b687b9a389944aa9e40da1d53059493
        String teln = redisTemplate.opsForValue().get("tenant:c815c971fdabbda20f8fdf7d0ee658ff");
        JSONObject parseObject = JSON.parseObject(teln);
        String urlPre = parseObject.getString("urlPre");
        String replace = baseUrl.replace("https://wx.tenpay.com", PreUtils.parseUrl(urlPre + "11").baseUrl + "/api/jd/proxy");
        String paramStr = urlEntity.getParamStr();
        return replace + "?" + paramStr;
    }


    private String weixinUrl(String mweb_url, Map<String, String> map, JdProxyIpPort oneIp) {
        try {
            if (ObjectUtil.isNull(mweb_url)) {
                return null;
            }
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
            OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy).build();
            Request.Builder header = new Request.Builder()
                    .url(mweb_url)
                    .get()
                    .addHeader("Referer", "https://pay.m.jd.com/");
            if (CollUtil.isNotEmpty(map)) {
                for (String key : map.keySet()) {
                    header.header(key, map.get(key));
                }
            }
            Request request = header.build();
            Response response = client.newCall(request).execute();
            String jingdonghtml = response.body().string();
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

        }
        return null;
    }


    public R findOrderStatus(JdMchOrder jdMchOrder) {
        try {
            Integer originalTradeId = jdMchOrder.getOriginalTradeId();
            JdOrderPt jdOrderPt = jdOrderPtMapper.selectById(originalTradeId);
            log.info("使用当前的ck，去获取订单信息");
            List<JdPayOrderPostAddress> jdPayOrderPostAddresses = jdPayOrderPostAddressMapper.selectList(Wrappers.emptyWrapper());
            Map<Integer, JdPayOrderPostAddress> stepsMap = jdPayOrderPostAddresses.stream().collect(Collectors.toMap(it -> it.getStep(), it -> it));
            JdPayOrderPostAddress jdPayOrderPostAddress_8 = stepsMap.get(8);
            ArrayList<SuccessSkuDto> successSkuDtos = getSuccess(jdOrderPt, jdPayOrderPostAddress_8);
            if (CollUtil.isEmpty(successSkuDtos)) {
                return null;
            }
            log.info("获取到成功订单msg:{}", successSkuDtos);
            log.info("开始获取订单详情页面");
            List<SuccessSkuDto> successSkuDtosFilter = successSkuDtos.stream().filter(it -> it.getOrderId().equals(jdMchOrder.getOriginalTradeNo())).collect(Collectors.toList());
            if (CollUtil.isEmpty(successSkuDtosFilter)) {
                return null;
            }
            for (SuccessSkuDto successSkuDto : successSkuDtosFilter) {
                JdPayOrderPostAddress jdPayOrderPostAddress_10 = stepsMap.get(10);
                HttpRequest orderinfoHttp = HttpRequest.get(String.format(jdPayOrderPostAddress_10.getUrl(), successSkuDto.getOrderId()));
                orderinfoHttp.header("cookie", jdOrderPt.getCurrentCk());
                orderinfoHttp.header("referer", jdPayOrderPostAddress_10.getReferer());
                orderinfoHttp.header("user-agent", jdPayOrderPostAddress_10.getUserAgent());
                String body = orderinfoHttp.execute().body();
                Object orderCompleteTimeObj = JSON.parseObject(body).get("orderCompleteTime");
                if (ObjectUtil.isNotNull(orderCompleteTimeObj) && StrUtil.isNotBlank(orderCompleteTimeObj.toString())) {
                    DateTime orderCompleteTime = DateUtil.parse(orderCompleteTimeObj.toString(), "yyyy-MM-dd HH:mm:ss");
                    if (orderCompleteTime.getTime() > DateUtil.offsetMinute(jdMchOrder.getCreateTime(), 20).getTime()) {
                        log.info("不是当前这个订单支付完成的数据");
                        continue;
                    }
                    if (!successOrderData(jdMchOrder, originalTradeId, jdOrderPt, successSkuDto)) {
                        continue;
                    }
                    R cartNumAndMy = this.getCartNumAndMy(jdMchOrder);
                    Integer errCode = deleteOrderId(jdOrderPt, stepsMap);
                    if (errCode == 0) {
                        jdOrderPt.setPaySuccessTime(orderCompleteTime);
                        jdOrderPtMapper.updateById(jdOrderPt);
                        return cartNumAndMy;
                    }
                }
            }
            return R.ok();
        } catch (Exception e) {
            log.info("获取支付状态失败，msg:{}", e.getMessage());
        }
        return null;

    }

    public Integer deleteOrderId(JdOrderPt jdOrderPt, Map<Integer, JdPayOrderPostAddress> stepsMap) {
        try {
            log.info("删除订单");
            JdPayOrderPostAddress jdPayOrderPostAddress_9 = stepsMap.get(9);
            HttpRequest lastPageReq = HttpRequest.get(String.format(jdPayOrderPostAddress_9.getUrl(), jdOrderPt.getOrderId()));
            lastPageReq.header("cookie", jdOrderPt.getCurrentCk());
            lastPageReq.header("referer", jdPayOrderPostAddress_9.getReferer());
            HttpResponse execute = lastPageReq.execute();
            return Integer.valueOf(JSON.parseObject(execute.body()).get("errCode").toString());
        } catch (Exception e) {
            log.error("删除订单报错，msg:{}", e.getMessage());
        }
        return null;
    }

    private boolean successOrderData(JdMchOrder jdMchOrder, Integer originalTradeId, JdOrderPt jdOrderPt, SuccessSkuDto successSkuDto) {
        try {
            String matchLockOk = redisTemplate.opsForValue().get("锁定账号:" + jdOrderPt.getPtPin());
            log.info("当前订单成功。不管要不要。请去查看，skuId:{}", successSkuDto.getSkuName());
            log.info("数据库肯定有个最新的一条。是这个返回的数据msg:{}", matchLockOk);
            log.info("这条数据状态");
            JdMchOrder jdMchOrderThisGet = jdMchOrderMapper.selectOne(Wrappers.<JdMchOrder>lambdaQuery().eq(JdMchOrder::getOriginalTradeId, originalTradeId));
            jdMchOrder.setOriginalTradeNo(successSkuDto.getOrderId());
            jdMchOrderMapper.updateById(jdMchOrderThisGet);
            log.info("当前订单支付成功++++++++++++,{}", jdMchOrderThisGet);
            jdOrderPt.setPaySuccessTime(new Date());
            jdOrderPt.setOrderId(successSkuDto.getOrderId());
            jdOrderPtMapper.updateById(jdOrderPt);
            return true;
        } catch (Exception e) {
            log.error("获取订单错误msg:{}", e.getMessage());
        }
        return false;
    }

    private ArrayList<SuccessSkuDto> getSuccess(JdOrderPt jdOrderPt, JdPayOrderPostAddress jdPayOrderPostAddress_8) {
        try {
            ArrayList<SuccessSkuDto> successSkuDtos = new ArrayList<>();
            log.info("查询最后一次分页");
            HttpRequest lastPageReq = HttpRequest.get(jdPayOrderPostAddress_8.getUrl());
            lastPageReq.header("cookie", jdOrderPt.getCurrentCk());
            lastPageReq.header("referer", jdPayOrderPostAddress_8.getReferer());
            HttpResponse execute = lastPageReq.execute();
            String body = execute.body();
            JSONObject parseObject = JSON.parseObject(body);
            JSONArray orderList = JSON.parseArray(parseObject.get("orderList").toString());
            for (Object o : orderList) {
                JSONObject orderListOne = JSON.parseObject(o.toString());
                JSONObject stateInfo = JSON.parseObject(orderListOne.get("stateInfo").toString());
                JSONObject productListOne = JSON.parseArray(orderListOne.get("productList").toString(), JSONObject.class).get(0);
                if (!Integer.valueOf(stateInfo.get("stateCode").toString()).equals(18)) {
                    continue;
                }
                String skuId = jdOrderPt.getSkuId();
                if (!productListOne.get("skuId").toString().equals(skuId)) {
                    continue;
                }
                String orderId = orderListOne.get("orderId").toString();
                SuccessSkuDto successSkuDto = SuccessSkuDto.builder().skuId(skuId).orderId(orderId).skuName(jdOrderPt.getSkuName()).build();
                successSkuDtos.add(successSkuDto);
            }
            return successSkuDtos;
        } catch (Exception e) {
            log.info("查询最后一次分页失败,msg:{}", e.getMessage());
        }
        return null;
    }

    public R getCartNumAndMy(JdMchOrder jdMchOrder) {
        try {
            String orderPtId = jdMchOrder.getOriginalTradeNo();
            log.info("获取卡密详情数据的OrderId msg:{}", orderPtId);
            JdOrderPt jdOrderPt = jdOrderPtMapper.selectOne(Wrappers.<JdOrderPt>lambdaQuery().eq(JdOrderPt::getOrderId, orderPtId));
            if (ObjectUtil.isNull(jdOrderPt)) {
                return null;
            }
            JdAppStoreConfig jdAppStoreConfig = jdAppStoreConfigMapper.selectOne(Wrappers.<JdAppStoreConfig>lambdaQuery().eq(JdAppStoreConfig::getSkuId, jdOrderPt.getSkuId()));
            if (jdAppStoreConfig.getGroupNum() == PreConstant.THREE) {
                log.info("获取订单京东到家的支付详情支付详情");
                jdDjService.selectOrderStataus(jdOrderPt, jdMchOrder);
            }
            if (jdAppStoreConfig.getGroupNum() == PreConstant.FIVE) {
                log.info("获取门店订单回调");
                menDianService.selectOrderStataus(jdOrderPt, jdMchOrder);
            }
            if (jdAppStoreConfig.getGroupNum() == PreConstant.SIX) {
                log.info("唯品会查询订单");
                wphService.selectOrderStataus(jdOrderPt, jdMchOrder);
            }
            if (jdAppStoreConfig.getGroupNum() == PreConstant.SEVEN) {
                log.info("唯品会查询订单");
                pcAppStoreService.selectOrderStataus(jdOrderPt, jdMchOrder);
            }
            if (jdAppStoreConfig.getGroupNum() == PreConstant.EIGHT) {
                String payLocalUrl = URLDecoder.decode(jdOrderPt.getHrefUrl().replace("alipays://platformapi/startapp?appId=20000067&url=", ""));
                String orderIdDb = PreUtils.parseUrl(payLocalUrl).getParams().get("orderId");
                log.info("订单号:{},抖音查询订单msg,校验是否是当前订单,payUrl:{}", jdMchOrder.getTradeNo(), payLocalUrl);
                if (jdMchOrder.getTradeNo().equals(orderIdDb)) {
                    log.info("订单号:{}，是当前订单准查询订单", orderIdDb);
                    douyinService.selectOrderStataus(jdOrderPt, jdMchOrder);
                } else {
                    log.info("订单号:{}订单查单已经过期,当前订单已经匹配跟另外的数据请看日志，支付地址msg：{}", jdMchOrder.getTradeNo(), jdOrderPt.getHrefUrl());
                    return null;
                }
            }
            if (jdAppStoreConfig.getGroupNum() == PreConstant.NINE) {
                String payLocalUrl = URLDecoder.decode(jdOrderPt.getHrefUrl().replace("alipays://platformapi/startapp?appId=20000067&url=", ""));
                String orderIdDb = PreUtils.parseUrl(payLocalUrl).getParams().get("orderId");
                log.info("订单号:{},抖音话单查询订单msg,校验是否是当前订单,payUrl:{}", jdMchOrder.getTradeNo(), payLocalUrl);
                if (jdMchOrder.getTradeNo().equals(orderIdDb)) {
                    log.info("订单号:{}，是当前订单准查询订单", orderIdDb);
                    douYinHuaDanService.selectOrderStataus(jdOrderPt, jdMchOrder);
                } else {
                    log.info("订单号:{}话单订单查单已经过期,当前订单已经匹配跟另外的数据请看日志，支付地址msg：{}", jdMchOrder.getTradeNo(), jdOrderPt.getHrefUrl());
                    return null;
                }
            }
            if (jdAppStoreConfig.getGroupNum() == PreConstant.TWO || jdAppStoreConfig.getGroupNum() == PreConstant.ONE) {
                List<JdPayOrderPostAddress> jdPayOrderPostAddresses = jdPayOrderPostAddressMapper.selectList(Wrappers.emptyWrapper());
                Map<Integer, JdPayOrderPostAddress> stepsMap = jdPayOrderPostAddresses.stream().collect(Collectors.toMap(it -> it.getStep(), it -> it));
                boolean kaMi = getKaMi(stepsMap, jdOrderPt, jdMchOrder.getId(), jdAppStoreConfig);
                return R.ok(kaMi);
            }
        } catch (Exception e) {
            log.info("查看卡密失败");
        }
        return null;
    }

    public static void main(String[] args) {
        HttpRequest httpRequest = HttpRequest.get(String.format("https://xmlya.m.jd.com/getDetail?orderId=%s", "244479334808"));
        httpRequest.header("cookie", "__jdv=122270672%7C192.168.2.149%3A7890%7C-%7Creferral%7C-%7C1653740495199; __jdc=122270672; mba_muid=16537404951981117646615; _gia_s_local_fingerprint=581dcf32a8bf3983134c7266e2fd44ae; _gia_s_e_joint={\"eid\":\"5H4EB2VPLLWZIHUAGADUUCKJPSQ5OC5ASDLN7ONNEGL335L52X2NQK56AWHKEFDXY5CNUB6US2JG627RXDSVGNW3NE\",\"ma\":\"\",\"im\":\"\",\"os\":\"Mac OS X (iPhone)\",\"ip\":\"183.221.18.112\",\"ia\":\"\",\"uu\":\"\",\"at\":\"6\"}; wxa_level=1; cid=9; retina=1; jxsid=16544768693133217486; appCode=ms0ca95114; webp=1; visitkey=31701223650894512; PPRD_P=UUID.16537404951981117646615; sc_width=390; shshshfp=8d9ef1593d41c0b1a8c4282de6e6888b; shshshfpa=9681e923-45a9-6e71-f7bc-07ab2134a222-1654476873; shshshfpb=fTLiwkKvNeMWjHl5Bq34Z8Q; share_cpin=; share_open_id=; share_gpin=; shareChannel=; source_module=; erp=; channel=; 3AB9D23F7A4B3C9B=5H4EB2VPLLWZIHUAGADUUCKJPSQ5OC5ASDLN7ONNEGL335L52X2NQK56AWHKEFDXY5CNUB6US2JG627RXDSVGNW3NE; __jda=122270672.16537404951981117646615.1653740495.1654493311.1654503618.6; pt_key=app_openAAJinbr1ADBhYsIHwkPtj_v49HHFft0f-SyWAm8RABDB9x7YD9qq3JaDQ_l7k_N_q-CzpzxzqLM; pt_pin=wdxyCloKMntxII; pwdt_id=wdxyCloKMntxII; sid=5e667d971b20abdfba34bc0628e9441w; autoOpenApp_downCloseDate_jd_homePage=1654504185942_1; wqmnx1=MDEyNjM5MXRzb2VtYyY1MSZlZzExNGxQVU8gIHAvIGxvbk1FcjNmZjVZSUZS; __wga=1654504191482.1654503620993.1654494709045.1654476873150.7.3; jxsid_s_u=https%3A//wqs.jd.com/order/orderlist_jdm.shtml; jxsid_s_t=1654504191533; shshshsID=2c5979f2a346f7b92b71e3f2669c052a_7_1654504191804; __jdb=122270672.23.16537404951981117646615|6.1654503618; mba_sid=16545036186249656737226144044.23");
        HttpResponse execute = httpRequest.execute();
        String body = execute.body();
        Document document = Jsoup.parse(body);
        Elements kwd = document.getElementsByClass("kwd");
        String zhanghao = ((Element) kwd.first().childNodes().get(1)).html();
        String mima = ((Element) kwd.last().childNodes().get(1)).html();
        log.info("账号:msg:{}", zhanghao);
        log.info("密码:msg:{}", mima);
    }

//    private boolean getKaMiMeituan(Map<Integer, JdPayOrderPostAddress> stepsMap, JdOrderPt jdOrderPt, Integer jdMachOrderId, JdMchOrder jdMchOrder) {
//        HttpRequest httpRequest = HttpRequest.get(String.format("https://xmlya.m.jd.com/getDetail?orderId=%s", jdOrderPt.getOrderId()));
//        httpRequest.header("cookie", jdOrderPt.getCurrentCk());
//        HttpResponse execute = httpRequest.execute();
//        String body = execute.body();
//        jdOrderPt.setHtml(body);
//        jdOrderPtMapper.updateById(jdOrderPt);
//        Document document = Jsoup.parse(body);
//        Elements kwd = document.getElementsByClass("kwd");
//        String zhanghao = ((Element) kwd.first().childNodes().get(1)).html();
//        String mima = ((Element) kwd.last().childNodes().get(1)).html();
//        log.info("账号:msg:{}", zhanghao);
//        log.info("密码:msg:{}", mima);
//        jdOrderPt.setCardNumber(zhanghao);
//        jdOrderPt.setCarMy(mima);
//        jdOrderPt.setPaySuccessTime(new Date());
//        return false;
//    }


    private boolean getKaMi(Map<Integer, JdPayOrderPostAddress> stepsMap, JdOrderPt jdOrderPt, Integer jdMachOrderId, JdAppStoreConfig jdAppStoreConfig) {
        try {
            HttpRequest httpRequest = null;
            if (jdAppStoreConfig.getGroupNum() == PreConstant.ONE) {
                JdPayOrderPostAddress jdPayOrderPostAddress_6 = stepsMap.get(6);
                httpRequest = HttpRequest.get(String.format(jdPayOrderPostAddress_6.getUrl(), jdOrderPt.getOrderId()));
            }
            if (jdAppStoreConfig.getGroupNum() == PreConstant.TWO) {
                JdPayOrderPostAddress jdPayOrderPostAddress_102 = stepsMap.get(102);
                httpRequest = HttpRequest.get(String.format(jdPayOrderPostAddress_102.getUrl(), jdOrderPt.getOrderId()));
            }
            httpRequest.header("cookie", jdOrderPt.getCurrentCk());
            HttpResponse execute = httpRequest.execute();
            String body = execute.body();
            jdOrderPt.setHtml(body);
            jdOrderPtMapper.updateById(jdOrderPt);
            Document document = Jsoup.parse(body);
            Elements kwd = document.getElementsByClass("kwd");
            String zhanghao = ((Element) kwd.first().childNodes().get(1)).html();
            String mima = ((Element) kwd.last().childNodes().get(1)).html();
            log.info("账号:msg:{}", zhanghao);
            log.info("密码:msg:{}", mima);
            jdOrderPt.setCardNumber(zhanghao);
            jdOrderPt.setCarMy(mima);
            jdOrderPt.setPaySuccessTime(new Date());
            if (ObjectUtil.isNull(jdMachOrderId)) {
                log.info("开始获取额外的卡密");
                JdExKami jdExKami = new JdExKami(zhanghao + "----" + mima);
                List<JdExKami> jdExKamis = jdExKamiMapper.selectList(Wrappers.<JdExKami>lambdaQuery().eq(JdExKami::getExKami, jdExKami));
                if (ObjectUtil.isNotNull(jdExKami)) {
                    log.info("已经存在这个卡密msg pt:msg:{}", jdOrderPt.getCurrentCk());
                    return false;
                }
                jdExKamiMapper.insert(jdExKami);
                return false;
            }
            JdMchOrder jdClientOrderDb = jdMchOrderMapper.selectById(jdMachOrderId);
            jdClientOrderDb.setStatus(PreConstant.TWO);
            jdClientOrderDb.setOriginalTradeNo(jdOrderPt.getOrderId());
            jdMchOrderMapper.updateById(jdClientOrderDb);
            log.info("支付成功回显状态");
            jdOrderPtMapper.updateById(jdOrderPt);
            log.info("删除订单");
            Boolean order_recycle_m = HMAC.emptyTrash(jdOrderPt.getOrderId(), jdOrderPt.getCurrentCk(), "order_recycle_m");
            if (order_recycle_m != true) {
                HMAC.emptyTrash(jdOrderPt.getOrderId(), jdOrderPt.getCurrentCk(), "order_recycle_m");
            }
            log.info("msg删除订单:{},orderId:{}", order_recycle_m, jdOrderPt.getOrderId());
//            this.deleteOrderId(jdOrderPt, stepsMap);
            log.info("删除微信支付链接的key");
            String pt_pin = PreUtils.get_pt_pin(jdOrderPt.getCurrentCk());
            redisTemplate.delete(PreConstant.当前CK已经有的订单 + pt_pin);
            return true;
        } catch (Exception e) {
            log.error("获取卡密错误");
        }
        return Boolean.FALSE;
    }

    public String getMck(Proxy proxy, String token) {
        try {
            String toenUrl = String.format(" https://un.m.jd.com/cgi-bin/app/appjmp?tokenKey=%s&to=https://gamerecg.m.jd.com?skuId=%s", token, "11183343342");
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            if (ObjectUtil.isNotNull(proxy)) {
                builder.proxy(proxy);
            }
            OkHttpClient client = builder.followRedirects(false).build();
            Request request = new Request.Builder()
                    .url(toenUrl)
                    .addHeader("User-Agent", "okhttp/3.12.1")
                    .build();
            Response execute1 = client.newCall(request).execute();
            List<String> headers = execute1.headers("Set-Cookie");
            StringBuilder stringBuilder = new StringBuilder();
            for (String header : headers) {
                if (StrUtil.isNotBlank(header) && (header.contains("pt_pin") || header.contains("pt_key"))) {
                    String[] split = header.split(";");
                    for (String s : split) {
                        if (s.contains("pt_pin") || s.contains("pt_key")) {
                            stringBuilder.append(s + ";");
                        }
                    }
                }
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            log.info("获取mck失败msg:{}", e.getMessage());
        }
        return null;
    }


    public List<JdOrderPt> checkCkAndMatch(String ck, String skuId, String tradeNo) {
        try {
            JdAppStoreConfig jdAppStoreConfigProduct = this.jdAppStoreConfigMapper.selectOne(Wrappers.<JdAppStoreConfig>lambdaQuery().eq(JdAppStoreConfig::getSkuId, skuId));
            String pt_pin = URLDecoder.decode(PreUtils.get_pt_pin(ck));
            //锁定当前账号4分钟
            redisTemplate.opsForValue().set(PreConstant.正在生产库存的CK + pt_pin, pt_pin, 4, TimeUnit.MINUTES);
            String redisOrderS = redisTemplate.opsForValue().get("账号关联订单编号:" + pt_pin);
            TokenKeyVo build = TokenKeyVo.builder().cookie(ck.trim()).build();
            JdProxyIpPort oneIp = proxyProductService.getOneIp(0, 0, false);
            TokenKeyResVo tokenKey = tokenKeyService.getTokenKey(build, oneIp, tradeNo);
            if (ObjectUtil.isNull(tokenKey)) {
                return null;
            }
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
            String mck = getMck(proxy, tokenKey.getTokenKey());
            log.info("mckMsg:{}", mck);
            //执行下单。
            //https://api.m.jd.com/client.action?functionId=submitGPOrder&clientVersion=%s&client=%s&uuid=%s&st=%s&sign=%s&sv=%s
            //{"appKey":"android","brandId":"999440","buyNum":1,"payMode":"0","rechargeversion":"10.9","skuId":"%s","totalPrice":"%s","type":1,"version":"1.10"}
            List<JdPayOrderPostAddress> jdPayOrderPostAddresses = jdPayOrderPostAddressMapper.selectList(Wrappers.emptyWrapper());
            Map<Integer, JdPayOrderPostAddress> stepsMap = jdPayOrderPostAddresses.stream().collect(Collectors.toMap(it -> it.getStep(), it -> it));
            JdPayOrderPostAddress jdPayOrderPostAddress_11 = stepsMap.get(11);
            String param = String.format(jdPayOrderPostAddress_11.getParam(), jdAppStoreConfigProduct.getSkuId(), jdAppStoreConfigProduct.getSkuPrice().intValue() * 100 + "");
            //执行签证。
//            {"result":{"orderId":247982124420,"paySuccessUrl":""},"code":"0"}
//            SignVoAndDto signVoAndDto = new SignVoAndDto("submitGPOrder", param);
//            signVoAndDto = JdSgin.newSign(signVoAndDto);
//            String url_11 = String.format(jdPayOrderPostAddress_11.getUrl(), signVoAndDto.getClientVersion(), signVoAndDto.getAndroidOrIos(),
//                    signVoAndDto.getUuid(), signVoAndDto.getSt(), signVoAndDto.getSt(), signVoAndDto.getSv());
//            log.debug("当前下单的地址为msg:{}", url_11);
            String orderId = null;
            if (jdAppStoreConfigProduct.getGroupNum() == PreConstant.ONE) {
                orderId = getOrderId(ck, proxy, jdAppStoreConfigProduct);
            }

            if (jdAppStoreConfigProduct.getGroupNum() == PreConstant.TWO) {
                orderId = getOrderIdMeiTuan(mck, proxy, jdAppStoreConfigProduct);
            }
            if (StrUtil.isBlank(orderId)) {
                return null;
            }
            if (StrUtil.isBlank(redisOrderS) && jdAppStoreConfigProduct.getGroupNum() == PreConstant.ONE) {
                redisTemplate.opsForValue().set("账号关联订单编号:" + pt_pin, JSON.toJSONString(Arrays.asList(orderId)), 12, TimeUnit.HOURS);
            } else if (StrUtil.isNotBlank(redisOrderS) && jdAppStoreConfigProduct.getGroupNum() == PreConstant.ONE) {
                List<String> orderList = JSON.parseArray(redisOrderS, String.class);
                if (orderList.size() >= 2) {
                    log.info("当前账号大于2了。不要再生产了.这单可以当成ios的拉起订单");
                    orderList.add(orderId);
                    redisTemplate.opsForValue().set("账号关联订单编号:" + pt_pin, JSON.toJSONString(orderList), 12, TimeUnit.HOURS);
                    return null;
                }
            }
            //当前账号没有下满2单。应该可以获取到微信链接
            //https://api.m.jd.com/client.action?functionId=genAppPayId&clientVersion=%s&client=%&uuid=%s&st=%s&sign=%s&sv=%s
            //执行获取payid
            String payId = null;
            if (jdAppStoreConfigProduct.getGroupNum() == PreConstant.ONE) {
                payId = getPayId(ck, orderId, proxy, jdAppStoreConfigProduct.getSkuPrice().intValue() + ".00");
            }
            if (jdAppStoreConfigProduct.getGroupNum() == PreConstant.TWO) {
                payId = getPayIdMeiTuan(mck, orderId, proxy, jdAppStoreConfigProduct);
            }
            if (StrUtil.isBlank(payId)) {
                return null;
            }
            //检查payid
            Boolean check = check(payId, mck, proxy);
            if (!check) {
                mck = getMck(proxy, tokenKey.getTokenKey());
                Boolean check1 = check(payId, mck, proxy);
                if (!check1) {
                    tokenKey = tokenKeyService.getTokenKey(build, oneIp, tradeNo);
                    mck = getMck(proxy, tokenKey.getTokenKey());
                    check(payId, mck, proxy);
                }
            }
            //获取微信链接
            String payUrl = payUrl(payId, mck, proxy, pt_pin, ck, PreConstant.ZERO, jdAppStoreConfigProduct.getGroupNum(), orderId, jdAppStoreConfigProduct);
            if (StrUtil.isBlank(payUrl) && jdAppStoreConfigProduct.getGroupNum() == PreConstant.ONE) {
                log.error("当前订单没有微信订单");
                log.info("查看当前是否有有ios订单存在的信息，如果存在。就拉起ios订单信息，放入ios信息");
                List<JdOrderPt> jdOrderPts = jdOrderPtMapper.selectList(Wrappers.<JdOrderPt>lambdaQuery().eq(JdOrderPt::getPtPin, pt_pin));
                if (CollUtil.isNotEmpty(jdOrderPts)) {
                    List<JdOrderPt> isPaySuccess = jdOrderPts.stream().filter(it -> StrUtil.isNotBlank(it.getCarMy())).collect(Collectors.toList());
                    if (CollUtil.isNotEmpty(isPaySuccess)) {
                        log.info("当前订单可以成为ios拉起订单,存放ios订单，获取ios订单信息，并且拉起来");
                        String payData = payUrlIos(proxy, payId);
                        if (StrUtil.isNotBlank(payData)) {
                            JdOrderPt ios = JdOrderPt.builder().orderId(orderId).ptPin(pt_pin).expireTime(DateUtil.offsetHour(new Date(), 12)).createTime(new Date())
                                    .skuPrice(jdAppStoreConfigProduct.getSkuPrice()).skuId(jdAppStoreConfigProduct.getSkuId()).skuName(jdAppStoreConfigProduct.getSkuName())
                                    .prerId(payId).isWxSuccess(PreConstant.ZERO).currentCk(mck).port(oneIp.getPort()).ip(oneIp.getIp()).orgAppCk(ck).isEnable(PreConstant.FIVE)
                                    .payData(payData)
                                    .build();
                            if (jdAppStoreConfigProduct.getSkuPrice().intValue() == PreConstant.HUNDRED) {
                                log.info("IOS订单存放完毕");
                                redisTemplate.opsForValue().set(PreConstant.IOS订单_100 + orderId, JSON.toJSONString(ios), 10, TimeUnit.HOURS);
                            }
                            if (jdAppStoreConfigProduct.getSkuPrice().intValue() == PreConstant.HUNDRED_2) {
                                log.info("IOS200订单存放完毕");
                                redisTemplate.opsForValue().set(PreConstant.IOS订单_200 + orderId, JSON.toJSONString(ios), 10, TimeUnit.HOURS);
                            }
                        }
                    }
                }
                return null;
            }
            if (StrUtil.isBlank(payUrl) && jdAppStoreConfigProduct.getGroupNum() == PreConstant.TWO) {
                log.error("当前美团没有获取到微信链接");
                return null;
            }
            log.info("当前订单有微信支付msg:{}", payUrl);
            redisTemplate.opsForValue().set(PreConstant.当前CK已经有的订单 + pt_pin, orderId, 12, TimeUnit.HOURS);
            redisTemplate.opsForValue().set(PreConstant.订单管理微信链接 + orderId, payUrl, 4, TimeUnit.MINUTES);
            log.debug("组装安卓订单");
            JdOrderPt jdOrderPt = JdOrderPt.builder().orderId(orderId).ptPin(pt_pin).expireTime(DateUtil.offsetHour(new Date(), 12)).createTime(new Date())
                    .skuPrice(jdAppStoreConfigProduct.getSkuPrice()).skuId(jdAppStoreConfigProduct.getSkuId()).skuName(jdAppStoreConfigProduct.getSkuName())
                    .prerId(payId).isWxSuccess(PreConstant.ONE).currentCk(mck).port(oneIp.getPort()).ip(oneIp.getIp()).orgAppCk(ck).isEnable(PreConstant.FIVE).build();
            return Arrays.asList(jdOrderPt);
        } catch (Exception e) {
            log.error("这个ck报错了msg:{}", ck);
        }
        return null;
    }


    public String payUrlIos(Proxy proxy, String payId) {
        try {
            String bodyData = String.format("{\"appId\":\"jd_android_app4\",\"payId\":\"%s\"}", payId);
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            if (ObjectUtil.isNotNull(proxy)) {
                builder.proxy(proxy);
            }
            OkHttpClient client = builder.build();
            RequestBody requestBody = new FormBody.Builder()
                    .add("body", bodyData)
                    .build();
            Request request = new Request.Builder()
                    .url("https://pay.m.jd.com/index.action?functionId=weixinPay")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            Response response = client.newCall(request).execute();
            String payData = response.body().string();
            if (StrUtil.isNotBlank(payData) && payData.contains("prepayId")) {
                return payData;
            }
        } catch (Exception e) {

        }
        return null;
    }

    public String payUrl(String payId, String mck, Proxy proxy, String pin, String appck, Integer index, Integer group, String orderId, JdAppStoreConfig jdAppStoreConfigProduct) {
        if (index >= 3) {
            return null;
        }
        index = index + 1;
        check(payId, mck, proxy);
        String payIdTimeOut = redisTemplate.opsForValue().get("订单支付已超时1分钟:" + payId);
        if (StrUtil.isNotBlank(payIdTimeOut)) {
            return null;
        }
        try {
            String bodyData = String.format("{\"source\":\"mcashier\",\"origin\":\"h5\",\"page\":\"pay\",\"mcashierTraceId\":1653762486838,\"appId\":\"jd_m_pay\",\"payId\":\"%s\",\"eid\":\"2XRK4PH7YTECS7DYZNDH764SWHELI2J2COCDRU357GLIV6TKL63PRAESJZVNTNB53M6BZABAON74E2QQEOCZO745CY\"}", payId);
            RequestBody requestBody = new FormBody.Builder()
                    .add("body", bodyData)
                    .build();
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            if (ObjectUtil.isNotNull(proxy)) {
                builder.proxy(proxy);
            }
            OkHttpClient client = builder.build();
            Request request = new Request.Builder()
                    .url("https://api.m.jd.com/client.action?functionId=platWapWXPay&appid=mcashier")
                    .post(requestBody)
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .addHeader("cookie", mck)
                    .addHeader("User-Agent", "jdapp;android;9.4.4;10;7316266326835303-1663034366462326;network/wifi;model/PACT00;addressid/0;aid/7ab6b850a604fd2b;oaid/;osVer/29;appBuild/87076;psn/m 0Ddoh86M2Rp emACf77VJZ2BYiaC7o|91;psq/1;adk/;ads/;pap/JA2015_311210|9.4.4|ANDROID 10;osv/10;pv/34.23;installationId/5315bb3ac03f4341bde696c7fb2aaf28;jdv/0|kong|t_1000440933_|jingfen|efde4563d64c4e18ba131fd2e011f050|1653590049;ref/com.jd.lib.ordercenter.mygoodsorderlist.view.activity.MyOrderListActivity;partner/lc031;apprpd/OrderCenter_List;eufv/1;jdSupportDarkMode/0;hasUPPay/1;hasOCPay/0;supportHuaweiPay/0;supportBestPay/0;Mozilla/5.0 (Linux; Android 10; PACT00 Build/QP1A.190711.020; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/89.0.4389.72 MQQBrowser/6.2 TBS/046010 Mobile Safari/537.36")
                    .addHeader("Origin", "https://pay.m.jd.com")
                    .addHeader("Referer", "https://pay.m.jd.com/cpay/newPay-index.html?appId=jd_android_app4&needLoginSwitch=1&payId=5dbe72b4a9e643099019616912583543&sid=0fe98f15efa19a8d714fe2330bb7de7w&un_area=22_1930_0_0")
                    .build();
            Response response = client.newCall(request).execute();
            String returnStr = response.body().string();
            log.info("response:{},payId:{}", returnStr, payId);
            if (returnStr.contains("wx.tenpay.com")) {
                redisTemplate.opsForValue().set(PreConstant.有用的payId + orderId, payId, 10, TimeUnit.MINUTES);
                String payUrl = JSON.parseObject(JSON.parseObject(returnStr).getString("payInfo")).get("mweb_url").toString();
                redisTemplate.opsForValue().set(PreConstant.订单管理微信链接 + orderId, payUrl, 4, TimeUnit.MINUTES);
                return payUrl;
            } else if (returnStr.contains("当前支付方式不可用") || returnStr.contains("请更换其他支付方式")) {
                if (group == PreConstant.ONE) {
                    log.error("当前apptoreck锁定到第二天不能释放。不能释放");
                    Date endOfDay = DateUtil.endOfDay(new Date());
                    long between = DateUtil.between(new Date(), endOfDay, DateUnit.MINUTE);
                    redisTemplate.opsForValue().set(PreConstant.锁定CK到第二天结束 + pin, pin, between, TimeUnit.MINUTES);
                } else if (group == PreConstant.TWO) {
                    log.error("当前ck锁定到第二天不能释放。不能释放");
                    Date endOfDay = DateUtil.endOfDay(new Date());
                    long between = DateUtil.between(new Date(), endOfDay, DateUnit.MINUTE);
                    redisTemplate.opsForValue().set(PreConstant.美团锁定CK到第二天结束 + pin, pin, between, TimeUnit.MINUTES);
                }
            } else if (returnStr.contains("移动端暂不支持该支付类型支付")) {
                payId = getPayIdMeiTuan(mck, orderId, proxy, jdAppStoreConfigProduct);
                Boolean check = check(payId, mck, proxy);
                JdProxyIpPort oneIp = getJdProxyIpPort(null);
                TokenKeyVo build = TokenKeyVo.builder().cookie(appck.trim()).build();
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
                TokenKeyResVo tokenKey = tokenKeyService.getTokenKey(build, oneIp, "");
                mck = getMck(proxy, tokenKey.getTokenKey());
                return payUrl(payId, mck, proxy, pin, appck, index, group, orderId, jdAppStoreConfigProduct);
            }
            return null;
        } catch (Exception e) {

        }
        return null;
    }


    public Boolean check(String payId, String mck, Proxy proxy) {
        try {
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();

            if (ObjectUtil.isNotNull(proxy)) {
                builder.proxy(proxy);
            }
            OkHttpClient client = builder.build();
            RequestBody requestBody = new FormBody.Builder()
                    .add("lastPage", "https://wqs.jd.com/")
                    .add("appId", "jd_android_app4")
                    .add("payId", payId)
                    .build();
            Request request = new Request.Builder()
                    .url("https://pay.m.jd.com/newpay/index.action")
                    .post(requestBody)
                    .addHeader("cookie", mck)
                    .addHeader("User-Agent", "jdapp;android;9.4.4;10;7316266326835303-1663034366462326;network/wifi;model/PACT00;addressid/0;aid/7ab6b850a604fd2b;oaid/;osVer/29;appBuild/87076;psn/m 0Ddoh86M2Rp emACf77VJZ2BYiaC7o|91;psq/1;adk/;ads/;pap/JA2015_311210|9.4.4|ANDROID 10;osv/10;pv/34.23;installationId/5315bb3ac03f4341bde696c7fb2aaf28;jdv/0|kong|t_1000440933_|jingfen|efde4563d64c4e18ba131fd2e011f050|1653590049;ref/com.jd.lib.ordercenter.mygoodsorderlist.view.activity.MyOrderListActivity;partner/lc031;apprpd/OrderCenter_List;eufv/1;jdSupportDarkMode/0;hasUPPay/1;hasOCPay/0;supportHuaweiPay/0;supportBestPay/0;Mozilla/5.0 (Linux; Android 10; PACT00 Build/QP1A.190711.020; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/89.0.4389.72 MQQBrowser/6.2 TBS/046010 Mobile Safari/537.36")
                    .addHeader("Origin", "https://pay.m.jd.com")
                    .addHeader("Referer", "https://pay.m.jd.com/cpay/newPay-index.html?appId=jd_android_app4&needLoginSwitch=1&payId=5dbe72b4a9e643099019616912583543&sid=0fe98f15efa19a8d714fe2330bb7de7w&un_area=22_1930_0_0")
                    .build();
            Response response = client.newCall(request).execute();
            String body = response.body().string();
            response.close();
            log.info("checkMsg:{},payId:{}", body.substring(0, 20), payId);
            if (StrUtil.isNotBlank(body) && body.length() >= 50 && !body.contains("登录") && !body.contains("订单支付已超时（1分钟）")) {
                return true;
            }
            if (body.contains("订单支付已超时（1分钟）")) {
                redisTemplate.opsForValue().set("订单支付已超时1分钟:", payId, 10, TimeUnit.MINUTES);
            }
            if (StrUtil.isBlank(body)) {
                redisTemplate.opsForValue().set("订单支付已超时1分钟:", payId, 10, TimeUnit.MINUTES);
            }
        } catch (Exception e) {

        }
        return false;
    }

    public String getPayId(String ck, String orderId, Proxy proxy, String payablePrice) {
        try {
            String bodyData = String.format("{\"appId\":\"jd_android_app4\",\"fk_aid\":\"7ab6b850a604fd2b\",\"fk_appId\":\"com.jingdong.app.mall\",\"fk_terminalType\":\"02\",\"fk_traceIp\":\"192.168.2.247\",\"orderId\":\"%s\",\"orderType\":\"34\",\"orderTypeCode\":\"0\",\"paySourceId\":\"2\",\"payablePrice\":\"%s\",\"paysign\":\"%s\"}", orderId, payablePrice, PaySign.getPaySign(orderId, payablePrice));
            SignVoAndDto signVoAndDto = new SignVoAndDto("genAppPayId", bodyData);
            signVoAndDto = JdSgin.newSign(signVoAndDto);
            String url = String.format("https://api.m.jd.com/client.action?functionId=genAppPayId&clientVersion=9.4.4&client=android&uuid=%s&st=%s&sign=%s&sv=120", signVoAndDto.getUuid(), signVoAndDto.getSt(), signVoAndDto.getSign());
            OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy).build();
            RequestBody requestBody = new FormBody.Builder()
                    .add("body", bodyData)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("Cookie", ck)
                    .addHeader("Charset", "UTF-8")
                    .addHeader("user-agent", "okhttp/3.12.1;jdmall;android;version/11.0.2;build/97565;")
                    .build();
            Response response = client.newCall(request).execute();
            String payIdStr = response.body().string();
            String payId = JSON.parseObject(payIdStr).getString("payId");
            return payId;
        } catch (Exception e) {

        }
        return null;

    }

    public String getPayIdMeiTuan(String mck, String orderId, Proxy proxy, JdAppStoreConfig jdAppStoreConfigProduct) {
        try {
            String url = String.format("https://xmlya.m.jd.com/goPay.action?orderId=%s&onlineAmount=%s", orderId, jdAppStoreConfigProduct.getSkuPrice().toString());
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            builder.followRedirects(false);
            if (ObjectUtil.isNotNull(proxy)) {
                builder.proxy(proxy);
            }
            OkHttpClient client = builder.build();
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Cookie", mck)
                    .addHeader("Charset", "UTF-8")
                    .addHeader("user-agent", "okhttp/3.12.1;jdmall;android;version/11.0.2;build/97565;")
                    .addHeader("Origin", "https://pay.m.jd.com")
                    .addHeader("Referer", "https://pay.m.jd.com/cpay/newPay-index.html?appId=jd_android_app4&needLoginSwitch=1&payId=5dbe72b4a9e643099019616912583543&sid=0fe98f15efa19a8d714fe2330bb7de7w&un_area=22_1930_0_0")
                    .build();
            Response response = client.newCall(request).execute();
            String location = response.header("Location");
            if (StrUtil.isNotBlank(location)) {
                String payId = PreUtils.parseUrl(location).getParams().get("payId");
                return payId;
            }
            log.info("美团生产没有payId");
        } catch (Exception e) {
            log.error("美团生产没有获取到payId：msg:{}", e.getMessage());
        }
        return null;

    }

    public String getOrderId(String ck, Proxy proxy, JdAppStoreConfig jdAppStoreConfig) {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy).build();
            String bodyData = String.format("{\"appKey\":\"android\",\"brandId\":\"999440\",\"buyNum\":1,\"payMode\":\"0\",\"rechargeversion\":\"10.9\",\"skuId\":\"%s\",\"totalPrice\":\"%s\",\"type\":1,\"version\":\"1.10\"}",
                    jdAppStoreConfig.getSkuId(), jdAppStoreConfig.getSkuPrice().intValue() * 100 + "");
            SignVoAndDto signVoAndDto = new SignVoAndDto("submitGPOrder", bodyData);
            signVoAndDto = JdSgin.newSign(signVoAndDto);
            RequestBody requestBody = new FormBody.Builder()
                    .add("body", bodyData)
                    .build();
            Request request = new Request.Builder()
                    .url(String.format("https://api.m.jd.com/client.action?functionId=submitGPOrder&clientVersion=%s&client=android&uuid=%s&st=%s&sign=%s&sv=%s",
                            signVoAndDto.getClientVersion(), signVoAndDto.getUuid(), signVoAndDto.getSt(), signVoAndDto.getSign(), signVoAndDto.getSv()))
                    .post(requestBody)
                    .addHeader("Cookie", ck)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .addHeader("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 10; PACT00 Build/QP1A.190711.020)")
                    .addHeader("Host", "api.m.jd.com")
                    .addHeader("cache-control", "no-cache")
                    .build();
            Response response = client.newCall(request).execute();
            String orderStr = response.body().string();
            if (orderStr.contains("orderId")) {
                String orderId = JSON.parseObject(JSON.parseObject(orderStr).getString("result")).getString("orderId") + "";
                return orderId;
            }

        } catch (Exception e) {

        }
        return null;
    }

    public String getOrderIdMeiTuan(String mck, Proxy proxy, JdAppStoreConfig jdAppStoreConfig) {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy).build();
            String bodyData = String.format("{\"couponIds\":\"\",\"orderAmount\":%s,\"payType\":\"0\",\"brand\":\"73\",\"quantity\":\"1\",\"skuId\":\"%s\",\"dongquanAmount\":\"\",\"jingquanAmount\":\"\",\"onlineAmount\":\"%s\",\"repeatKey\":\"%s\",\"featureParam\":{},\"eid\":\"%s\"}",
                    jdAppStoreConfig.getSkuPrice(), jdAppStoreConfig.getSkuId(), jdAppStoreConfig.getSkuPrice(), PreUtils.getRandomString(35), PreUtils.getRandomString(90));
            RequestBody requestBody = new FormBody.Builder()
                    .add("submitParam", bodyData)
                    .build();
            Request request = new Request.Builder()
                    .url("https://xmlya.m.jd.com/submitOrder")
                    .post(requestBody)
                    .addHeader("Cookie", mck)
                    .addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1")
                    .addHeader("Host", "xmlya.m.jd.com")
                    .addHeader("Origin", "https://pay.m.jd.com")
                    .addHeader("Referer", "https://xmlya.m.jd.com/?skuId=200148732995")
                    .build();
            Response response = client.newCall(request).execute();
            String orderStr = response.body().string();
            response.close();
            if (orderStr.contains("orderId")) {
                String orderId = JSON.parseObject(orderStr).getString("orderId");
                return orderId;
            }
        } catch (Exception e) {

        }
        return null;
    }

    public JdOrderPt checkAndReturnOrderPt(String ck, String payId, JdProxyIpPort oneIp, JdAppStoreConfig jdAppStoreConfig, Map<Integer, JdPayOrderPostAddress> stepsMap) {
        try {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
            OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy).build();
            RequestBody requestBody = new FormBody.Builder()
                    .add("appId", "jd_m_yxdk")
                    .add("lastPage", "https://gamerecg.m.jd.com/")
                    .add("payId", payId)
                    .build();
            Request request = new Request.Builder().post(requestBody).url("https://pay.m.jd.com/newpay/index.action")
                    .header("referer", "https://pay.m.jd.com/cpay/newPay-index.html?appId=jd_m_yxdk&payId=" + payId)
                    .header("origin", "https://pay.m.jd.com")
                    .header("cookie", ck)
                    .header("user-agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4239.0 Mobile Safari/537.36").build();
            Response execute = client.newCall(request).execute();
            String body = execute.body().string();
            execute.close();
            if (StrUtil.isNotBlank(body) && body.contains("wapWeiXinPay")) {
                List<JSONObject> jsonObjects = JSON.parseArray(JSON.parseObject(JSON.parseObject(body).get("payParamsObject").toString()).get("payChannelList").toString(), JSONObject.class);
                for (JSONObject jsonObject : jsonObjects) {
                    if ("wapWeiXinPay".equals(jsonObject.get("code").toString()) && Integer.valueOf(jsonObject.get("status").toString()) == 1) {
                        log.info("当前订单有微信支付msg:{}", payId);
                        JdOrderPt.JdOrderPtBuilder orderPtBuilder = JdOrderPt.builder();
                        JSONObject payParamsObject = JSON.parseObject(JSON.parseObject(body).get("payParamsObject").toString());
                        orderPtBuilder.orderId(payParamsObject.get("orderId").toString());
                        String pt_pin = URLDecoder.decode(PreUtils.get_pt_pin(ck));
                        orderPtBuilder.ptPin(pt_pin);
//                        int countdownTime = new BigDecimal(payParamsObject.getString("countdownTime")).intValue();
                        DateTime expire_time = DateUtil.offsetMinute(new Date(), 690);
                        orderPtBuilder.expireTime(expire_time);
                        orderPtBuilder.createTime(new Date());
                        orderPtBuilder.skuPrice(new BigDecimal(payParamsObject.getString("payprice")));
                        orderPtBuilder.skuName(jdAppStoreConfig.getSkuName());
                        orderPtBuilder.skuId(jdAppStoreConfig.getSkuId());
                        orderPtBuilder.prerId(payId);
                        orderPtBuilder.isWxSuccess(PreConstant.ONE);
                        orderPtBuilder.currentCk(ck);
                        orderPtBuilder.ip(oneIp.getIp());
                        orderPtBuilder.port(oneIp.getPort());
                        orderPtBuilder.failTime(PreConstant.ZERO);
                        orderPtBuilder.isEnable(PreConstant.FIVE);
                        JdOrderPt jdOrderPt = orderPtBuilder.build();
                        check_4_1(jdOrderPt, oneIp);
                        check_4_2(jdOrderPt, oneIp, null);
                        String weiXinPayUrlMath_5 = getWeiXinPayUrlMath_5(stepsMap.get(5), jdOrderPt, client, null);
                        if (StrUtil.isBlank(weiXinPayUrlMath_5)) {
                            return null;
                        }
                        jdOrderPt.setWeixinUrl(weiXinPayUrlMath_5);
//                        this.jdOrderPtMapper.insert(jdOrderPt);
                        return jdOrderPt;
                    }
                }
            }
        } catch (Exception e) {
            log.error("google检查报错。请查看。msg:{}", e.getStackTrace());
        }
        return null;
    }

    private String submitOrder(JdCk jdCkDb, OkHttpClient client, JdPayOrderPostAddress jdPayOrderPostAddress_2, JdAppStoreConfig jdAppStoreConfig) {
        String python_url = submitOrder_2(client, jdPayOrderPostAddress_2, jdAppStoreConfig);
        try {
            String payId = PreUtils.parseUrl(python_url).getParams().get("payId");
            return payId;
        } catch (Exception e) {
            if (ObjectUtil.isNotNull(jdCkDb)) {

            }
            log.info("获取payId解析报错。没有这个Id:msg:{},{}", python_url, e.getMessage());
        }
        return null;
    }


    public String getWeiXinPayUrlMath_5(JdPayOrderPostAddress jdPayOrderPostAddress, JdOrderPt jdOrderPt, OkHttpClient client, Map<String, String> map) {
        try {
            if (ObjectUtil.isNull(jdOrderPt)) {
                return null;
            }
            String url = String.format(jdPayOrderPostAddress.getUrl(), jdOrderPt.getPrerId(), PreUtils.getRandomString("eidAf7ec812217sc2unIhDbfRC2vyIiCyWCfp9rpygQ1n05pH+F1dg0Jdhd0vcmUDK5s/mtSTjOeIOzXUO1lnWYQ/J491OJXOd6I2dnstXCXFGiREnBu".length()));
            Request.Builder header = new Request.Builder()
                    .url(url)
                    .get()
                    .header("user-agent", jdPayOrderPostAddress.getUserAgent())
                    .header("cookie", jdOrderPt.getCurrentCk())
                    .header("referer", String.format(jdPayOrderPostAddress.getReferer(), jdOrderPt.getPrerId()));
            if (CollUtil.isNotEmpty(map)) {
                for (String key : map.keySet()) {
                    header.header(key, map.get(key));
                }
            }
            Request request = header.build();
            Response response = client.newCall(request).execute();
            String body = response.body().string();
            response.close();
            log.info("---------Msg:[{}]", body);
            if (body.contains("wx.tenpay.com")) {
                return JSON.parseObject(body).get("mweb_url").toString();
            }
            return null;
        } catch (Exception e) {
            jdOrderPtMapper.updateById(jdOrderPt);
            log.error("获取微信支付失败e:{}", e.getMessage());
        }
        return null;
    }

    private void getOrderIds(OkHttpClient client, Map<Integer, JdPayOrderPostAddress> stepsMap, List<JdOrderPt> jdOrderPtFindOrderId, List<TepmDto> payIds) {
        for (JdOrderPt orderPt : jdOrderPtFindOrderId) {
            JdPayOrderPostAddress jdPayOrderPostAddress_7 = stepsMap.get(7);
            String jumpurl = step7PerIdByWapWeiXinPay_7(jdPayOrderPostAddress_7, orderPt.getOrderId(), client, null, ckManager(client));
            log.info("检查");
            try {
                String payId = PreUtils.parseUrl(jumpurl).getParams().get("payId");
                TepmDto tepmDto = new TepmDto();
                tepmDto.setOrderId(orderPt.getOrderId());
                tepmDto.setCurrentCk(ckManager(client));
                tepmDto.setPayId(payId);
                tepmDto.setSkuId(orderPt.getSkuId());
                tepmDto.setPtPin(orderPt.getPtPin());
                tepmDto.setSkuName(orderPt.getSkuName());
                tepmDto.setExpireTime(orderPt.getExpireTime());
                tepmDto.setSkuPrice(orderPt.getSkuPrice());
                payIds.add(tepmDto);
            } catch (Exception e) {
                log.info("匹配获取payId解析报错。没有这个Id:msg:{}", jumpurl);
            }
            log.info("开始执行已经有的顶顶那。获取payId");
        }
    }

    @Resource
    private JdSignParamMapper jdSignParamMapper;

    private Boolean check_4_2(JdOrderPt jdOrderPt, JdProxyIpPort oneIp, Map<String, String> map) {
        try {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
            OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy).build();
            String data = String.format("{\"source\":\"mcashier\",\"origin\":\"h5\",\"page\":\"pay\",\"mcashierTraceId\":1649431293196,\"appId\":\"jd_m_yxdk\",\"payId\":\"%s\"}", jdOrderPt.getPrerId());
            RequestBody requestBody = new FormBody.Builder()
                    .add("body", data)
                    .build();
            Request.Builder header = new Request.Builder()
                    .url("https://api.m.jd.com/client.action?functionId=platPayChannel&appid=mcashier")
                    .post(requestBody)
                    .addHeader("user-agent", " Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1")
                    .addHeader("referer", "https://mpay.m.jd.com/")
                    .addHeader("Cookie", jdOrderPt.getCurrentCk());
            if (CollUtil.isNotEmpty(map)) {
                for (String key : map.keySet()) {
                    header.header(key, map.get(key));
                }
            }
            Request request = header.build();
            Response execute = client.newCall(request).execute();
            JSONObject parseObject = JSON.parseObject(execute.body().string());
            execute.close();
            Object payChannelListStr = parseObject.get("payChannelList");
            JSONArray payChannelList = JSON.parseArray(payChannelListStr.toString());
            if (CollUtil.isNotEmpty(payChannelList))
                for (Object str : payChannelList) {
                    JSONObject payOne = JSON.parseObject(str.toString());
                    if (payOne.get("code").equals("wapWeiXinPay") && Integer.valueOf(payOne.get("status").toString()) == 1) {
                        log.info("此号可以微信支付");
                        return true;
                    }
                }
        } catch (Exception e) {

        }
        return false;
    }

    private Boolean check_4_1(JdOrderPt jdOrderPt, JdProxyIpPort oneIp) {
        try {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
            OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy).build();
            JdSignParam genTokenDb = jdSignParamMapper.selectOne(Wrappers.<JdSignParam>lambdaQuery().eq(JdSignParam::getMark, "genToken"));
            genTokenDb.setUuid(PreUtils.getRandomString("55bs162e53b926e1".length()));
            String data = String.format("{\"appId\":\"jd_android_app4\",\"hasBestPay\":\"0\",\"hasHuaweiPay\":\"0\",\"hasOCPay\":\"0\",\"hasUPPay\":\"0\",\"payId\":\"%s\"}", jdOrderPt.getPrerId());
            SignVoAndDto signVoAndDto = new SignVoAndDto("payIndex", data, genTokenDb.getClientVersion(), genTokenDb.getUuid());
            log.info("开始签证");
            JdPathConfig path = proxyProductService.getPath();
            signVoAndDto = JdSgin.newSign(signVoAndDto);
//            signVoAndDto = RunSignUtils.signMain(signVoAndDto, path.getJdApk());
            RequestBody requestBody = new FormBody.Builder()
                    .add("body", data)
                    .build();
            Request request = new Request.Builder()
                    .url(String.format("https://api.m.jd.com/client.action?functionId=%s&clientVersion=%s&client=android&sdkVersion=29&uuid=%s&st=%s&sign=%s&sv=%s",
                            signVoAndDto.getFunctionId(), signVoAndDto.getClientVersion(), signVoAndDto.getUuid(), signVoAndDto.getSt(), signVoAndDto.getSign(), signVoAndDto.getSv()))
                    .post(requestBody)
                    .addHeader("Charset", "UTF-8")
                    .addHeader("User-Agent", "okhttp/3.12.1")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("Cookie", jdOrderPt.getCurrentCk()).build();
            Response execute = client.newCall(request).execute();
            log.info("微信验证是否可以用msg:{}", execute.body().string());
            execute.close();
        } catch (Exception e) {

        }
        return false;
    }

    public Boolean check_4(JdPayOrderPostAddress jdPayOrderPostAddress_4, JdOrderPt jdOrderPt, JdProxyIpPort oneIp) {
        try {
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();

            if (ObjectUtil.isNotNull(oneIp)) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
                builder.proxy(proxy);
            }
            OkHttpClient client = builder.build();
            RequestBody requestBody = new FormBody.Builder()
                    .add("appId", jdPayOrderPostAddress_4.getAppId())
                    .add("payId", jdOrderPt.getPrerId())
                    .build();
            Request request = new Request.Builder().post(requestBody).url(jdPayOrderPostAddress_4.getUrl())
                    .header("referer", String.format(jdPayOrderPostAddress_4.getReferer(), jdOrderPt.getSkuId()))
                    .header("origin", jdPayOrderPostAddress_4.getOrigin())
                    .header("cookie", jdOrderPt.getCurrentCk())
                    .header("user-agent", jdPayOrderPostAddress_4.getUserAgent()).build();
            Response execute = client.newCall(request).execute();
            String body = execute.body().string();
            execute.close();
            if (StrUtil.isNotBlank(body) && body.contains("wapWeiXinPay")) {
                List<JSONObject> jsonObjects = JSON.parseArray(JSON.parseObject(JSON.parseObject(body).get("payParamsObject").toString()).get("payChannelList").toString(), JSONObject.class);
                for (JSONObject jsonObject : jsonObjects) {
                    if ("wapWeiXinPay".equals(jsonObject.get("code").toString()) && Integer.valueOf(jsonObject.get("status").toString()) == 1) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {

        }
        return false;
    }

    private List<JdOrderPt> orderList_3(JdPayOrderPostAddress jdPayOrderPostAddress_3, List<JdAppStoreConfig> jdAppStoreConfigs, JdProxyIpPort oneIp, String ck) {
        try {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
            OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy).build();
            String ptPin = PreUtils.get_pt_pin(ck);
            if (StrUtil.isBlank(ptPin)) {
                return null;
            }
            Request request = new Request.Builder().get().url(jdPayOrderPostAddress_3.getUrl())
                    .header("referer", jdPayOrderPostAddress_3.getReferer())
                    .header("cookie", ck)
                    .header("user-agent", jdPayOrderPostAddress_3.getUserAgent()).build();
            Response execute = client.newCall(request).execute();
            String body = execute.body().string();
            execute.close();
            JSONObject jsonObject = JSON.parseObject(body);
            if (!"0".equals(jsonObject.get("errCode"))) {
                return null;
            }
            JSONArray orderList = JSON.parseArray(jsonObject.get("orderList").toString());
            if (CollUtil.isEmpty(orderList)) {
                return null;
            }
            List<JdOrderPt> jdOrderPts = new ArrayList<>();
            for (Object oneStr : orderList) {
                JSONObject one = JSON.parseObject(oneStr.toString());
                String orderId = one.get("orderId").toString();
                BigDecimal factPrice = new BigDecimal(one.get("factPrice").toString()).divide(new BigDecimal(100), BigDecimal.ROUND_HALF_UP, 2);
                Integer s = Integer.valueOf(JSON.parseObject(one.get("stateInfo").toString()).get("payLeftTime").toString());
                Date expire_time = DateUtil.offsetSecond(new Date(), s);
                Object productListObj = JSON.parseArray(one.get("productList").toString()).get(0);
                JSONObject productList = JSON.parseObject(productListObj.toString());
                String skuId = productList.get("skuId").toString();
                String title = productList.get("title").toString();
                JdOrderPt jdOrderPt = JdOrderPt.builder().orderId(orderId).ptPin(ptPin).success(0).expireTime(expire_time)
                        .createTime(new Date()).skuPrice(factPrice).skuName(title).skuId(skuId).build();
                jdOrderPts.add(jdOrderPt);
            }
            if (CollUtil.isNotEmpty(jdOrderPts)) {
                List<String> toList = jdAppStoreConfigs.stream().map(JdAppStoreConfig::getSkuId).collect(Collectors.toList());
                List<JdOrderPt> jdOrderPtAccoutOrderList = jdOrderPts.stream().filter(it -> toList.contains(it.getSkuId())).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(jdOrderPtAccoutOrderList)) {
                    log.info("返回订单详情msg:{}", jdOrderPtAccoutOrderList);
                    return jdOrderPtAccoutOrderList;
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            log.error("解析订单报错");
        }
        return null;
    }


    public String step7PerIdByWapWeiXinPay_7(JdPayOrderPostAddress jdPayOrderPostAddress_7, String orderId, OkHttpClient client, Map<String, String> map, String ck) {
        try {
            String urlWapWeiXinPay = String.format(jdPayOrderPostAddress_7.getUrl(), orderId);
            Request.Builder header = new Request.Builder()
                    .url(urlWapWeiXinPay)
                    .get()
                    .header("user-agent", jdPayOrderPostAddress_7.getUserAgent())
                    .header("cookie", ck)
                    .header("referer", jdPayOrderPostAddress_7.getReferer());
            if (CollUtil.isNotEmpty(map)) {
                for (String key : map.keySet()) {
                    header.header(key, map.get(key));
                }
            }
            Request request = header.build();
            Response response = client.newCall(request).execute();
            String body = response.body().string();
            response.close();
            JSONObject jsonObject = JSON.parseObject(body);
            log.info("跳转页面msg:{}", jsonObject);
            String jumpurl = JSON.parseObject(jsonObject.get("data").toString()).get("jumpurl").toString();
            log.info("匹配订单:jumpurl:{}", body);
            return jumpurl;
        } catch (Exception e) {
            log.error("匹配订单:报错jumpurl:{}", e);
        }
        return null;
    }

    private void failTimeBuild(String orderId) {
        try {
            log.info("开始计数有多少次失败的概率");
            JdOrderPt jdOrderPt = this.jdOrderPtMapper.selectOne(Wrappers.<JdOrderPt>lambdaQuery().eq(JdOrderPt::getOrderId, orderId));
            if (ObjectUtil.isNotNull(jdOrderPt) && ObjectUtil.isNotNull(jdOrderPt.getFailTime()) && jdOrderPt.getFailTime() >= 0) {
                if (jdOrderPt.getFailTime() >= 5) {
                    jdOrderPt.setFailTime(jdOrderPt.getFailTime() + 1);
                    jdOrderPt.setIsWxSuccess(0);
                    jdOrderPtMapper.updateById(jdOrderPt);
                } else {
                    jdOrderPt.setFailTime(jdOrderPt.getFailTime() + 1);
                    jdOrderPtMapper.updateById(jdOrderPt);
                }
            }
        } catch (Exception e) {
            log.info("报错了。msg:{}", e.getStackTrace());
        }

    }


    public String ckManager(OkHttpClient client) {
        LocalCookieJar cookieJar = (LocalCookieJar) client.cookieJar();
        List<Cookie> cookies = cookieJar.cookies;
        StringBuilder stringBuilder = new StringBuilder();
        for (Cookie ck : cookies) {
            stringBuilder.append(ck.name() + "=" + ck.value() + ";");
        }
        return stringBuilder.toString();
    }

    public boolean init(OkHttpClient client, String url, Map<String, String> map) {
        try {
            Request.Builder header = new Request.Builder()
                    .url(url)
                    .get();

           /* if (CollUtil.isNotEmpty(map)) {
                for (String key : map.keySet()) {
                    header.header(key, map.get(key));
                }
            }*/
            Request request = header.build();
            Response execute = client.newCall(request).execute();
            String location = execute.header("Location");
            execute.close();
            log.info("跳转到msg:{}", location);
            Request locationRe = new Request.Builder()
                    .url(location)
                    .get()
                    .build();
            LocalCookieJar cookieJar = (LocalCookieJar) client.cookieJar();
            List<Cookie> cookies = cookieJar.cookies;
            UrlEntity urlEntity = PreUtils.parseUrl(location);
            Map<String, String> params = urlEntity.getParams();
            String un_area = params.get("un_area");
            String sid = params.get("sid");
            Cookie cookie39 = new Cookie.Builder()
                    .domain("jd.com")
                    .name("un_area")
                    .value(un_area)
                    .build();
            Cookie cookie40 = new Cookie.Builder()
                    .domain("jd.com")
                    .name("sid")
                    .value(sid)
                    .build();
            cookies.add(cookie39);
            cookies.add(cookie40);
            ((LocalCookieJar) client.cookieJar()).cookies = cookies;
            Response execute1 = client.newCall(locationRe).execute();
            execute1.close();
            return true;
        } catch (Exception e) {
            log.error("初始化报错，{}", e.getMessage());
        }
        return Boolean.FALSE;
    }

    public OkHttpClient clientManager(Proxy proxy, String currentCk) {
        try {
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder()
//                    .proxy(proxy)
                    .followRedirects(false);
            if (ObjectUtil.isNotNull(proxy)) {
                builder.proxy(proxy);
            }
            //代理设置好了
            if (StrUtil.isNotBlank(currentCk)) {
                LocalCookieJar localCookieJar = new LocalCookieJar();
                List<Cookie> cookies = localCookieJar.cookies;
                cookies.addAll(new LocalCookieJar(currentCk).cookies);
                builder.cookieJar(localCookieJar);
            } else {
                LocalCookieJar localCookieJar = new LocalCookieJar();
                builder.cookieJar(localCookieJar);
            }

//            builder.cookieJar()
            return builder.build();
        } catch (Exception e) {
            log.error("报错msg:e:{}", e.getMessage());
        }
        return null;
    }

    private String submitOrder_2(OkHttpClient client, JdPayOrderPostAddress jdPayOrderPostAddress_2, JdAppStoreConfig jdAppStoreConfig) {
        try {
            RequestBody requestBody = new FormBody.Builder()
                    .add("chargeType", "13759")
                    .add("skuId", jdAppStoreConfig.getSkuId())
                    .add("brandId", "999440")
                    .add("payPwd", "")
                    .add("customs", "")
                    .add("gamesrv", "")
                    .add("accounttype", "")
                    .add("chargetype", "")
                    .add("couponIds", "")
                    .add("useBean", "")
                    .add("skuName", "1")
                    .add("buyNum", "1")
                    .add("type", "1")
                    .add("payMode", "0")
                    .add("totalPrice", jdAppStoreConfig.getSkuPrice().toString())
                    .build();
            Request request = new Request.Builder().post(requestBody).url(jdPayOrderPostAddress_2.getUrl())
                    .header("origin", "https://gamerecg.m.jd.com")
                    .header("referer", String.format("https://gamerecg.m.jd.com/?skuId=%s&lng=123.1212169&lat=37.196431&sid=19221a7219f0663d5c7fe613b6d0bd7w&un_area=22_1930_49324_49398", jdAppStoreConfig.getSkuId()))
                    .header("user-agent", jdPayOrderPostAddress_2.getUserAgent())
                    .build();
            Response execute = client.newCall(request).execute();
            String location = execute.header("Location");
            execute.close();
            Request locationRe = new Request.Builder()
                    .url(location)
                    .get()
                    .build();
            Response execute1 = client.newCall(locationRe).execute();
            execute1.close();
            log.info("本地跳转路径msg:{}", location);
            return location;
        } catch (Exception e) {
            log.error("下单失败msg:[e:{}]", e.getMessage());
        }
        return null;
    }

    /**
     * 锁定并且删除当前锁定pt
     *
     * @param tradeNo
     * @return
     */
    private boolean MatchLock(String tradeNo) {
        if (StrUtil.isBlank(tradeNo)) {
            return true;
        }
        String matchLockOk = redisTemplate.opsForValue().get("匹配锁定成功:" + tradeNo);
        if (StrUtil.isNotBlank(matchLockOk)) {
            return true;
        }
        return false;
    }

    // 发送消息，destination是发送到的队列，message是待发送的消息
    private void sendMessageSenc(Destination destination, final String message, Integer second) {
        Map<String, Object> headers = new HashMap<>();
        //发送延迟队列，延迟10秒,单位毫秒
        headers.put(ScheduledMessage.AMQ_SCHEDULED_DELAY, second * 1000);
        jmsMessagingTemplate.convertAndSend(destination, message, headers);
    }
}
