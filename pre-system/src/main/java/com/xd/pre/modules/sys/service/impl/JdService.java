package com.xd.pre.modules.sys.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xd.pre.common.constant.PreConstant;
import com.xd.pre.modules.px.vo.resvo.FlowingWaterResVo;
import com.xd.pre.modules.px.vo.resvo.StockRes;
import com.xd.pre.modules.sys.domain.JdAppStoreConfig;
import com.xd.pre.modules.sys.domain.JdMchOrder;
import com.xd.pre.modules.sys.domain.JdOrderPt;
import com.xd.pre.modules.sys.mapper.JdAppStoreConfigMapper;
import com.xd.pre.modules.sys.mapper.JdMchOrderMapper;
import com.xd.pre.modules.sys.mapper.JdOrderPtMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JdService {

    @Resource
    private JdOrderPtMapper jdOrderPtMapper;

    @Resource
    private JdAppStoreConfigMapper jdAppStoreConfigMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource
    private JdMchOrderMapper jdMchOrderMapper;

    public List<StockRes> selectStock() {
        List<StockRes> returnList = new ArrayList<>();
        log.info("查询库存");
        List<JdAppStoreConfig> jdAppStoreConfigs = jdAppStoreConfigMapper.selectList(Wrappers.<JdAppStoreConfig>lambdaQuery().eq(JdAppStoreConfig::getIsProduct, PreConstant.ONE));
        for (JdAppStoreConfig jdAppStoreConfig : jdAppStoreConfigs) {
            StockRes stockRes = new StockRes();
            returnList.add(stockRes);
            stockRes.setSkuName(jdAppStoreConfig.getSkuName());
            stockRes.setSkuId(jdAppStoreConfig.getSkuId());
            DateTime offset = DateUtil.offset(new Date(), DateField.MINUTE, -jdAppStoreConfig.getPayIdExpireTime());
            Integer stockDb = jdOrderPtMapper.selectStockDb(offset, jdAppStoreConfig.getSkuId());
            stockRes.setStock(stockDb);
            String stockRe = redisTemplate.opsForValue().get("stock:" + jdAppStoreConfig.getSkuId());
            if (StrUtil.isBlank(stockRe)) {
                stockRes.setProductStock(0);
            } else {
                stockRes.setProductStock(Integer.valueOf(stockRe));
            }
            log.debug("查询实时库存");
            LambdaQueryWrapper<JdOrderPt> realTimeStockWrapper = Wrappers.<JdOrderPt>lambdaQuery();
            Set<String> readyData = redisTemplate.keys("订单管理微信链接:*");
            if (CollUtil.isNotEmpty(readyData)) {
                List<String> orderIds = readyData.stream().map(it -> it.split(":")[1]).collect(Collectors.toList());
                log.debug("当前订单的实时key");
                realTimeStockWrapper.in(JdOrderPt::getOrderId, orderIds);
            }
            realTimeStockWrapper.eq(JdOrderPt::getSkuId, jdAppStoreConfig.getSkuId());
            Integer realTimeStock = this.jdOrderPtMapper.selectCount(realTimeStockWrapper);
            stockRes.setRealTimeStock(realTimeStock);
            log.debug("查询订单锁定库存");
            LambdaQueryWrapper<JdOrderPt> lockStockWrapper = Wrappers.<JdOrderPt>lambdaQuery();
            lockStockWrapper.eq(JdOrderPt::getSkuId, jdAppStoreConfig.getSkuId());
            Set<String> lockStockData = redisTemplate.keys("JD匹配锁定:*");
            if (CollUtil.isNotEmpty(lockStockData)) {
                List<String> orderIds = lockStockData.stream().map(it -> it.split(":")[1]).collect(Collectors.toList());
                log.debug("查询锁定库存");
                lockStockWrapper.in(JdOrderPt::getOrderId, orderIds);
                //去掉锁定的
                realTimeStockWrapper.notIn(JdOrderPt::getOrderId, orderIds);
                Integer lockStock = this.jdOrderPtMapper.selectCount(lockStockWrapper);
                stockRes.setLockStock(lockStock);
            }else{
                stockRes.setLockStock(PreConstant.ZERO);
            }
            log.debug("查询剩余可以匹配库存");
            Integer surplusStock = this.jdOrderPtMapper.selectCount(realTimeStockWrapper);
            stockRes.setSurplusStock(surplusStock);
        }
        return returnList;
    }


    public FlowingWaterResVo flowingWater(String startTime, String endTime) {
        Integer createOrderNums = jdMchOrderMapper.selectCount(Wrappers.<JdMchOrder>lambdaQuery()
                .ge(JdMchOrder::getCreateTime, startTime)
                .le(JdMchOrder::getCreateTime, endTime));
        Integer successOrderNums = jdMchOrderMapper.selectCount(Wrappers.<JdMchOrder>lambdaQuery()
                .ge(JdMchOrder::getCreateTime, startTime)
                .le(JdMchOrder::getCreateTime, endTime)
                .eq(JdMchOrder::getStatus, 2));

        BigDecimal totalFlowingWater = jdMchOrderMapper.selectTotalFlowingWater(startTime, endTime);
        BigDecimal successFlowingWater = jdMchOrderMapper.selectSuccessFlowingWater(startTime, endTime);
        BigDecimal failFlowingWater = jdMchOrderMapper.selectFailFlowingWater(startTime, endTime);
        BigDecimal successRate = successFlowingWater.divide(successFlowingWater.add(failFlowingWater).intValue() == 0
                ? new BigDecimal(1) : successFlowingWater.add(failFlowingWater), BigDecimal.ROUND_HALF_UP, 2);
        BigDecimal noMatchFlowingWater = jdMchOrderMapper.selectNoMatchFlowingWater(startTime, endTime);

        FlowingWaterResVo build = new FlowingWaterResVo().builder()
                .createOrderNums(createOrderNums)
                .successOrderNums(successOrderNums)
                .totalFlowingWater(totalFlowingWater)
                .successFlowingWater(successFlowingWater)
                .failFlowingWater(failFlowingWater)
                .successRate(successRate)
                .noMatchFlowingWater(noMatchFlowingWater).build();
        return build;
    }
}
