package com.xd.pre.douyinnew;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xd.pre.jddj.douy.Douyin3;
import com.xd.pre.modules.px.douyin.huadan.HuaDanDto;
import com.xd.pre.modules.px.douyin.huadan.HuaDanSkuVo;
import com.xd.pre.pcScan.Demo;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class DouYinHuaDan {
    public static void main(String[] args) throws Exception {
        String device_id = "70685221970";
        String iid = "374277199890590";
        Map<String, String> ipAndPort = Douyin3.getIpAndPort();
        String rechargePhone = "18408282245";
        String ck = "install_id=119169286681983; ttreq=1$c937432add1ac5543b40dc8b95cb769bf024bf3a; passport_csrf_token=dc084fdfd9182b2006ac015d23d5094e; passport_csrf_token_default=dc084fdfd9182b2006ac015d23d5094e; d_ticket=5d8498d9c5c57a18f23083f8b948b45743690; multi_sids=659356656346136%3A140a336dd81551eaa30bc0e9e8d336fd; odin_tt=31cfa0089dd41156d7ef15dc965652aac3992c75cf21a7028e7de03cc06bad99000ef807e63e702274ccb12447d4a5d88eb3277edf255dc0b5f5e277b611155de832de5a7ffbb6acd70c6d5f7e1ad8f9; n_mh=8nysT__BxDL_VpPZTRMYKZZSN1pywPhZ9o63MSmzGLg; passport_assist_user=CkCRydX49tKRiP6NfppL8EZXqhP7I0lHXjcq-1NuFi9tetbHhO7j8WgKWcNY0u1c2_pwQmIxWsLy25zu5vuCS4y2GkgKPEawcjcdGGFhQ7XJU9Cvcme37ad7_x2LoXTiOHQl20bPqoQm-Xexq_YwQPA0X1fytaQzn-aCrETNNkRTDBDpip0NGImv1lQiAQMRQLcc; sid_guard=140a336dd81551eaa30bc0e9e8d336fd%7C1664383681%7C5183999%7CSun%2C+27-Nov-2022+16%3A48%3A00+GMT; uid_tt=1e4686eabe61b69fd57f1db3639b39b0; uid_tt_ss=1e4686eabe61b69fd57f1db3639b39b0; sid_tt=140a336dd81551eaa30bc0e9e8d336fd; sessionid=140a336dd81551eaa30bc0e9e8d336fd; sessionid_ss=140a336dd81551eaa30bc0e9e8d336fd; msToken=DefrbKNjA4krIh7tp5KF_ZXXM1__4BIGoe2_r-2pbIFokQpdlsAe8eodr9epNPRS43Yu3Wpkh4HktFYRO-i2ASuuPCj8e7LOFmIy0hm1yEw=";
        Integer price = 10;
        OkHttpClient client = Demo.getOkHttpClient(ipAndPort.get("ip"), Integer.valueOf(ipAndPort.get("port")));
        HuaDanDto huaDanDto = HuaDanDto.builder().iid(iid).device_id(device_id).client(client).ck(ck)
                .price(price).ip(ipAndPort.get("ip")).port(Integer.valueOf(ipAndPort.get("port")))
                .rechargePhone(rechargePhone).build();
        huaDanDto = findSkuList(huaDanDto);
        huaDanDto = calcSkuPrice(huaDanDto);
        log.info("开始下单手机号msg:{}", huaDanDto.getRechargePhone());
        huaDanDto= mainCreateOrder(huaDanDto);
        log.info("订单编号msg:{}",huaDanDto.getOrderId());
    }

    private static HuaDanDto findSkuList(HuaDanDto huaDanDto) {
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

    public static HuaDanDto calcSkuPrice(HuaDanDto huaDanDto) {
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

    public static HuaDanDto mainCreateOrder(HuaDanDto huaDanDto) {
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
                log.info("手机号,{}创建订单orderId:{}", huaDanDto.getRechargePhone(), huaDanDto.getOrderId());
                return huaDanDto;
            }
        } catch (Exception e) {
            log.error("手机号:{},创建订单报错msg:{}", huaDanDto.getRechargePhone(), e.getMessage());
        }
        return null;
    }
}
