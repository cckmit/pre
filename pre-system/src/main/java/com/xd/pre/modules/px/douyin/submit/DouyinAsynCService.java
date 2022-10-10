package com.xd.pre.modules.px.douyin.submit;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.TimeInterval;
import com.xd.pre.common.utils.R;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.modules.px.douyin.DouyinService;
import com.xd.pre.modules.sys.domain.JdAppStoreConfig;
import com.xd.pre.modules.sys.domain.JdLog;
import com.xd.pre.modules.sys.domain.JdMchOrder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DouyinAsynCService {
    @Autowired
    private DouyinService douyinService;

    @Async("asyncPool")
    public R synDouyinProductStock(JdMchOrder jdMchOrderOr, JdAppStoreConfig storeConfig, JdLog jdLog, TimeInterval timer, OkHttpClient client, String payReUrl) {
        log.info("订单号:{},异步库存生成", jdMchOrderOr.getTradeNo());
        JdMchOrder jdMchOrderNew = new JdMchOrder();
        BeanUtil.copyProperties(jdMchOrderOr, jdMchOrderNew);
        String newOrderNo = PreUtils.getRandomString(4).toUpperCase();
        log.info("订单号:{},生成库存新订单号:{}", jdMchOrderOr.getTradeNo(),newOrderNo);
        jdMchOrderNew.setTradeNo(newOrderNo);
        jdMchOrderNew.setId(Integer.valueOf(PreUtils.getRandomNum(2)));
        return douyinService.douyinProductNewOrder(jdMchOrderNew, storeConfig, jdLog, timer, client, payReUrl);
    }

}
