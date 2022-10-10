package com.xd.pre.modules.px.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xd.pre.common.utils.px.ChromeDriverUtils;
import com.xd.pre.common.utils.px.ParseWeiPayJsonUtil;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.modules.px.vo.reqvo.TokenVo;
import com.xd.pre.modules.px.vo.tmpvo.appstorevo.AppStoreVo;
import com.xd.pre.modules.sys.domain.*;
import com.xd.pre.modules.sys.mapper.JdAppStoreConfigMapper;
import com.xd.pre.modules.sys.mapper.JdDocumentMapper;
import com.xd.pre.modules.sys.mapper.JdOrderPtMapper;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WeiXinPayUrlService {


    @Resource
    private JdAppStoreConfigMapper jdAppStoreMapper;
    @Resource
    private JdOrderPtMapper jdOrderPtMapper;
    @Resource
    private JdDocumentMapper documentMapper;
    @Autowired
    private ProxyProductService proxyProductService;

    public TokenVo obtainPayUrl(TokenVo tokenVo) throws Exception {
        log.info("执行代码成功--------------------------------");
        log.info("当前参数msg:[tokenVo:{},需要的参数 headless tokenKey skuId]", tokenVo);
        JdAppStoreConfig jdAppStoreConfig = jdAppStoreMapper.selectOne(Wrappers.<JdAppStoreConfig>lambdaQuery()
                .eq(JdAppStoreConfig::getSkuId, tokenVo.getSkuId()));
        Assert.isTrue(ObjectUtil.isNotNull(jdAppStoreConfig), "skuId找不到配置");
        JdDocument jdDocument = getWeixinPayByJdSkuIdConfig(tokenVo.getTokenKey(), false, jdAppStoreConfig,
                new BigDecimal(tokenVo.getSkuPrice()), tokenVo.getSkuId());
        log.info("当前返回的msg:[weiXinPayByJdSkuIdConfig:{}]", jdDocument);
        tokenVo.setJdDocument(jdDocument);
        return tokenVo;
    }

    public TokenVo obtainPayUrl1(TokenVo tokenVo) throws Exception {
        log.info("执行代码成功--------------------------------");
        log.info("当前参数msg:[tokenVo:{},需要的参数 headless tokenKey skuId]", tokenVo);
        JdAppStoreConfig jdAppStoreConfig = jdAppStoreMapper.selectOne(Wrappers.<JdAppStoreConfig>lambdaQuery()
                .eq(JdAppStoreConfig::getSkuId, tokenVo.getSkuId()));
        Assert.isTrue(ObjectUtil.isNotNull(jdAppStoreConfig), "skuId找不到配置");
        JdDocument jdDocument = getWeixinPayByJdSkuIdConfig(tokenVo.getTokenKey(), true, jdAppStoreConfig,
                new BigDecimal(tokenVo.getSkuPrice()), tokenVo.getSkuId());
        log.info("当前返回的msg:[weiXinPayByJdSkuIdConfig:{}]", jdDocument);
        tokenVo.setJdDocument(jdDocument);
        tokenVo.setCk(jdDocument.getCk());
        return tokenVo;
    }

    public JdDocument getWeixinPayByJdSkuIdConfig(
            String tokenKey, Boolean isCk, JdAppStoreConfig jdAppStoreConfig, BigDecimal skuPrice, String skuId) throws InterruptedException {
        ChromeDriverUtils chromeDriverUtils = new ChromeDriverUtils();
        JdProxyIpPort oneIp = proxyProductService.getOneIp(0,0,false);
        if (ObjectUtil.isNull(oneIp)) {
            log.info("当前代理ip为空，不能启用");
            return null;
        }
        JdPathConfig jdPathConfig = proxyProductService.getPath();
        ChromeDriver driver = chromeDriverUtils.getChromeDriver(jdPathConfig.getHeadless() == 1 ? true : false, oneIp.getIp(), oneIp.getPort(), jdPathConfig.getChromeDriver());
        try {
            String tokenKey_url = String.format(jdAppStoreConfig.getUrl(), tokenKey);
            Map<String, AppStoreVo> appStoreUrlMap = jdAppStoreConfig.getAppStoreVo().stream().collect(Collectors.toMap(it -> it.getXpath(), it -> it));
            driver.get(tokenKey_url);
            log.info("执行确定");
            AppStoreVo appStoreVo1 = appStoreUrlMap.get("simplePopBtnSure");
            for (int i = 0; i < appStoreVo1.getLoopTime(); i++) {
                simplePopBtnSure(driver, appStoreVo1.getSleepTime());
            }
            AppStoreVo appStoreVo2 = appStoreUrlMap.get("submitOrder");
            for (int i = 0; i < appStoreVo2.getLoopTime(); i++) {
                submitOrder(driver, appStoreVo2.getSleepTime());
            }
            Set<Cookie> cookies = driver.manage().getCookies();
            log.info("cookies:{}", cookies);
            StringBuilder stringBuilder = new StringBuilder();
            for (Cookie cookie : cookies) {
                stringBuilder.append(cookie.getName() + "=" + cookie.getValue() + ";");
            }
            if (isCk) {
                log.info("{}", stringBuilder.toString());
                JdDocument document = new JdDocument();
                document.setCk(stringBuilder.toString());
                return document;
            }
            log.info("{}", stringBuilder.toString());
            System.out.println(stringBuilder.toString());
            AppStoreVo appStoreVo3 = appStoreUrlMap.get("payList");
            for (int i = 0; i < appStoreVo3.getLoopTime(); i++) {
                payList(driver, appStoreVo3.getSleepTime());
            }
            AppStoreVo appStoreVo4 = appStoreUrlMap.get("pay_confirm");
            for (int i = 0; i < appStoreVo4.getLoopTime(); i++) {
                confirmPay(driver, appStoreVo4.getSleepTime());
            }
            log.info("开始执行第二种");
            AppStoreVo appStoreVo5 = appStoreUrlMap.get("payBtn");
            for (int i = 0; i < appStoreVo5.getLoopTime(); i++) {
                payBtn(driver, appStoreVo5.getSleepTime());
            }
            AppStoreVo appStoreVo6 = appStoreUrlMap.get("logTime");
            for (int i = 0; i < appStoreVo6.getLoopTime(); i++) {
                log.info("获取日志");
                if (appStoreVo6.getSleepTime() != 0) {
                    Thread.sleep(appStoreVo6.getSleepTime());
                }
                LogEntries logEntries = driver.manage().logs().get(LogType.PERFORMANCE);
                String documentURL = ParseWeiPayJsonUtil.ParseWeiPayJsonLogEntries(logEntries);
                Date createTime = new Date();
                log.info("获取订单编号开始");
                String orderId = getOrderData(skuPrice, appStoreUrlMap, driver);
                log.info("获取订单编号结束");
                if (StrUtil.isNotBlank(documentURL) && StrUtil.isBlank(orderId)) {
                    log.info("------------------第二次获取订单编号开始");
                    orderId = getOrderData(skuPrice, appStoreUrlMap, driver);
                    log.info("-------------第二次获取订单编号结束");
                }
                log.info("当前支付msg:[链接-------------:{} 和订单号为:msg:{}}", documentURL, orderId);
                Cookie pt_pin = driver.manage().getCookieNamed("pt_pin");
                if (ObjectUtil.isNull(pt_pin) || StrUtil.isEmpty(pt_pin.getValue())) {
                    log.error("当前获取ck1，msg:[]");
                    return null;
                }
                JdDocument insertData = JdDocument.builder().documentUrl(documentURL).orderId(orderId).isSuccess(0).skuId(skuId).
                        createTime(createTime).ptPin("pt_pin=" + pt_pin.getValue()).build();
                JdDocument jdDocument = documentMapper.selectOne(Wrappers.<JdDocument>lambdaQuery()
                        .eq(JdDocument::getDocumentUrl, insertData.getDocumentUrl())
                        .eq(JdDocument::getOrderId, orderId));
                if (ObjectUtil.isNull(jdDocument)) {
                    documentMapper.insert(insertData);
                }
                return insertData;

            }
        } catch (Exception e) {
            log.error("发生异常msg:[e:{}]", e.getMessage());
        } finally {
            driver.quit();
        }
        return null;
    }

    private String getOrderData(BigDecimal skuPrice, Map<String, AppStoreVo> appStoreUrlMap, ChromeDriver driver) {
        try {
            AppStoreVo appStoreVo7 = appStoreUrlMap.get("orderlist_jdm");
            //获取我的订单
            for (int i = 0; i < appStoreVo7.getLoopTime(); i++) {
                orderlist_jdm(driver, appStoreVo7.getSleepTime());
            }
            //点击待支付订单
//        driver.find_element_by_xpath("//div[@data-tab-id='waitPay']").click()
            //待支付
            AppStoreVo appStoreVo8 = appStoreUrlMap.get("waitPay");
            for (int i = 0; i < appStoreVo8.getLoopTime(); i++) {
                waitPay(driver, appStoreVo8.getSleepTime());
            }
            //代支付元素
            Cookie pt_pin = driver.manage().getCookieNamed("pt_pin");
            if (ObjectUtil.isNull(pt_pin) || StrUtil.isEmpty(pt_pin.getValue())) {
                log.error("当前获取ck，msg:[]");
                return null;
            }
            log.info("获取订单号结束");
            InsertOderId(appStoreUrlMap, driver);
            log.info("获取订单号结束");
            log.info("匹配最新一条数据，并且价格相同,并且未成功的订单");
            List<JdOrderPt> jdOrderPts = jdOrderPtMapper.selectList(Wrappers.<JdOrderPt>lambdaQuery().eq(JdOrderPt::getPtPin, pt_pin.getValue())
                    .eq(JdOrderPt::getSuccess, 0)
                    .eq(JdOrderPt::getSkuPrice, skuPrice)
                    .orderByDesc(JdOrderPt::getExpireTime));
            if (CollUtil.isEmpty(jdOrderPts)) {
                return null;
            }
            JdOrderPt jdOrderPt = jdOrderPts.get(0);
            log.info("准备拉起这个订单msg:[jdOrderPt:{}]", jdOrderPt);
            String orderId = jdOrderPt.getOrderId();
            log.info("订单编号:[orderId:{}]", orderId);
            AppStoreVo appStoreVo10 = appStoreUrlMap.get("payByOrderId");
            for (int i = 0; i < appStoreVo10.getLoopTime(); i++) {
                log.info("暂不支付，不需要从这地方支付");
//            payByOrderId(driver, appStoreVo10.getSleepTime(), orderId);
            }
            return orderId;
        } catch (Exception e) {
            log.info("获取订单失败:msg:[]", e);
        }
        return null;
    }

    private void payByOrderData(WebElement webElement) {
        try {
            webElement.click();
        } catch (Exception e) {
            log.info("点击支付订单 错误");
        }
    }

    private void payByOrderId(ChromeDriver driver, Integer sleepTime, String orderId) {
        try {
            log.info("点击支付订单");
            Thread.sleep(sleepTime);
            String xpathStr = String.format("//div[@data-id='payNormal'and @data-order-id='%s']", orderId);
//            WebElement elementByXPath = driver.findElementByXPath("//div[@data-order-id='240955892486' and @data-id='payNormal']");
            List<WebElement> elementByXPaths = driver.findElementsByXPath(xpathStr);
            log.info("elementByXPath.getText():{}", elementByXPaths.get(0).getText());
            if (CollUtil.isNotEmpty(elementByXPaths)) {
                for (WebElement elementByXPath : elementByXPaths) {
                    payByOrderData(elementByXPath);
                }
            }
        } catch (Exception e) {
            log.info("点击支付订单 错误");
        }
    }


    /**
     * 根据ck插入订单号
     *
     * @param appStoreUrlMap
     * @param driver
     * @return
     */
    private void InsertOderId(Map<String, AppStoreVo> appStoreUrlMap, ChromeDriver driver) {
        try {
            AppStoreVo appStoreVo9 = appStoreUrlMap.get("payNormal");
            for (int i = 0; i < appStoreVo9.getLoopTime(); i++) {
//            returnMap.put("payNormal", payNormal);
//            returnMap.put("left_time", left_time);
                Map<String, List<WebElement>> returnMap = payNormal(driver, appStoreVo9.getSleepTime());
                if (CollUtil.isEmpty(returnMap)) {
                    return;
                }
                List<WebElement> payNormals = returnMap.get("payNormal");
                List<WebElement> left_times = returnMap.get("left_time");
//                List<WebElement> prices = returnMap.get("prices");
                List<WebElement> sku_names = returnMap.get("sku_names");
                log.info("获取pt_pin 和ck关联");
                Cookie pt_pin = driver.manage().getCookieNamed("pt_pin");
                if (ObjectUtil.isNull(pt_pin) || StrUtil.isEmpty(pt_pin.getValue())) {
                    log.error("当前获取ck失败msg:[]");
                    return;
                }
                for (int index = 0; index < payNormals.size(); index++) {
                    WebElement payNormal = payNormals.get(index);
                    WebElement left_time = left_times.get(index);
//                    WebElement price = prices.get(index);
                    if (ObjectUtil.isNotNull(payNormal.getText()) && payNormal.getText().contains("去支付")) {
                        String orderId = payNormal.getAttribute("data-order-id");
                        String leftTime = left_time.getText();
                        String xpathDesc = String.format("//div[@data-order-id='%s' and @class='order_item']/div[@class='oi_content']/div[@class='content']/div[@class='desc']", orderId);
                        String xpathPrice = String.format("//div[@data-order-id='%s' and @class='order_item']//span[@class='price']", orderId);
                        List<WebElement> xpathDescEs = driver.findElementsByXPath(xpathDesc);
                        WebElement xpathPriceE = driver.findElementByXPath(xpathPrice);
                        String sku_name = "";
                        for (WebElement xpathDescE : xpathDescEs) {
                            String text = xpathDescE.getText().trim();
                            if (StrUtil.isNotBlank(text)) {
                                sku_name = text;
                            }
                        }
                        String priceText = xpathPriceE.getAttribute("innerHTML");
                        String sku_price = PreUtils.getNum(priceText).get(0);
                        log.info("当前支付时间msg:[leftTime:{}]", leftTime);
                        Integer h = Integer.valueOf(PreUtils.getNum(leftTime).get(0));
                        Integer m = Integer.valueOf(PreUtils.getNum(leftTime).get(1));
                        DateTime offset = DateUtil.offset(new Date(), DateField.HOUR, h);
                        DateTime expire_time = DateUtil.offset(offset, DateField.MINUTE, m - 20);
                        log.info("当前待支付订单号为msg:[orderId:{}]", orderId);
                        log.info("当前待支付金额为msg:[sku_price:{}]", sku_price);
                        log.info("当前待支付购买商品名称为msg:[sku_name:{}]", sku_name);
                        JdOrderPt jdOrderPt = JdOrderPt.builder()
                                .orderId(orderId)
                                .ptPin(pt_pin.getValue())
                                .success(0)
                                .expireTime(expire_time)
                                .createTime(new Date())
                                .skuName(sku_name)
                                .skuPrice(new BigDecimal(sku_price)).build();
                        JdOrderPt jdOrderPtDb = jdOrderPtMapper.selectOne(Wrappers.<JdOrderPt>lambdaQuery().eq(JdOrderPt::getOrderId, orderId));
                        if (ObjectUtil.isNull(jdOrderPtDb)) {
                            jdOrderPtMapper.insert(jdOrderPt);
                        }

                    }
                }
            }
        } catch (Exception e) {
            log.error("获取订单号失败，请从新获取");
        }
        return;
    }

    private Map<String, List<WebElement>> payNormal(ChromeDriver driver, Integer sleepTime) {
        try {
            Map<String, List<WebElement>> returnMap = new HashMap<>();
            log.info("获取待支付订单");
            if (sleepTime != 0) {
                Thread.sleep(sleepTime);
            }
            List<WebElement> payNormal = driver.findElementsByXPath("//div[@data-id='payNormal']");
            List<WebElement> left_time = driver.findElementsByXPath("//div[@class='left_time']");
            List<WebElement> prices = driver.findElementsByXPath("//span[@class='price']");
            List<WebElement> sku_names = driver.findElementsByXPath("//div[@class='oi_content']/div[@class='content']/div[@class='desc']");
            returnMap.put("payNormal", payNormal);
            returnMap.put("left_time", left_time);
            returnMap.put("prices", prices);
            returnMap.put("sku_names", sku_names);
            if (CollUtil.isNotEmpty(payNormal) && CollUtil.isNotEmpty(left_time)) {
                return returnMap;
            }
        } catch (Exception e) {
            log.info("获取待支付订单 错误");
        }
        return null;
    }

    private void waitPay(ChromeDriver driver, Integer sleepTime) {
        try {
            log.info("点击待支付订单");
            if (sleepTime != 0) {
                Thread.sleep(sleepTime);
            }
            driver.findElementByXPath("//div[@data-tab-id='waitPay']").click();
        } catch (Exception e) {
            log.info("点击待支付订单 错误");
        }
    }


    private void orderlist_jdm(ChromeDriver driver, Integer sleepTime) {
        try {
            log.info("点击我的订单");
            if (sleepTime != 0) {
                Thread.sleep(sleepTime);
            }
            driver.get("https://wqs.jd.com/order/orderlist_jdm.shtml#/");
        } catch (Exception e) {
            log.info("点击我的订单 错误");
        }
    }

    private void payBtn(ChromeDriver driver, Integer sleepTime) {
        try {
            log.info("第二种提交微信支付");
            if (sleepTime != 0) {
                Thread.sleep(sleepTime);
            }
            driver.findElementByXPath("//div[@class='payBtn']").click();
        } catch (Exception e) {
            log.info("第二种提交微信支付 错误");
        }
    }

    private void payList(ChromeDriver driver, Integer sleepTime) {
        try {
            log.info("选择支付方式");
            if (sleepTime != 0) {
                Thread.sleep(sleepTime);
            }
            driver.findElementByXPath("//ul[@class='list pay-list p-other-pay-list']").click();
        } catch (Exception e) {
            log.info("选择支付方式，错误");
        }
    }

    private void submitOrder(ChromeDriver driver, Integer sleepTime) {
        try {
            log.info("提交订单");
            if (sleepTime != 0) {
                Thread.sleep(sleepTime);
            }
            driver.findElementById("submitOrder").click();
        } catch (Exception e) {
            log.info("执行失败提交订单");
        }
    }

    private void simplePopBtnSure(ChromeDriver driver, Integer sleepTime) {
        try {
            log.info("点击确认");
            if (sleepTime != 0) {
                Thread.sleep(sleepTime);
            }
            driver.findElementById("simplePopBtnSure").click();
        } catch (Exception e) {
            log.info("执行失败点击确认");
        }
    }


    private void confirmPay(ChromeDriver driver, Integer sleepTime) {
        try {
            log.info("第一种开始点击微信支付");
            if (sleepTime != 0) {
                Thread.sleep(sleepTime);
            }
            driver.findElementByXPath("//a[@class='btn pay-next confirm-pay']").click();
        } catch (Exception e) {
            log.info("第一种错误");
        }
    }
}
