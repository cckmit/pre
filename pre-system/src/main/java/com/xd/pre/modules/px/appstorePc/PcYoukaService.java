package com.xd.pre.modules.px.appstorePc;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import com.xd.pre.common.utils.R;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.modules.px.appstorePc.pcScan.Get_confirmQRCodeScanned;
import com.xd.pre.modules.px.appstorePc.pcScan.Get_oi_symmetry_encrypt;
import com.xd.pre.modules.px.appstorePc.pcScan.PcThorDto;
import com.xd.pre.modules.px.appstorePc.pcScan.ScanUtils;
import com.xd.pre.modules.px.appstorePc.pcScan.dto.PcOrderDto;
import com.xd.pre.modules.px.psscan.BuildPcThor;
import com.xd.pre.modules.px.psscan.PcPayQr;
import com.xd.pre.modules.px.psscan.PcQRCodeDto;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PcYoukaService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    PcAppStoreService pcAppStoreService;

    public R pcPayQr(PcPayQr pcPayQr) {
        String md5 = SecureUtil.md5(pcPayQr.getAppck());
        if (!md5.equals(pcPayQr.getSign())) {
            log.error("签名错误msg:{}");
            return R.error(600, "签证错误");
        }
        String pt_pin = PreUtils.get_pt_pin(pcPayQr.getAppck());
        String loginInfo = redisTemplate.opsForValue().get("PC:" + pt_pin);
        if (ObjectUtil.isNull(pt_pin) || StrUtil.isBlank(loginInfo)) {
                return R.error(701, "没有登录信息请调用登录");
        }
        BuildPcThor buildPcThor = JSON.parseObject(loginInfo, BuildPcThor.class);
        String body = HttpRequest.get("https://api.m.jd.com/api?appid=pc_home_page&functionId=getBaseUserInfo").header("referer", " https://home.jd.com/")
                .header("cookie", "thor=" + buildPcThor.getPcThorDto().getThor()).execute().body();
        Boolean success = JSON.parseObject(body).getBoolean("success");
        if (!success) {
            BuildPcThor pcThor = new BuildPcThor();
            BeanUtil.copyProperties(pcPayQr, pcPayQr);
            this.pcScan(pcThor);
            return R.error(702, "登录已经过期，请调用登录");
        }
        OkHttpClient client = buildClent(pcPayQr.getProxyIp(), pcPayQr.getProxyPort());
        Map<String, String> headerMap = PreUtils.buildIpMap(pcPayQr.getUserIp());
        //判断是否是5分钟之内的如果是的话。就直接刷新就是了不需要请求完了
        String pcOrderDtoRedisData = redisTemplate.opsForValue().get("YOUKA:ORDERID_REFRESH:" + pcPayQr.getOrderId());
        if (StrUtil.isNotBlank(pcOrderDtoRedisData)) {
            PcOrderDto pcOrderDto = JSON.parseObject(pcOrderDtoRedisData, PcOrderDto.class);
            R refresh = refresh(pcOrderDto, headerMap, client);
            if (ObjectUtil.isNotNull(refresh) && refresh.getCode() == HttpStatus.HTTP_OK) {
                pcOrderDto = JSON.parseObject(JSON.toJSONString(refresh.getData()), PcOrderDto.class);
                PcOrderDto pcOrderDtoreturn = new PcOrderDto();
                pcOrderDtoreturn.setBizpayurl(pcOrderDto.getBizpayurl());
                pcOrderDtoreturn.setOrderId(pcOrderDto.getOrderId());
                return R.ok(pcOrderDtoreturn);
            }
            return refresh;
        }
        //根据pc获取登录thor
        // 然后根据订单来请求
        String url301_1 = String.format("https://jiayouka.jd.com/order/pay/%s", pcPayQr.getOrderId());
        String payUrl301_1 = "";
        for (int i = 0; i < 4; i++) {
            payUrl301_1 = pcAppStoreService.getPayUrl301_2(JSON.toJSONString(buildPcThor.getPcThorDto()), url301_1, headerMap, client);
            if (StrUtil.isNotBlank(payUrl301_1)) {
                break;
            }
        }
        if (StrUtil.isBlank(payUrl301_1)) {
            return R.error(703, "请检查代理或者参数是否正确");
        }
        PcOrderDto pcOrderDto = PcOrderDto.builder().orderId(pcPayQr.getOrderId()).build();
        pcOrderDto.setPcThorDto(buildPcThor.getPcThorDto());
        pcOrderDto.setPayUrl301_1(payUrl301_1);
        String deviceId = PreUtils.getRandomString(90).toUpperCase();
        pcOrderDto.setDeviceId(deviceId);
        String fingerprint = PreUtils.getRandomString(32).toUpperCase();
        pcOrderDto.setFingerprint(fingerprint);
        String reqInfo = PreUtils.parseUrl(payUrl301_1).getParams().get("reqInfo");
        pcOrderDto.setReqInfo(reqInfo);
        String sign = PreUtils.parseUrl(payUrl301_1).getParams().get("sign");
        pcOrderDto.setIsYouka(true);//设置成油卡
        pcOrderDto.setSign(sign);
        for (int i = 0; i < 4; i++) {
            PcOrderDto pcOrderDtoT = pcAppStoreService.getPaySign(JSON.toJSONString(buildPcThor.getPcThorDto()), pcOrderDto, headerMap, client);
            if (ObjectUtil.isNotNull(pcOrderDtoT) && ObjectUtil.isNull(pcOrderDtoT.getOrderStatus())) {
                pcOrderDto = pcOrderDtoT;
                break;
            }
            if (ObjectUtil.isNotNull(pcOrderDtoT) && ObjectUtil.isNotNull(pcOrderDtoT.getOrderStatus() == 0)) {
                return R.error(704, pcOrderDtoT.getOrderInfo());
            }
            return R.error(710, "未知错误，有可能代理错误");
        }
        for (int i = 0; i < 4; i++) {
            PcOrderDto pcOrderDtoT = pcAppStoreService.weixinConfirm(JSON.toJSONString(buildPcThor.getPcThorDto()), pcOrderDto, headerMap, client);
            if (ObjectUtil.isNotNull(pcOrderDtoT)) {
                pcOrderDto = pcOrderDtoT;
                break;
            }
            return R.error(710, "未知错误，有可能代理错误");
        }
        R refresh = refresh(pcOrderDto, headerMap, client);
        if (ObjectUtil.isNotNull(refresh) && refresh.getCode() == HttpStatus.HTTP_OK) {
            pcOrderDto = JSON.parseObject(JSON.toJSONString(refresh.getData()), PcOrderDto.class);
            redisTemplate.opsForValue().set("YOUKA:ORDERID_REFRESH:" + pcOrderDto.getOrderId(), JSON.toJSONString(pcOrderDto), 5, TimeUnit.MINUTES);
            PcOrderDto pcOrderDtoreturn = new PcOrderDto();
            pcOrderDtoreturn.setBizpayurl(pcOrderDto.getBizpayurl());
            pcOrderDtoreturn.setOrderId(pcOrderDto.getOrderId());
            return R.ok(pcOrderDtoreturn);
        } else if (ObjectUtil.isNotNull(refresh)) {
            return refresh;
        }
        return R.error(710, "未知错误，有可能代理错误");
    }

    public R refresh(PcOrderDto pcOrderDto, Map<String, String> headerMap, OkHttpClient client) {
        for (int i = 0; i < 4; i++) {
            PcOrderDto pcOrderDtoT = pcAppStoreService.qrCodeSign(JSON.toJSONString(pcOrderDto.getPcThorDto()), pcOrderDto, headerMap, client);
            if (ObjectUtil.isNotNull(pcOrderDtoT)) {
                pcOrderDto = pcOrderDtoT;
                break;
            }
            return R.error(710, "未知错误，有可能代理错误");
        }
        log.info("getWeixinImageUrl:{}", pcOrderDto.getWeixinImageUrl());
        log.info("开始获取二维码msg:{}", "");
        for (int i = 0; i < 4; i++) {
            PcOrderDto pcOrderDtoT = getWeixinPayByUrl(JSON.toJSONString(pcOrderDto.getPcThorDto()), pcOrderDto, headerMap, client);
            if (ObjectUtil.isNotNull(pcOrderDtoT)) {
                pcOrderDto = pcOrderDtoT;
                break;
            }
            return R.error(710, "未知错误，有可能代理错误");
        }
        log.info("设置redis数据，刷新redis数据和真实的url数据");
        log.info("getBizpayurl:{}", pcOrderDto.getBizpayurl());
        return R.ok(pcOrderDto);
    }

    public PcOrderDto getWeixinPayByUrl(String thorStr, PcOrderDto pcOrderDto, Map<String, String> headerMap, OkHttpClient client) {
        try {
            PcThorDto pcThorDto = JSON.parseObject(thorStr, PcThorDto.class);
            PcOrderDto pcOrderDtoT = new PcOrderDto();
            BeanUtil.copyProperties(pcOrderDto, pcOrderDtoT);
            Request.Builder header = new Request.Builder().url(pcOrderDto.getWeixinImageUrl())
                    .addHeader("Cookie", "thor=" + pcThorDto.getThor())
                    .get();
            pcAppStoreService.setHeader(headerMap, header);
            Response response = client.newCall(header.build()).execute();
            String bizpayurl = PreUtils.parsePayUrl(response.body().byteStream());
            log.info("当前解析的 bizpayurl地址为msg:{}", bizpayurl);
            pcOrderDtoT.setBizpayurl(bizpayurl);
            response.close();
            return pcOrderDtoT;
        } catch (Exception e) {
            log.error("解析二维码报错没试过：{}", e.getMessage());
        }
        return null;

    }

    public R pcScan(BuildPcThor pcThor) {
        log.info("验证签证是否通过");
        String md5 = SecureUtil.md5(pcThor.getAppck());
        if (!md5.equals(pcThor.getSign())) {
            return R.error(600, "签证错误");
        }
        String pt_pin = PreUtils.get_pt_pin(pcThor.getAppck());
        String loginData = redisTemplate.opsForValue().get("PC:" + pt_pin);
        if (ObjectUtil.isNotNull(pt_pin) && StrUtil.isNotBlank(loginData)) {
            log.info("检验是否是登录有效信息");
            BuildPcThor buildPcThor = JSON.parseObject(loginData, BuildPcThor.class);
            String body = HttpRequest.get("https://api.m.jd.com/api?appid=pc_home_page&functionId=getBaseUserInfo").header("referer", " https://home.jd.com/")
                    .header("cookie", "thor=" + buildPcThor.getPcThorDto().getThor()).execute().body();
            //登录状态
            log.info("登录状态查询msg:{}", body);
            Boolean success = JSON.parseObject(body).getBoolean("success");
            if (success) {
                return R.ok(buildPcThor);
            }
        }
        log.info("构建clent代理");
        OkHttpClient client = buildClent(pcThor.getProxyIp(), pcThor.getProxyPort());
        for (int i = 0; i < 5; i++) {
            R r = xunhuanPc(pcThor, client);
            if (ObjectUtil.isNotNull(r)) {
                return r;
            }
        }

        return R.error(610, "未知错误");
    }

    private R xunhuanPc(BuildPcThor pcThor, OkHttpClient client) {
        try {
            ScanUtils scanUtils = new ScanUtils();
            PcQRCodeDto pcQRCodeDto = null;
            for (int i = 0; i < 4; i++) {
                try {
                    pcQRCodeDto = scanUtils.getQRAndMs(client);
                    if (ObjectUtil.isNotNull(pcQRCodeDto)) {
                        break;
                    }
                } catch (Exception e) {
                    log.info("获取二维码失败msg:{}", e.getMessage());
                }
            }
            if (ObjectUtil.isNull(pcQRCodeDto)) {
                return R.error(601, "代理有问题");
            }
            log.info("执行扫码程序");
            String Uuid = PreUtils.getRandomString(13);
            String Devices_ = PreUtils.getRandomString(32).toUpperCase();
            String QRCodeKey = pcQRCodeDto.getQRCodeKey();
            String key = PreUtils.getRandomString(16);
            String[] split = pcThor.getAppck().split(";");
            String pin = "";
            String wskey = "";
            for (String s : split) {
                if (s.contains("wskey=")) {
                    wskey = s.split("=")[1].trim();
                }
                if (s.contains("pin=") && !s.contains("pt_pin")) {
                    pin = s.split("=")[1].trim();
                }
            }
            if (StrUtil.isBlank(pin) || StrUtil.isBlank(wskey)) {
                return R.error(602, "传入的不是app的ck");
            }
            try {
                List<Integer> confirmQRCodeScanned = Get_confirmQRCodeScanned.get_confirmQRCodeScanned(Uuid, QRCodeKey, Devices_, false, wskey, pin);
                String reqScanData = Get_oi_symmetry_encrypt.get_oi_symmetry_encrypt(confirmQRCodeScanned.stream().mapToInt(i -> i).toArray(), key);
                String postScanData = scanUtils.postScan(client, reqScanData);
                List<Integer> get_confirmQRCodeLogined = Get_confirmQRCodeScanned.get_confirmQRCodeScanned(Uuid, QRCodeKey, Devices_, true, wskey, pin);
                String confirmQRCodeLoginedData = Get_oi_symmetry_encrypt.get_oi_symmetry_encrypt(get_confirmQRCodeLogined.stream().mapToInt(i -> i).toArray(), key);
                String resConfirmData = scanUtils.postComfirmQR(client, confirmQRCodeLoginedData);
                String ticket = scanUtils.getTicket(client, pcQRCodeDto);
                PcThorDto pcThorDto = scanUtils.getPcCkByTicket(client, pcQRCodeDto, ticket);
                if (ObjectUtil.isNotNull(pcThorDto) && StrUtil.isNotBlank(pcThorDto.getThor())) {
                    pcThor.setPcThorDto(pcThorDto);
                    redisTemplate.opsForValue().set("PC:" + pin, JSON.toJSONString(pcThor));
                    return R.ok(pcThor);
                }
            } catch (Exception e) {
                log.error("当前程序报错msg:{}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("意外错误msg:{}", e.getMessage());
        }
        return null;
    }

    private OkHttpClient buildClent(String ip, Integer port) {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        if (StrUtil.isNotBlank(ip) && ObjectUtil.isNotNull(port)) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
            builder.proxy(proxy);
        }
        OkHttpClient client = builder.connectTimeout(3, TimeUnit.SECONDS).readTimeout(3, TimeUnit.SECONDS).followRedirects(false).build();
        return client;
    }


}
