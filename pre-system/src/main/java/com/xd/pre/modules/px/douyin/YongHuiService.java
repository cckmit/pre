package com.xd.pre.modules.px.douyin;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xd.pre.common.constant.PreConstant;
import com.xd.pre.common.utils.R;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.modules.data.tenant.PreTenantContextHolder;
import com.xd.pre.modules.px.appstorePc.PcAppStoreService;
import com.xd.pre.modules.px.douyin.buyRender.BuyRenderParamDto;
import com.xd.pre.modules.px.douyin.pay.PayDto;
import com.xd.pre.modules.sys.domain.*;
import com.xd.pre.modules.sys.mapper.DouyinHexiaoPhoneMapper;
import com.xd.pre.modules.sys.mapper.JdMchOrderMapper;
import com.xd.pre.modules.sys.mapper.JdOrderPtMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class YongHuiService {


    @Autowired
    @Lazy
    private DouyinService douyinService;
    @Autowired
    private PcAppStoreService pcAppStoreService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Resource
    private JdOrderPtMapper jdOrderPtMapper;
    @Resource
    private JdMchOrderMapper jdMchOrderMapper;
    @Resource
    private DouyinHexiaoPhoneMapper douyinHexiaoPhoneMapper;

    public R match(JdMchOrder jdMchOrder, JdAppStoreConfig storeConfig, JdLog jdLog) {
        log.info("订单号:{}永辉开始匹配", jdMchOrder.getTradeNo());
        PreTenantContextHolder.setCurrentTenantId(jdMchOrder.getTenantId());
        TimeInterval timer = DateUtil.timer();
        DouyinAppCk douyinAppCk = douyinService.randomDouyinAppCk(jdMchOrder, storeConfig, false);
        List<DouyinDeviceIid> douyinDeviceIUseids = douyinService.getDouyinDeviceIids(jdMchOrder);
        OkHttpClient client = pcAppStoreService.buildClient();
        DouyinHexiaoPhone douyinHexiaoPhone = randomDouyinRechargePhone(jdMchOrder);
        if (ObjectUtil.isNull(douyinAppCk) || ObjectUtil.isNull(douyinDeviceIUseids) || ObjectUtil.isNull(douyinHexiaoPhone) || ObjectUtil.isNull(client)) {
            log.error("订单号:{}，任意一个有问题，为空,请查看日志", jdMchOrder.getTradeNo());
            return null;
        }
        BuyRenderParamDto buyRenderParamDto = JSON.parseObject(storeConfig.getConfig(), BuyRenderParamDto.class);
        Integer payType = douyinService.getPayType();
        PayDto payDto = douyinService.createOrder(client, buyRenderParamDto, payType, douyinAppCk, jdLog, jdMchOrder, douyinDeviceIUseids, timer,
                douyinHexiaoPhone.getHexiaoPhone());
        String payReUrl = douyinService.getPayReUrl(jdMchOrder, jdLog, timer, client, payDto);
        if (StrUtil.isBlank(payReUrl)) {
            log.error("订单号:{}", jdMchOrder.getTradeNo());
            return null;
        }
        Boolean isLockMath = redisTemplate.opsForValue().setIfAbsent("匹配锁定成功:" + jdMchOrder.getTradeNo(), JSON.toJSONString(jdMchOrder),
                storeConfig.getExpireTime(), TimeUnit.MINUTES);
        if (!isLockMath) {
            log.error("订单号{}，用会卡当前已经匹配了,请查看数据库msg:{}", jdMchOrder.getTradeNo(), jdMchOrder.getTradeNo());
            return null;
        }
        JdOrderPt jdOrderPtDb = JdOrderPt.builder().orderId(payDto.getOrderId()).ptPin(douyinAppCk.getUid()).success(PreConstant.ZERO)
                .skuName(storeConfig.getSkuName()).skuId(storeConfig.getSkuId())
                .expireTime(DateUtil.offsetMinute(new Date(), storeConfig.getPayIdExpireTime())).createTime(new Date()).skuPrice(storeConfig.getSkuPrice())
                .wxPayExpireTime(DateUtil.offsetMinute(new Date(), storeConfig.getPayIdExpireTime()))
                .isWxSuccess(PreConstant.ONE).isMatch(PreConstant.ONE).currentCk(douyinAppCk.getCk())
                .createTime(new Date()).skuPrice(storeConfig.getSkuPrice())
                .hrefUrl(payReUrl).weixinUrl(payReUrl).wxPayUrl(payReUrl)
                .wphCardPhone(douyinHexiaoPhone.getHexiaoPhone())
                .mark(JSON.toJSONString(payDto))
                .build();
        this.jdOrderPtMapper.insert(jdOrderPtDb);
        log.info("订单号{}，放入数据数据订单数据为msg:{}", jdMchOrder.getTradeNo(), JSON.toJSONString(jdOrderPtDb));
        long l = (System.currentTimeMillis() - jdMchOrder.getCreateTime().getTime()) / 1000;
        jdMchOrder.setMatchTime(l - 1);
        jdMchOrder.setOriginalTradeNo(jdOrderPtDb.getOrderId());
        jdMchOrder.setOriginalTradeId(jdOrderPtDb.getId());
        jdMchOrderMapper.updateById(jdMchOrder);
        log.info("订单号{},永辉，完成匹配:时间戳{}", jdMchOrder.getTradeNo(), timer.interval());
        return R.ok(jdOrderPtDb);
    }

    private DouyinHexiaoPhone randomDouyinRechargePhone(JdMchOrder jdMchOrder) {
        log.info("订单号随机核销电话号码:{}", jdMchOrder);
        LambdaQueryWrapper<DouyinHexiaoPhone> objectLambdaQueryWrapper = Wrappers.lambdaQuery();
        objectLambdaQueryWrapper.eq(DouyinHexiaoPhone::getIsEnable, PreConstant.ONE);
        objectLambdaQueryWrapper.ge(DouyinHexiaoPhone::getHexiaoPhoneEnd, new Date());
        Integer count = douyinHexiaoPhoneMapper.selectCount(objectLambdaQueryWrapper);
        if (count <= 0) {
            return null;
        }
        int pageIndex = PreUtils.randomCommon(0, count - 1, 1)[0];
        Page<DouyinHexiaoPhone> douyinHexiaoPhonePage = new Page<>(pageIndex, 1);
        douyinHexiaoPhonePage = douyinHexiaoPhoneMapper.selectPage(douyinHexiaoPhonePage, objectLambdaQueryWrapper);
        DouyinHexiaoPhone douyinHexiaoPhone = douyinHexiaoPhonePage.getRecords().get(PreConstant.ZERO);
        return douyinHexiaoPhone;
    }
}
