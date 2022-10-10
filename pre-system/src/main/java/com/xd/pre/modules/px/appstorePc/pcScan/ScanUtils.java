package com.xd.pre.modules.px.appstorePc.pcScan;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xd.pre.common.constant.PreConstant;
import com.xd.pre.common.sign.JdSgin;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.common.utils.px.dto.SignVoAndDto;
import com.xd.pre.modules.px.psscan.PcQRCodeDto;
import com.xd.pre.modules.px.weipinhui.service.WphService;
import com.xd.pre.modules.sys.domain.JdCk;
import com.xd.pre.modules.sys.mapper.JdCkMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ScanUtils {


    public PcThorDto scanPcByAppCk(JdCk jdCk, OkHttpClient client, JdCkMapper jdCkMapper, StringRedisTemplate redisTemplate, WphService wphService, Integer index) {
        jdCk = jdCkMapper.selectById(jdCk.getId());
        log.info("判断是否能下单msg:{}");
        if (jdCk.getIsAppstoreOrder() == 0) {
            try {
                String bodyData = "{\"appKey\":\"android\",\"brandId\":\"999440\",\"buyNum\":1,\"payMode\":\"0\",\"rechargeversion\":\"10.9\",\"skuId\":\"10022039398507\",\"totalPrice\":\"10000\",\"type\":1,\"version\":\"1.10\"}";
                RequestBody requestBody = new FormBody.Builder()
                        .add("body", bodyData)
                        .build();
                SignVoAndDto signVoAndDto = new SignVoAndDto("submitGPOrder", bodyData);
                signVoAndDto = JdSgin.newSign(signVoAndDto);

                String url = String.format("https://api.m.jd.com/client.action?functionId=submitGPOrder&clientVersion=9.4.4&client=android&uuid=%s&st=%s&sign=%s&sv=120", signVoAndDto.getUuid(),
                        signVoAndDto.getSt(), signVoAndDto.getSign());
                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .addHeader("Cookie", jdCk.getCk())
                        .build();
                Response response = client.newCall(request).execute();
                String resStr = response.body().string();
                if (resStr.contains("销售火爆，请稍后再试")) {
                    jdCk.setIsAppstoreOrder(PreConstant.FUYI_1);
                    jdCkMapper.updateById(jdCk);
                    return null;
                } else {
                    jdCk.setIsAppstoreOrder(PreConstant.ONE);
                    jdCkMapper.updateById(jdCk);
                }
            } catch (Exception e) {
                log.info("下单失败msg:{}", e.getMessage());
            }
        }
        // boolean b = ObjectUtil.isNotNull(jdCk.getPcExpireDate().getTime()) && jdCk.getPcExpireDate().getTime() > System.currentTimeMillis();
        if (jdCk.getIsEnable() != 1 || jdCk.getIsAppstoreOrder() == -1 || jdCk.getIsPc() == -1) {
            return null;
        }
        if (ObjectUtil.isNotNull(jdCk.getPcExpireDate()) && jdCk.getPcExpireDate().getTime() > System.currentTimeMillis()) {
            return null;
        }

        log.info("开始之心次数msg:{}", index);
        if (index >= 5) {
            return null;
        }
        index = index + 1;
        Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent("执行扫码操作:" + jdCk.getId(), "", 1, TimeUnit.MINUTES);
        if (!ifAbsent) {
            log.info("刚刚已经执行了扫码操作了");
            return null;
        }
        if (StrUtil.isNotBlank(jdCk.getThor())) {
            JdCk refresh = refresh(client, jdCk);
            if (ObjectUtil.isNotNull(refresh)) {
                jdCkMapper.updateById(refresh);
                return JSON.parseObject(refresh.getThor(), PcThorDto.class);
            }
        }

        try {
            PcQRCodeDto pcQRCodeDto = getQRAndMs(client);
            if (ObjectUtil.isNull(pcQRCodeDto)) {
                return scanPcByAppCk(jdCk, client, jdCkMapper, redisTemplate, wphService, index);
            }
            String Uuid = PreUtils.getRandomString(13);
            String Devices_ = PreUtils.getRandomString(32).toUpperCase();
            String key = PreUtils.getRandomString(16);
            String QRCodeKey = pcQRCodeDto.getQRCodeKey();
            String[] split = jdCk.getCk().split(";");
            String pin = "";
            String wskey = "";
            for (String s : split) {
                if (s.contains("pin=") && !s.contains("pt_pin")) {
                    pin = s.split("=")[1].trim();
                }
                if (s.contains("wskey=")) {
                    wskey = s.split("=")[1].trim();
                }
            }
            if (StrUtil.isBlank(pin) || StrUtil.isBlank(wskey)) {
                jdCk.setIsPc(0);
                log.info("当前ck不是appck不能执行扫码操作");
                jdCkMapper.updateById(jdCk);
                return null;
            }
            log.debug("当前账号看是否登录，如果没有登录就执行修改isenbale");
            String loginInfo = getLoginInfo(client, jdCk.getCk(), jdCk.getId());
            if (ObjectUtil.isNull(loginInfo)) {
                jdCk.setIsEnable(-1);
                jdCkMapper.updateById(jdCk);
                log.info("当前账号没有登录，置为失败");
                return null;
            }
            if (loginInfo.length() == 1) {
                log.info("当前账号出现执行超时操作请查看日志");
                redisTemplate.delete("执行扫码操作:" + jdCk.getId());
                return scanPcByAppCk(jdCk, client, jdCkMapper, redisTemplate, wphService, index);
            }
            List<Integer> confirmQRCodeScanned = Get_confirmQRCodeScanned.get_confirmQRCodeScanned(Uuid, QRCodeKey, Devices_, false, wskey, pin);
            String reqScanData = Get_oi_symmetry_encrypt.get_oi_symmetry_encrypt(confirmQRCodeScanned.stream().mapToInt(i -> i).toArray(), key);
            log.info("执行扫码操作msg:{}", reqScanData);
            String postScanData = postScan(client, reqScanData);
            if (StrUtil.isBlank(postScanData)) {
                log.error("scanPcByAppCk接口报错");
                redisTemplate.delete("执行扫码操作:" + jdCk.getId());
                return scanPcByAppCk(jdCk, client, jdCkMapper, redisTemplate, wphService, index);
            }
            // int[] ints = Fuzhu.易语言Base64(postScanData);
            // String oi_symmetry_encrypt = Get_oi_symmetry_encrypt.get_oi_symmetry_encrypt(ints, key);
            // LoginPcEnum tlv_decode = Get_TLV_Decode.get_TLV_Decode(Fuzhu.byte2Int(oi_symmetry_encrypt.getBytes()));
            // log.info("msg:{}", tlv_decode.getValue());
            List<Integer> get_confirmQRCodeLogined = Get_confirmQRCodeScanned.get_confirmQRCodeScanned(Uuid, QRCodeKey, Devices_, true, wskey, pin);
            String confirmQRCodeLoginedData = Get_oi_symmetry_encrypt.get_oi_symmetry_encrypt(get_confirmQRCodeLogined.stream().mapToInt(i -> i).toArray(), key);
            log.info("执行确认登录数据msg:{}", confirmQRCodeLoginedData);
            String resConfirmData = postComfirmQR(client, confirmQRCodeLoginedData);
            if (StrUtil.isBlank(resConfirmData)) {
                log.error("resConfirmData接口报错");
                redisTemplate.delete("执行扫码操作:" + jdCk.getId());
                return scanPcByAppCk(jdCk, client, jdCkMapper, redisTemplate, wphService, index);
            }
            log.info("confrimRes:{}", resConfirmData);
            log.info("开始执行tikenData");
            String ticket = getTicket(client, pcQRCodeDto);
            if (StrUtil.isBlank(ticket)) {
                Long qiehuanIp = redisTemplate.opsForValue().increment("扫码切换ip", 1);
                if (qiehuanIp >= 15) {
                    client = wphService.buildClient();
                }
                log.error("getTicketmsg:{}", ticket);
                redisTemplate.delete("执行扫码操作:" + jdCk.getId());
                return scanPcByAppCk(jdCk, client, jdCkMapper, redisTemplate, wphService, index);
            }
            redisTemplate.delete("扫码切换ip");
            log.info("执行");
            PcThorDto pcThorDto = getPcCkByTicket(client, pcQRCodeDto, ticket);
            if (ObjectUtil.isNotNull(pcThorDto) && StrUtil.isBlank(pcThorDto.getThor())) {
                log.error("pc扫码失败msg:{}", pin);
                jdCk.setIsPc(PreConstant.FUYI_1);
                jdCkMapper.updateById(jdCk);
                return null;
            }
            if (ObjectUtil.isNull(pcThorDto)) {
                redisTemplate.delete("执行扫码操作:" + jdCk.getId());
                return scanPcByAppCk(jdCk, client, jdCkMapper, redisTemplate, wphService, index);
            } else {
                log.info("当前账号能扫码执行成功msg:{}", jdCk);
                jdCk.setIsPc(PreConstant.ONE);
                jdCk.setPcExpireDate(DateUtil.offsetMinute(new Date(), 480));
                jdCk.setThor(JSON.toJSONString(pcThorDto));
                jdCkMapper.updateById(jdCk);
                return pcThorDto;
            }

        } catch (Exception e) {
            redisTemplate.delete("执行扫码操作:" + jdCk.getId());
            log.error("当前执行扫码操作报错了msg:{}", e.getMessage());
        }
        redisTemplate.delete("执行扫码操作:" + jdCk.getId());
        return scanPcByAppCk(jdCk, client, jdCkMapper, redisTemplate, wphService, index);
    }

    public JdCk refresh(OkHttpClient client, JdCk jdCk) {
        try {
            PcThorDto pcThorDto = JSON.parseObject(jdCk.getThor(), PcThorDto.class);
            String url = "https://passport.jd.com/call/getHelloJson?m=ls";
            Request.Builder builder = new Request.Builder().url(url)
                    .header("Cookie", "thor=" + pcThorDto.getThor())
                    .header("referer", "https://pcashier.jd.com/")
                    .get();
            Response response = client.newCall(builder.build()).execute();
            String resStr = response.body().string();
            if (StrUtil.isNotBlank(resStr) && resStr.contains(jdCk.getPtPin())) {
                log.info("刷新成功msg:{}", resStr);
                List<String> headers = response.headers("Set-Cookie");
                if (CollUtil.isNotEmpty(headers)) {
                    for (String header : headers) {
                        if (header.contains("thor=")) {
                            String thor = header.split("thor=")[1];
                            pcThorDto.setThor(thor);
                            response.close();
                            JdCk jdCk1 = new JdCk();
                            BeanUtil.copyProperties(jdCk, jdCk1);
                            jdCk1.setThor(JSON.toJSONString(pcThorDto));
                            jdCk1.setPcExpireDate(DateUtil.offsetMinute(new Date(), 8 * 60));
                            return jdCk1;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("刷新token失败msg:{}", e.getMessage());
        }
        return null;
    }

    public PcThorDto getPcCkByTicket(OkHttpClient client, PcQRCodeDto pcQRCodeDto, String ticket) throws IOException {
        try {
            String url = String.format("https://passport.jd.com/uc/qrCodeTicketValidation?t=%s", ticket);
            Request.Builder builder = new Request.Builder().url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                    .header("cookie", "__jda=122270672.16594120065241893412991.1659412006.1659412006.1659709731.2;" + String.format("QRCodeKey=%s;wlfstk_smdl=%s;", pcQRCodeDto.getQRCodeKey(), pcQRCodeDto.getWlfstk_smdl()))
                    .header("referer", "https://passport.jd.com/new/login.aspx?ReturnUrl=https%3A%2F%2Fwww.jd.com%2F")
                    .get();
            Response response = client.newCall(builder.build()).execute();
            String resStr = response.body().string();
            log.info("getPcCkByTicket,resStr:{}", resStr);
            Headers headers = response.headers();
            response.close();
            if (StrUtil.isNotBlank(resStr) && resStr.contains("http://passport.jd.com/relay/loginRelay")) {
                return new PcThorDto();
            }
            Map<String, List<String>> listMap = headers.toMultimap();
            if (listMap.containsKey("Set-Cookie")) {
                List<String> setCookies = listMap.get("Set-Cookie");
                Map<String, Object> tMap = new HashMap<>();
                if (CollUtil.isNotEmpty(setCookies) && JSON.toJSONString(setCookies).contains("thor=")) {
                    for (String setCookie : setCookies) {
                        log.info("setCookie:{}", setCookie);
                        String setCoookieData = setCookie.split(";")[0];
                        if (setCoookieData.contains("=")) {
                            tMap.put(setCoookieData.split("=")[0], setCoookieData.split("=")[1]);
                        }
                    }
                    if (CollUtil.isNotEmpty(tMap)) {
                        log.info("登录 信息msg:{}", tMap);
                        PcThorDto pcThorDto = JSON.parseObject(JSON.toJSONString(tMap), PcThorDto.class);
                        return pcThorDto;
                    }
                }
            }
        } catch (Exception e) {
            log.info("当前扫码最后一步报错了msg:{}", e.getMessage());
        }
        return null;

    }

    public PcQRCodeDto getQRAndMs(OkHttpClient client) throws IOException {
        try {
            Request.Builder builder = new Request.Builder().url("https://qr.m.jd.com/show?appid=133&size=147&t=1659457776130").get();
            Response response = client.newCall(builder.build()).execute();
            String resStr = response.body().string();
            List<String> headers = response.headers("Set-cookie");
            if (CollUtil.isEmpty(headers) || headers.size() != 2) {
                log.info("获取登录token报错msg:{}", headers);
                return null;
            }
            String QRCodeKey = null;
            String wlfstk_smdl = null;
            for (String header : headers) {
                if (header.contains("QRCodeKey")) {
                    QRCodeKey = header.split(";")[0].split("=")[1];

                }
                if (header.contains("wlfstk_smdl")) {
                    wlfstk_smdl = header.split(";")[0].split("=")[1];
                }
            }
            log.info("wlfstk_smdl:{},QRCodeKey:{}", wlfstk_smdl, QRCodeKey);
            PcQRCodeDto pcQRCodeDto = new PcQRCodeDto(wlfstk_smdl, QRCodeKey);
            return pcQRCodeDto;
        } catch (Exception e) {
            log.error("getQRAndMs报凑了。多半都是代理问题msg:{}", e.getMessage());
        }
        return null;

    }

    public String postScan(OkHttpClient client, String data) throws IOException {
        try {
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, data);
            Request.Builder builder = new Request.Builder().url("https://wlogin.m.jd.com/applogin_v2")
                    .post(body);
            Response response = client.newCall(builder.build()).execute();
            String resStr = response.body().string();
            log.info("postcan返回值msg:{}", resStr);
            return resStr;
        } catch (Exception e) {
            log.error("postScan 确认扫码报错msg:{}", e.getMessage());
        }
        return null;
    }

    public String postComfirmQR(OkHttpClient client, String data) throws IOException {
        try {
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, data);
            Request.Builder builder = new Request.Builder().url("https://wlogin.m.jd.com/applogin_v2")
                    .post(body);
            Response response = client.newCall(builder.build()).execute();
            String resStr = response.body().string();
            log.info("postComfirmQR:{}", resStr);
            return resStr;
        } catch (Exception e) {
            log.error("确认扫码报错msg:{}", e.getMessage());
        }
        return null;

    }

    public String getTicket(OkHttpClient client, PcQRCodeDto pcQRCodeDto) throws IOException {
        try {
            String url = String.format("https://qr.m.jd.com/check?callback=jQuery8224659&appid=133&token=%s", pcQRCodeDto.getWlfstk_smdl());
            Request.Builder builder = new Request.Builder().url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                    .header("cookie", String.format("QRCodeKey=%s;wlfstk_smdl=%s;", pcQRCodeDto.getQRCodeKey(), pcQRCodeDto.getWlfstk_smdl()) + " __jda=187205033.1659380826991660805628.1659380826.1659973031.1659976896.26;shshshfpb=egkf3n0LEgWO0G0iHmsyw5w; shshshfpa=bbf21a1c-a812-cb79-5396-2a8137e98af7-1659167533;")
                    .header("referer", "https://passport.jd.com/")
                    .get();
            Response response = client.newCall(builder.build()).execute();
            String resStr = response.body().string();
            response.close();
            log.info("getTicket:{}", resStr);
            resStr = resStr.replace("jQuery8224659(", "");
            resStr = resStr.replace(")", "");
            JSONObject parseObject = JSON.parseObject(resStr);
            String ticket = parseObject.getString("ticket");
            return ticket.trim();
        } catch (Exception e) {
            log.error("当前获取ticket报错msg:{},报错信息msg:{}", pcQRCodeDto, e.getMessage());
        }
        return null;
    }

    public String getLoginInfo(OkHttpClient client, String ck, Integer id) {
        try {
            Request.Builder builder = new Request.Builder().url("https://api.m.jd.com/?functionId=queryJDUserInfo&appid=jd-cphdeveloper-m")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                    .header("cookie", ck)
                    .header("referer", " https://wqs.jd.com")
                    .get();
            Response response = client.newCall(builder.build()).execute();
            String resStr = response.body().string();
            response.close();
            log.info("开始检查id:{},resStr:{}", id, resStr);
            if (resStr.contains("isRealNameAuth")) {
                return ck;
            }
        } catch (Exception e) {
            log.error("执行查询报错了msg:{}", id);
            return "1";
        }
        return null;
    }
}
