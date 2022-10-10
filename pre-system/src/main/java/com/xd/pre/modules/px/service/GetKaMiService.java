package com.xd.pre.modules.px.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xd.pre.common.utils.R;
import com.xd.pre.common.utils.px.ChromeDriverUtils;
import com.xd.pre.modules.px.vo.reqvo.KaMiVo;
import com.xd.pre.modules.px.vo.resvo.TokenKeyResVo;
import com.xd.pre.modules.sys.domain.*;
import com.xd.pre.modules.sys.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GetKaMiService {

    @Resource
    private JdAppStoreConfigMapper jdAppStoreConfigMapper;
    @Resource
    private JdOrderPtMapper jdOrderPtMapper;
    @Resource
    private JdDocumentMapper documentMapper;
    @Resource
    private JdLocalUrlMapper jdLocalUrlMapper;
    @Resource
    private JdCkMapper jdCkMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource
    private JdAppStoreConfigMapper jdAppStoreMapper;

    @Autowired
    private ProxyProductService proxyProductService;

    public void getKaMiByGoogle(KaMiVo kaMiVo) {
        TimeInterval timer = DateUtil.timer();
        ChromeDriverUtils chromeDriverUtils = new ChromeDriverUtils();
        JdProxyIpPort oneIp = proxyProductService.getOneIp(0,0,false);
        if (ObjectUtil.isNull(oneIp)) {
            log.info("当前代理ip为空，不能启用");
            return;
        }
        JdPathConfig jdPathConfig = proxyProductService.getPath();
        ChromeDriver driver = chromeDriverUtils.getChromeDriver(jdPathConfig.getHeadless()==1?true:false, oneIp.getIp(), oneIp.getPort(),jdPathConfig.getChromeDriver());
//        ChromeDriver driver = chromeDriverUtils.getChromeDriver(true);
//        ChromeDriver driver = null;
        try {
            log.info("1--------------------------" + timer.interval());
            JdCk jdCk = jdCkMapper.selectOne(Wrappers.<JdCk>lambdaQuery().eq(JdCk::getPtPin, kaMiVo.getPtPin()));
            Assert.isTrue(ObjectUtil.isNotNull(jdCk), "ck为空");
            List<JdLocalUrl> jdLocalUrls = jdLocalUrlMapper.selectList(Wrappers.<JdLocalUrl>lambdaQuery().eq(JdLocalUrl::getGroupNum, kaMiVo.getGroupNum()).eq(JdLocalUrl::getIsEnable, 1));
            Map<String, JdLocalUrl> mapUrls = jdLocalUrls.stream().collect(Collectors.toMap(it -> it.getTag(), it -> it));
            JdLocalUrl getTokenKey1 = mapUrls.get("getTokenKey");
            log.info("1--------------------------" + timer.interval());
            String genTokenBody =null;
            log.info("2--------------------------" + timer.interval());
            R r = com.alibaba.fastjson.JSON.parseObject(genTokenBody, R.class);
            TokenKeyResVo tokenKeyResVo = com.alibaba.fastjson.JSON.parseObject(r.getData().toString(), TokenKeyResVo.class);
            if (StrUtil.isBlank(tokenKeyResVo.getTokenKey())) {
                return;
            }
            JdAppStoreConfig jdAppStoreConfig = jdAppStoreMapper.selectList(Wrappers.<JdAppStoreConfig>lambdaQuery()
                    .eq(JdAppStoreConfig::getGroupNum, kaMiVo.getGroupNum())).get(0);
            log.info("3--------------------------" + timer.interval());
            String tokenKey_url = String.format(jdAppStoreConfig.getUrl(), tokenKeyResVo.getTokenKey());
            driver.get(tokenKey_url);
            log.info("4--------------------------" + timer.interval());
            String orderKaMiUrl = String.format("https://gamerecg.m.jd.com/game/detail.action?orderId=%s", kaMiVo.getOrderId());
            driver.get(orderKaMiUrl);
            log.info("5--------------------------" + timer.interval());
            List<WebElement> elementsByXPath = driver.findElementsByXPath("//li[@class='kwd']//span");
            if (CollUtil.isNotEmpty(elementsByXPath) && elementsByXPath.size() == 4) {
                String cardNumber = elementsByXPath.get(1).getText().trim();
                String carMy = elementsByXPath.get(3).getText().trim();
                if (StrUtil.isNotBlank(cardNumber) && StrUtil.isNotBlank(carMy)) {
                    log.info("当前获取的值卡号和卡密的值为:msg:[cardNumber:{},carMy:{}]", cardNumber, carMy);
                    JdDocument jdDocument = documentMapper.selectByOrderId(kaMiVo.getOrderId());
                    if (ObjectUtil.isNotNull(jdDocument)) {
                        jdDocument.setCardNumber(cardNumber);
                        jdDocument.setCarMy(carMy);
                        documentMapper.updateById(jdDocument);
                        log.info("6--------------------------" + timer.interval());
                    }
                }
            }

        } catch (Exception e) {
            log.info("获取订单失败，e:{}", e.getMessage());
        } finally {
            driver.quit();
        }

    }
}
