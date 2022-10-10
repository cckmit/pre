package com.xd.pre.modules.px.douyin;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xd.pre.common.constant.PreConstant;
import com.xd.pre.common.utils.R;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.modules.data.tenant.PreTenantContextHolder;
import com.xd.pre.modules.px.appstorePc.PcAppStoreService;
import com.xd.pre.modules.px.douyin.huadan.HuaDanDto;
import com.xd.pre.modules.px.douyin.huadan.HuaDanSkuVo;
import com.xd.pre.modules.px.douyin.pay.PayDto;
import com.xd.pre.modules.sys.domain.*;
import com.xd.pre.modules.sys.mapper.DouyinRechargePhoneMapper;
import com.xd.pre.modules.sys.mapper.JdMchOrderMapper;
import com.xd.pre.modules.sys.mapper.JdOrderPtMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DouYinHuaDanService {

    @Autowired
    private PcAppStoreService pcAppStoreService;
    @Autowired
    @Lazy
    private DouyinService douyinService;
    @Resource
    private DouyinRechargePhoneMapper douyinRechargePhoneMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource
    private JdOrderPtMapper jdOrderPtMapper;
    @Resource
    private JdMchOrderMapper jdMchOrderMapper;

    public R match(JdMchOrder jdMchOrder, JdAppStoreConfig storeConfig, JdLog jdLog) {
        PreTenantContextHolder.setCurrentTenantId(jdMchOrder.getTenantId());
        TimeInterval timer = DateUtil.timer();
        log.info("订单号{}，用户ip:{},话单，匹配金额;{}", jdMchOrder.getTradeNo(), JSON.toJSONString(jdLog), storeConfig.getSkuPrice());
        DouyinAppCk douyinAppCk = douyinService.randomDouyinAppCk(jdMchOrder, storeConfig, false);
        List<DouyinDeviceIid> douyinDeviceIids = douyinService.getDouyinDeviceIids(jdMchOrder);
        DouyinRechargePhone douyinRechargePhone = randomDouyinRechargePhone(jdMchOrder, storeConfig);
        OkHttpClient client = pcAppStoreService.buildClient();
        if (ObjectUtil.isNull(douyinAppCk) || ObjectUtil.isNull(douyinDeviceIids) || ObjectUtil.isNull(douyinRechargePhone) || ObjectUtil.isNull(client)) {
            log.error("订单号:{}，任意一个有问题，为空,请查看日志", jdMchOrder.getTradeNo());
            return null;
        }
        log.info("订单号:{}执行下单操作", jdMchOrder.getTradeNo());
        HuaDanDto huaDanDto = HuaDanDto.builder().iid(douyinDeviceIids.get(PreConstant.ZERO).getIid())
                .device_id(douyinDeviceIids.get(PreConstant.ZERO).getDeviceId())
                .client(client).ck(douyinAppCk.getCk()).price(storeConfig.getSkuPrice().intValue())
                .rechargePhone(douyinRechargePhone.getRechargePhone()).build();
        huaDanDto = findSkuList(huaDanDto);
        if (ObjectUtil.isNull(huaDanDto) || ObjectUtil.isNull(huaDanDto.getHuaDanSkuVo())) {
            redisTemplate.delete("话单锁定:" + douyinRechargePhone.getId());
            log.info("订单号:{},当前话单查询sku失败:{}", jdMchOrder.getTradeNo(), douyinRechargePhone.getRechargePhone());
            return null;
        }
        huaDanDto = calcSkuPrice(huaDanDto);
        if (ObjectUtil.isNull(huaDanDto) || !huaDanDto.getCheck()) {
            redisTemplate.delete("话单锁定:" + douyinRechargePhone.getId());
            log.error("订单号:{}计价失败:{}", jdMchOrder.getTradeNo());
        }
        log.info("开始下单");
        for (int i = 0; i < 3; i++) {
            huaDanDto.setDevice_id(douyinDeviceIids.get(i).getDeviceId());
            huaDanDto.setIid(douyinDeviceIids.get(i).getIid());
            HuaDanDto huaDanDtoT = mainCreateOrder(huaDanDto, jdMchOrder);
            if (ObjectUtil.isNull(huaDanDtoT) || StrUtil.isBlank(huaDanDtoT.getOrderId())) {
                log.error("订单号：{}", jdMchOrder.getTradeNo());
            }
            log.info("下单，订单号:{},成功msg:{}", jdMchOrder.getTradeNo(), JSON.toJSONString(huaDanDtoT));
            huaDanDto = huaDanDtoT;
            break;
        }
        if (ObjectUtil.isNull(huaDanDto) || StrUtil.isBlank(huaDanDto.getOrderId())) {
            redisTemplate.delete("话单锁定:" + douyinRechargePhone.getId());
        }
        Integer payType = douyinService.getPayType();
        PayDto payDto = PayDto.builder().ck(douyinAppCk.getCk()).device_id(huaDanDto.getDevice_id()).iid(huaDanDto.getIid()).pay_type(payType + "")
                .orderId(huaDanDto.getOrderId()).userIp(jdLog.getIp()).build();
        log.info("订单号:{}开始获取支付链接", jdMchOrder.getTradeNo());
//        if (getPayReUrl(jdMchOrder, jdLog, timer, client, payDto)) return null;
        String payReUrl = douyinService.getPayReUrl(jdMchOrder, jdLog, timer, client, payDto);
        if (StrUtil.isBlank(payReUrl)) {
            return null;
        }
        Boolean isLockMath = redisTemplate.opsForValue().setIfAbsent("匹配锁定成功:" + jdMchOrder.getTradeNo(), JSON.toJSONString(jdMchOrder),
                storeConfig.getExpireTime(), TimeUnit.MINUTES);
        if (!isLockMath) {
            log.error("订单号{}，当前已经匹配了,请查看数据库msg:{}", jdMchOrder.getTradeNo(), jdMchOrder.getTradeNo());
            return null;
        }
        JdOrderPt jdOrderPtDb = JdOrderPt.builder().orderId(payDto.getOrderId()).ptPin(douyinAppCk.getUid()).success(PreConstant.ZERO)
                .expireTime(DateUtil.offsetMinute(new Date(), storeConfig.getPayIdExpireTime())).createTime(new Date()).skuPrice(storeConfig.getSkuPrice())
                .wxPayExpireTime(DateUtil.offsetMinute(new Date(), storeConfig.getPayIdExpireTime()))
                .skuName(storeConfig.getSkuName()).skuId(storeConfig.getSkuId())
                .createTime(new Date()).skuPrice(storeConfig.getSkuPrice())
                .hrefUrl(payReUrl).weixinUrl(payReUrl).wxPayUrl(payReUrl)
                .isWxSuccess(PreConstant.ONE).isMatch(PreConstant.ONE).currentCk(douyinAppCk.getCk())
                .mark(JSON.toJSONString(payDto))
                .cardNumber(douyinRechargePhone.getRechargePhone())
                .wphCardPhone(JSON.toJSONString(douyinRechargePhone))
                .build();
        this.jdOrderPtMapper.insert(jdOrderPtDb);
        log.info("订单号{}，放入数据数据为msg:{}", jdMchOrder.getTradeNo(), JSON.toJSONString(jdOrderPtDb));
        if (ObjectUtil.isNotNull(jdMchOrder)) {
            long l = (System.currentTimeMillis() - jdMchOrder.getCreateTime().getTime()) / 1000;
            jdMchOrder.setMatchTime(l - 1);
            jdMchOrder.setOriginalTradeNo(jdOrderPtDb.getOrderId());
            jdMchOrder.setOriginalTradeId(jdOrderPtDb.getId());
            jdMchOrderMapper.updateById(jdMchOrder);
            log.info("当前");
            log.info("订单号{}，完成匹配:时间戳{}", jdMchOrder.getTradeNo(), timer.interval());
            return R.ok(jdOrderPtDb);
        }

        return null;
    }

    private String getPayReUrl(JdMchOrder jdMchOrder, JdLog jdLog, TimeInterval timer, OkHttpClient client, PayDto payDto) {
        String payReUrl = "";
        log.info("订单号{}，创建订单完成:时间戳{}", jdMchOrder.getTradeNo(), timer.interval());
        for (int i = 0; i < 5; i++) {
            log.info("订单号:{},第一次获取支付数据", jdMchOrder.getTradeNo());
            payReUrl = douyinService.payByOrderId(client, payDto, jdLog, jdMchOrder);
            if (StrUtil.isNotBlank(payReUrl)) {
                break;
            }
        }
        log.info("订单号{}，获取支付链接成功:时间戳{}", jdMchOrder.getTradeNo(), timer.interval());
        if (StrUtil.isBlank(payReUrl) && ObjectUtil.isNotNull(jdMchOrder)) {
            return null;
        }
        return payReUrl;
    }


    private DouyinRechargePhone randomDouyinRechargePhone(JdMchOrder jdMchOrder, JdAppStoreConfig storeConfig) {
        log.info("订单号:{},开始查询话单", jdMchOrder.getTradeNo());
        LambdaQueryWrapper<DouyinRechargePhone> douyinRechargePhoneLambdaQueryWrapper = Wrappers.lambdaQuery();
        douyinRechargePhoneLambdaQueryWrapper.eq(DouyinRechargePhone::getPrice, storeConfig.getSkuPrice())
                .eq(DouyinRechargePhone::getIsEnable, PreConstant.ONE)
                .eq(DouyinRechargePhone::getOrderStatus, PreConstant.ZERO)
                .orderByAsc(DouyinRechargePhone::getId);
        Set<String> huadans = redisTemplate.keys("话单锁定:*");
        if (CollUtil.isNotEmpty(huadans)) {
            List<Integer> ids = huadans.stream().map(it -> it.split("话单锁定:")[1]).map(it -> Integer.valueOf(it)).collect(Collectors.toList());
            douyinRechargePhoneLambdaQueryWrapper.notIn(DouyinRechargePhone::getId, ids);
        }
        log.info("订单号:{},当前订单锁定的账号", jdMchOrder.getTradeNo());
        //TODO 锁定正在使用的账号
        Integer rechargeCount = douyinRechargePhoneMapper.selectCount(douyinRechargePhoneLambdaQueryWrapper);
        if (rechargeCount < 0) {
            log.error("当前没有要充值的手机号码。请添加话单");
            return null;
        }
        int pageIndex = PreUtils.randomCommon(0, rechargeCount - 1, 1)[0];
        Page<DouyinRechargePhone> page = new Page<>(pageIndex, PreConstant.ONE);
        page = douyinRechargePhoneMapper.selectPage(page, douyinRechargePhoneLambdaQueryWrapper);
        DouyinRechargePhone douyinRechargePhone = page.getRecords().get(PreConstant.ZERO);
        log.info("订单号{},当前要充值的账号", jdMchOrder.getTradeNo());
        Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent("话单锁定:" + douyinRechargePhone.getId(), JSON.toJSONString(douyinRechargePhone), 420, TimeUnit.SECONDS);
        if (!ifAbsent) {
            log.info("订单号:{}当前这个话单有人正在匹配，重新挑选", jdMchOrder.getTradeNo());
            return null;
        }
        return douyinRechargePhone;
    }

    public void selectOrderStataus(JdOrderPt jdOrderPt, JdMchOrder jdMchOrder) {
        String wphCardPhone = jdOrderPt.getWphCardPhone();
        DouyinRechargePhone douyinRechargePhone = JSON.parseObject(wphCardPhone, DouyinRechargePhone.class);
        PreTenantContextHolder.setCurrentTenantId(jdMchOrder.getTenantId());
        log.info("订单号{}，开始查询订单", jdMchOrder.getTradeNo());
        DouyinDeviceIid douyinDeviceIid = JSON.parseObject(jdOrderPt.getMark(), DouyinDeviceIid.class);
        for (int i = 0; i < 5; i++) {
            String url = String.format("https://aweme.snssdk.com/aweme/v1/commerce/order/detailInfo/?" +
                            "device_id=%s&aid=1128&order_id=%s&app_name=aweme&channel=dy_tiny_juyouliang_dy_and24&iid=%s",
                    douyinDeviceIid.getDeviceId(), jdOrderPt.getOrderId(), douyinDeviceIid.getIid());
            String body = HttpRequest.get(url).header("cookie", jdOrderPt.getCurrentCk()).execute().body();
            if (StrUtil.isBlank(body)) {
                log.warn("订单号{}，查询订单话单结果msg:{}", body);
                continue;
            }
            log.info("订单号{}，查询订单数话单据订单结果msg:{}", jdMchOrder.getTradeNo(), body);
            String order_detail_info = JSON.parseObject(body).getString("order_detail_info");
            String shop_order_status_info = JSON.parseObject(order_detail_info).getString("shop_order_status_info");
            Integer order_status = JSON.parseObject(shop_order_status_info).getInteger("order_status");
            String order_status_desc = JSON.parseObject(shop_order_status_info).getString("order_status_desc");
            log.info("订单号:{},当前订单状态msg:{}", jdMchOrder.getTradeNo(), shop_order_status_info);
            jdOrderPt.setHtml(shop_order_status_info);
            PreTenantContextHolder.setCurrentTenantId(jdMchOrder.getTenantId());
            jdOrderPtMapper.updateById(jdOrderPt);
            //  `order_status` int(10) NOT NULL DEFAULT '0' COMMENT '0，还未充值，1充值中，2充值完成，3充值失败
            if (ObjectUtil.isNotNull(order_status) && order_status == 5) {
                log.info("订单号:{},手机号:{},话单id：{},", jdMchOrder.getTradeNo(), douyinRechargePhone.getRechargePhone(), douyinRechargePhone.getId());
                jdOrderPt.setSuccess(PreConstant.ONE);
                jdOrderPt.setSuccess(PreConstant.ONE);
                jdOrderPt.setPaySuccessTime(new Date());
                jdOrderPt.setOrgAppCk(order_status_desc);
                jdMchOrder.setStatus(PreConstant.TWO);
                douyinRechargePhone.setOrderStatus(PreConstant.TWO);
                PreTenantContextHolder.setCurrentTenantId(jdMchOrder.getTenantId());
                douyinRechargePhoneMapper.updateById(douyinRechargePhone);
                jdOrderPtMapper.updateById(jdOrderPt);
                jdMchOrderMapper.updateById(jdMchOrder);
            }
            if (ObjectUtil.isNotNull(order_status) && order_status == 3) {
                log.info("订单号:{},当前订单充值中", jdMchOrder.getTradeNo());
                redisTemplate.opsForValue().set("话单锁定:" + douyinRechargePhone.getId(), JSON.toJSONString(douyinRechargePhone), 420, TimeUnit.SECONDS);
                douyinRechargePhone.setOrderStatus(PreConstant.ONE);
                PreTenantContextHolder.setCurrentTenantId(jdMchOrder.getTenantId());
                jdOrderPt.setOrgAppCk(order_status_desc);
                jdOrderPtMapper.updateById(jdOrderPt);
                douyinRechargePhoneMapper.updateById(douyinRechargePhone);
            }
            return;
        }
    }


    private HuaDanDto findSkuList(HuaDanDto huaDanDto) {
        try {
            String sceneSkuListUrl = String.format("https://ec3-core-lq.ecombdapi.com/ve/topup/sceneSkuList?sceneId=MobileBalance&account=%s&iid=%s&device_id=%s",
                    huaDanDto.getRechargePhone(), huaDanDto.getIid(), huaDanDto.getDevice_id());
            Request request = new Request.Builder()
                    .url(sceneSkuListUrl)
                    .get()
                    .addHeader("user-agent", "com.ss.android.ugc.aweme/200001 (Linux; U; Android 9; zh_CN; Redmi 8A; Build/PKQ1.190319.001; Cronet/TTNetVersion:3a37693c 2022-02-10 QuicVersion:775bd845 2021-12-24)")
                    .addHeader("Cookie", huaDanDto.getCk())
                    .build();
            Response execute = huaDanDto.getClient().newCall(request).execute();
            String skuStr = execute.body().string();
            if (skuStr.contains("请输入正确的手机号码")) {
                log.error("手机号码不对msg:{}", huaDanDto.getRechargePhone());
                return null;
            }
            String sections = JSON.parseObject(JSON.parseObject(skuStr).getString("data")).getString("sections");
            JSONObject jsonObject = JSON.parseArray(sections, JSONObject.class).get(0);
            String skuList = jsonObject.getString("skuList");
            List<HuaDanSkuVo> huaDanSkuVos = JSON.parseArray(skuList, HuaDanSkuVo.class);
            huaDanSkuVos = huaDanSkuVos.stream().filter(it -> StrUtil.isNotBlank(it.getProductId())).filter(it -> it.getPrice() > 0).collect(Collectors.toList());
            log.info("当前查询的话单为msg:{}", JSON.toJSONString(huaDanSkuVos));
            Map<String, HuaDanSkuVo> mapHuaDan = huaDanSkuVos.stream().collect(Collectors.toMap(it -> it.getPriceSpec(), it -> it));
            String key = huaDanDto.getPrice() + "元";
            HuaDanSkuVo huaDanSkuVo = mapHuaDan.get(key);
            if (ObjectUtil.isNull(huaDanSkuVo)) {
                return null;
            }
            huaDanDto.setHuaDanSkuVo(huaDanSkuVo);
            log.info("充值店铺为msg:{}", huaDanSkuVo);
            return huaDanDto;
        } catch (Exception e) {
            log.error("查询sku报错msg:{}", e.getMessage());
        }
        return null;
    }

    public HuaDanDto calcSkuPrice(HuaDanDto huaDanDto) {
        try {
            String calcUrl = String.format("https://ec3-core-lq.ecombdapi.com/ve/topup/calcSkuPrice?iid=%s&device_id=%s", huaDanDto.getIid(), huaDanDto.getDevice_id());
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, String.format("productId=%s&skuId=%s", huaDanDto.getHuaDanSkuVo().getProductId()
                    , huaDanDto.getHuaDanSkuVo().getSkuId()));
            Request request = new Request.Builder()
                    .url(calcUrl)
                    .post(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("Cookie", huaDanDto.getCk())
                    .build();
            Response response = huaDanDto.getClient().newCall(request).execute();
            String checkManey = response.body().string();
            JSONObject data = JSON.parseObject(JSON.parseObject(checkManey).getString("data"));
            Integer discountPrice = data.getInteger("discountPrice");
            log.info("手机号:{},产品检验msg:{}", huaDanDto.getRechargePhone(), checkManey);
            if (discountPrice >= huaDanDto.getPrice() * 100 - 100 && discountPrice <= huaDanDto.getPrice() * 100) {
                huaDanDto.setCheck(true);
                huaDanDto.setDiscountPrice(data.getInteger("discountPrice"));
                huaDanDto.setOriginalPrice(data.getInteger("originalPrice"));
                return huaDanDto;
            }
        } catch (Exception e) {
            log.error("calcSkuPrice报错了msg:{}", huaDanDto.getRechargePhone());
        }
        return null;
    }

    public HuaDanDto mainCreateOrder(HuaDanDto huaDanDto, JdMchOrder jdMchOrder) {
        try {
            String createOrderUrl = String.format("https://ec3-core-lq.ecombdapi.com/ve/topup/createOrder?iid=%s&device_id=%s" +
                            "&channel=sem_shenma_dy_ls107&aid=1128&app_name=aweme&version_code=200000&version_name=20.0.0&device_platform=android&os=android&ssmix=a&device_type=Redmi+8A&device_brand=Xiaomi",
                    huaDanDto.getIid(), huaDanDto.getDevice_id());
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "platCouponId=&validDesc=%E6%89%8B%E6%9C%BA%E5%85%85%E5%80%BC" +
                    "&priceSubSpec=&source=recharge_order_homepage&isDiscount=false&sceneTitle=%E6%89%8B%E6%9C%BA%E8%AF%9D%E8%B4%B9&isSp=false&sceneId=MobileBalance&stock=1" +
                    "&priceSpec=" + huaDanDto.getPrice() + "%E5%85%83&" +
                    String.format("price=%s&shopId=%s&account=%s&skuId=%s&originalPrice=%s&productId=%s",
                            huaDanDto.getDiscountPrice(),
                            huaDanDto.getHuaDanSkuVo().getShopId(),
                            huaDanDto.getRechargePhone(),
                            huaDanDto.getHuaDanSkuVo().getSkuId(),
                            huaDanDto.getOriginalPrice(),
                            huaDanDto.getHuaDanSkuVo().getProductId()));
            Request request = new Request.Builder()
                    .url(createOrderUrl)
                    .post(body)
                    .addHeader("x-ss-dp", "1128")
                    .addHeader("user-agent", "com.ss.android.ugc.aweme/200001 (Linux; U; Android 9; zh_CN; Redmi 8A; Build/PKQ1.190319.001; Cronet/TTNetVersion:3a37693c 2022-02-10 QuicVersion:775bd845 2021-12-24)")
                    .addHeader("Cookie", huaDanDto.getCk())
                    .build();
            Response response = huaDanDto.getClient().newCall(request).execute();
            String orderStr = response.body().string();
            log.info("手机号:{}手机号充值返回数据", orderStr);
            response.close();
            if (StrUtil.isNotBlank(orderStr) && orderStr.contains("orderId")) {
                String data = JSON.parseObject(orderStr).getString("data");
                String orderId = JSON.parseObject(data).getString("orderId");
                huaDanDto.setOrderId(orderId);
                log.info("订单号:{},手机号,{}创建订单orderId:{}", jdMchOrder.getTradeNo(), huaDanDto.getRechargePhone(), huaDanDto.getOrderId());
                return huaDanDto;
            }
        } catch (Exception e) {
            log.error("订单号：{}，手机号:{},创建订单报错msg:{}", jdMchOrder.getTradeNo(), huaDanDto.getRechargePhone(), e.getMessage());
        }
        return null;
    }
}
