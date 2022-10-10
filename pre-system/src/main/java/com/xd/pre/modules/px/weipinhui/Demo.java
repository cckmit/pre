//package com.xd.pre.modules.px.weipinhui;
//
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.core.date.DateUtil;
//import cn.hutool.core.util.ObjectUtil;
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.db.nosql.redis.RedisDS;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.xd.pre.common.constant.PreConstant;
//import com.xd.pre.modules.px.weipinhui.aes.*;
//import com.xd.pre.modules.px.weipinhui.baiduyun.WordsResult;
//import com.xd.pre.modules.px.weipinhui.token.VipTank;
//import com.xd.pre.modules.px.weipinhui.yezi.YeZiGetMobileDto;
//import com.xd.pre.modules.px.yezijiema.YeZiUtils;
//import lombok.extern.slf4j.Slf4j;
//import okhttp3.*;
//import redis.clients.jedis.Jedis;
//
//import java.math.BigDecimal;
//import java.net.InetSocketAddress;
//import java.net.Proxy;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Slf4j
//public class Demo {
//    static String ip = "27.44.39.142";
//    static Integer port = 4231;
//
//    /**
//     * Scanner input = new Scanner(System.in);
//     * System.out.print("请输入：");
//     * String val = input.next();       // 等待输入值
//     * System.out.println("您输入的是：" + val);
//     * input.close(); // 关闭资源
//     */
//
//    public static void main(String[] args) {
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
//        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
////        builder.proxy(proxy);
//        OkHttpClient client = builder.followRedirects(false).build();
////        String phone = "17821011781";
//        String phone = YeZiUtils.get_mobileByDto(YeZiGetMobileDto.getWphRandomShiKa());
//        log.info("当前手机号msg；{}", phone);
//        CaptchaXY postMsgLogin = postMsg(client, phone);
//        if (ObjectUtil.isNull(postMsgLogin)) {
//            log.error("登录失败");
//            YeZiUtils.free_mobile(phone);
//            log.info("释放手机号");
//            return;
//        }
//        log.info(JSON.toJSONString(postMsgLogin));
//        log.info("开始执行验证码的数据之前的数据msg:{}", JSON.toJSONString(postMsgLogin));
//        String code = new YeZiUtils().get_message(phone);
//        postMsgLogin.setCaptchaCode(code);
////        String dataCaptchaCode = PointsDto.dataCaptchaCode(postMsgLogin);
//        PointsDto.dataCaptchaCode(postMsgLogin);
//        CaptchaXY ticketMsg = getTicket(client, postMsgLogin, PreConstant.TWO);
//        log.info("验证码获取ticketMsgMsg:{}", JSON.toJSONString(ticketMsg));
//        CaptchaXY captchaXY = ticketLogin(client, ticketMsg);
////        ticketMsg.getCaptchaRes().setSid("02300bfbea4244e9b3ae96aa0e01cb7");
//        if (ObjectUtil.isNull(captchaXY) || ObjectUtil.isNull(captchaXY.getVipTank())) {
//            log.error("登录失败请查看日志");
//        }
//        Jedis jedis = RedisDS.create().getJedis();
//        jedis.set("唯品会账号信息:" + captchaXY.getPhone(), JSON.toJSONString(captchaXY));
//        jedis.expire("唯品会账号信息:" + captchaXY.getPhone(), captchaXY.getVipTank().getTANK_EXPIRE() - 3600);
//        System.out.println(captchaXY);
//    }
//
//    public static CaptchaXY postMsg(OkHttpClient client, String phone) {
//        log.info("开始获取验证码的captchaId");
//        CaptchaRes captchaRes = getCaptchaId(client, phone);
//        log.info("获取验证码结束");
//        CaptchaXY captchaXY = parseQPAP(client, captchaRes.getCaptchaId());
//        captchaXY.setPhone(phone);
//        captchaXY.setCaptchaRes(captchaRes);
//        log.info("执行算出位置坐标msg:{}", captchaXY);
//        log.info("开始执行ticket");
////        String dataPoints = PointsDto.dataPoints(captchaXY);
//        CaptchaXY ticket = getTicket(client, captchaXY, PreConstant.ONE);
//        log.info("执行结束msg:{}", ticket);
//        log.info("开始执行getCheckmobileV1");
//        CaptchaXY checkmobileV1 = getCheckmobileV1(client, captchaXY);
//        log.info("执行结束getCheckmobileV1:{}", checkmobileV1);
//        log.info("执行发送短信");
//        CaptchaXY postMsgLogin = postMsgLogin(client, captchaXY);
//        if (ObjectUtil.isNull(postMsgLogin)) {
//            return null;
//        }
//        log.info("是否发送成功msg:{}", postMsgLogin.getPostMsg());
//        return postMsgLogin;
//    }
//
//    public static String buildApiSign(CaptchaXY captchaXY) {
//        JSONObject paramJson = new JSONObject();
//        paramJson.put("app_version", "4.0");
//        paramJson.put("mars_cid", captchaXY.getCaptchaRes().getMinaEdataDto().getMars_cid());
//        paramJson.put("pid", captchaXY.getCheckmobileV1Dto().getPid());
//        paramJson.put("ticket", captchaXY.getTicket());
//        log.info("验证参数封装完成");
//        String api = ApiSign.replaceHost("https://mlogin-api.vip.com/ajaxapi/user/ticketLogin");
//        String hashParam = ApiSign.hashParam(paramJson, "https://mlogin-api.vip.com/ajaxapi/user/ticketLogin");
//        String cid = captchaXY.getCaptchaRes().getMinaEdataDto().getMars_cid();
//        String sid = captchaXY.getCaptchaRes().getSid();
//        String secret = ApiSign.getSecret();
//        StringBuilder sb = new StringBuilder();
//        // rs = this.sha1(api + hashParam + cid + sid + secret);
//        String rs = sb.append(api).append(hashParam).append(cid).append(sid).append(secret).toString();
//        String sha1 = ApiSign.getSha1(rs);
//        return sha1;
//    }
//
//    public static CaptchaXY ticketLogin(OkHttpClient client, CaptchaXY captchaXY) {
//        try {
//            String api_sign = buildApiSign(captchaXY);
//            String url = "https://mlogin-api.vip.com/ajaxapi/user/ticketLogin";
//            RequestBody requestBody = new FormBody.Builder()
//                    .add("api_key", "8cec5243ade04ed3a02c5972bcda0d3f")
//                    .add("app_version", "4.0")
//                    .add("mars_cid", captchaXY.getCaptchaRes().getMinaEdataDto().getMars_cid())
//                    .add("pid", captchaXY.getCheckmobileV1Dto().getPid())
//                    .add("ticket", captchaXY.getTicket())
//                    .build();
//            Request request = new Request.Builder().url(url)
//                    .header("authorization", "OAuth api_sign=" + api_sign)
//                    .header("cookie", String.format("mars_cid=%s;mars_sid=%s", captchaXY.getCaptchaRes().getMinaEdataDto().getMars_cid(),
//                            captchaXY.getCaptchaRes().getSid()))
//                    .post(requestBody)
//                    .build();
//            Response response = client.newCall(request).execute();
//            String resStr = response.body().string();
//            log.info("执行ticketLogin返回的数据msg:{}", resStr);
//            if (StrUtil.isBlank(resStr) || JSON.parseObject(resStr).getInteger("code") != 1) {
//                log.error("返回VIP_TANK报错了");
//            }
//            VipTank vipTank = JSON.parseObject(JSON.parseObject(resStr).getString("data"), VipTank.class);
//            vipTank.setCreateDate(DateUtil.formatDateTime(new Date()));
//            captchaXY.setVipTank(vipTank);
//            return captchaXY;
//        } catch (Exception e) {
//            log.error("执行ticketLogin报错msg:{}", e.getMessage());
//        }
//        return null;
//    }
//
//
//    public static CaptchaXY postMsgLogin(OkHttpClient client, CaptchaXY captchaXY) {
//        try {
//            String url = "https://captcha.vip.com/getURL";
//            RequestBody requestBody = new FormBody.Builder()
//                    .add("v", "1")
//                    .add("source", "1")
//                    .add("captchaType", "2")
//                    .add("data", "{}")
//                    .add("captchaId", captchaXY.getCheckmobileV1Dto().getCaptchaId())
//                    .build();
//            Request request = new Request.Builder().url(url)
//                    .post(requestBody)
//                    .build();
//            Response response = client.newCall(request).execute();
//            String resStr = response.body().string();
//            log.info("执行postMsgLogin返回的数据msg:{}", resStr);
//            response.close();
//            if (StrUtil.isBlank(resStr) || JSON.parseObject(resStr).getInteger("code") != 0) {
//                log.error("postMsgLogin当前请求验证码失败");
//                return null;
//            }
//            captchaXY.setPostMsg(Boolean.TRUE);
//            return captchaXY;
//        } catch (Exception e) {
//            log.error("执行postMsgLogin报错msg:{}", e.getMessage());
//        }
//        return null;
//    }
//
//    public static CaptchaXY getCheckmobileV1(OkHttpClient client, CaptchaXY captchaXY) {
//        try {
//            MinaEdataDto minaEdataDto = ParamAes.convenient_login_wap_after_captcha(captchaXY);
//            String url = "https://mapi.vip.com/vips-mobile/rest/auth/quicklogin/wap/checkmobile/v1";
//            RequestBody requestBody = new FormBody.Builder()
//                    .add("api_key", "8cec5243ade04ed3a02c5972bcda0d3f")
//                    .add("app_version", "4.0")
//                    .add("mars_cid", captchaXY.getCaptchaRes().getMinaEdataDto().getMars_cid())
//                    .add("skey", "9cf2380318f54f31acfb1d6e274f5555")
//                    .add("mina_eversion", "0")
//                    .add("mina_edata", minaEdataDto.getMina_edata())
//                    .build();
//            Request request = new Request.Builder().url(url)
//                    .post(requestBody)

//                    .build();
//            Response response = client.newCall(request).execute();
//            String resStr = response.body().string();
//            log.info("执行getCheckmobileV1返回的数据msg:{}", resStr);
//            if (StrUtil.isNotBlank(resStr) && resStr.contains("80001")) {
//                log.error("账号账号存在风险msg:{}", captchaXY.getPhone());
//                return null;
//            }
//            response.close();
//            if (StrUtil.isBlank(resStr) || JSON.parseObject(resStr).getInteger("code") != 1) {
//                log.error("getTicket当前请求验证码失败");
//            }
//            CheckmobileV1Dto checkmobileV1Dto = JSON.parseObject(JSON.parseObject(resStr).getString("data"), CheckmobileV1Dto.class);
//            String authType = checkmobileV1Dto.getAuthType();
//            if (Integer.valueOf(authType) == PreConstant.ONE) {
//                log.error("当前是语音通知，释放");
//                Boolean free_mobile = YeZiUtils.free_mobile(captchaXY.getPhone());
//                log.info("释放是否成功msg:{}", free_mobile);
//                return null;
//            }
//            captchaXY.setCheckmobileV1Dto(checkmobileV1Dto);
//            return captchaXY;
//        } catch (Exception e) {
//            log.error("执行getCheckmobileV1报错msg:{}", e.getMessage());
//        }
//        return null;
//    }
//
//    private static CaptchaXY getTicket(OkHttpClient client, CaptchaXY captchaXY, Integer ticktetNum) {
//        try {
//            String dataPoints = null;
//            if (ticktetNum == 1) {
//                dataPoints = PointsDto.dataPoints(captchaXY);
//            }
//            if (ticktetNum == 2) {
//                dataPoints = PointsDto.dataCaptchaCode(captchaXY);
//            }
//            String url = "https://captcha.vip.com/check";
//            RequestBody requestBody = new FormBody.Builder()
//                    .add("v", "1")
//                    .add("source", "0")
//                    .add("captchaId", ticktetNum == 1 ? captchaXY.getCaptchaRes().getCaptchaId() : captchaXY.getCheckmobileV1Dto().getCaptchaId())
//                    .add("captchaType", ticktetNum == 1 ? "7" : "2")
//                    .add("data", dataPoints)
//                    .add("templateId", captchaXY.getCaptchaRes().getTemplateId())
//                    .build();
//            Request request = new Request.Builder().url(url)
//                    .post(requestBody)
//                    .build();
//            Response response = client.newCall(request).execute();
//            String resStr = response.body().string();
//            log.info("执行getTicket返回的数据msg:{}", resStr);
//            response.close();
//            if (StrUtil.isBlank(resStr) || JSON.parseObject(resStr).getInteger("code") != 0) {
//                log.error("getTicket当前请求验证码失败");
//            }
//            String ticket = JSON.parseObject(JSON.parseObject(resStr).getString("data")).getString("ticket");
//            captchaXY.setTicket(ticket);
//
//            return captchaXY;
//        } catch (Exception e) {
//            log.error("执行getCaptchaId报错msg:{}", e.getMessage());
//        }
//        return null;
//    }
//
//    private static CaptchaRes getCaptchaId(OkHttpClient client, String phone) {
//        try {
//            MinaEdataDto minaEdataDto = ParamAes.convenient_login_wap_img_captcha(phone);
//            String url = "https://mapi.vip.com/vips-mobile/rest/auth/captcha/mp/flow/v1";
//            RequestBody requestBody = new FormBody.Builder()
//                    .add("api_key", "8cec5243ade04ed3a02c5972bcda0d3f")
//                    .add("app_version", "4.0")
//                    .add("mars_cid", minaEdataDto.getMars_cid())
//                    .add("mina_eversion", "0")
//                    .add("skey", "9cf2380318f54f31acfb1d6e274f5555")
//                    .add("mina_edata", minaEdataDto.getMina_edata())
//                    .build();
//            Request request = new Request.Builder().url(url)
//                    .post(requestBody)
//                    .build();
//            Response response = client.newCall(request).execute();
//            String resStr = response.body().string();
//            log.info("执行getCaptchaId返回的数据为msg:{}", resStr);
//            response.close();
//            if (StrUtil.isBlank(resStr) || JSON.parseObject(resStr).getInteger("code") != 1) {
//                log.error("当前请求验证码失败");
//            }
//            CaptchaRes captchaRes = JSON.parseObject(JSON.parseObject(resStr).getString("data"), CaptchaRes.class);
//            captchaRes.setMinaEdataDto(minaEdataDto);
//            return captchaRes;
//        } catch (Exception e) {
//            log.error("执行getCaptchaId报错msg:{}", e.getMessage());
//        }
//        return null;
//
//    }
//
//
//    private static CaptchaXY parseQPAP(OkHttpClient client, String captchaId) {
//        try {
//            mark:
//            for (int i = 0; i < 20; i++) {
//                log.info("获取验证码开始");
//                CaptchaData yanzhengma = getYanzhengma(client, captchaId);
//                List<WordsResult> wordsResulAp = AuthService.parseCapData("https://captcha.vip.com/getImage?v=1&captchaType=7&imageId=" + yanzhengma.getAp());
//                if (wordsResulAp.size() < 5) {
//                    log.error("解析出来不是5个字符串，重新解析");
//                    continue;
//                }
//                Map<String, WordsResult> wordsResultMap = wordsResulAp.stream().collect(Collectors.toMap(it -> it.getWords(), it -> it));
//                List<WordsResult> sortWordReSult = wordsResulAp.stream().sorted(Comparator.comparing(it -> it.getLocation().getLeft())).collect(Collectors.toList());
//                Map<String, Integer> sortWordReSultIndexMap = sortWordReSult.stream().collect(Collectors.toMap(it -> it.getWords(), it -> sortWordReSult.indexOf(it)));
//
//                List<WordsResult> wordsResultsQp = AuthService.parseCapData("https://captcha.vip.com/getImage?v=1&captchaType=7&imageId=" + yanzhengma.getQp());
//                if (wordsResultsQp.size() != 1) {
//                    log.info("解析出来点击不是唯一的数据msg:{}");
//                    continue;
//                }
//                //请依次点击“私、赠、镇
//                log.info("解析成功");
//                int dianjiIndex = wordsResultsQp.get(PreConstant.ZERO).getWords().indexOf("请依次点击“");
//                String zic = wordsResultsQp.get(PreConstant.ZERO).getWords().substring(dianjiIndex + 6);
//                List<String> dianjiArrays = Arrays.asList(zic.split("、"));
//                CaptchaXY.CaptchaXYBuilder builder = CaptchaXY.builder();
//                for (String dianjiArray : dianjiArrays) {
//                    int x = dianjiArrays.indexOf(dianjiArray);
//                    WordsResult wordsResult = wordsResultMap.get(dianjiArray);
//                    if (ObjectUtil.isNull(wordsResult)) {
//                        continue mark;
//                    }
//                    Integer index = sortWordReSultIndexMap.get(dianjiArray);
//                    if (x == 0) {
//                        builder.x1(Double.valueOf((wordsResult.getLocation().getLeft() + (wordsResult.getLocation().getWidth() / 2))).intValue()).y1(36);
//                    }
//                    if (x == 1) {
//                        builder.x2(Double.valueOf((wordsResult.getLocation().getLeft() + (wordsResult.getLocation().getWidth() / 2))).intValue()).y2(36);
//                    }
//                    if (x == 2) {
//                        builder.x3(Double.valueOf((wordsResult.getLocation().getLeft() + (wordsResult.getLocation().getWidth() / 2))).intValue()).y3(36);
//                    }
//                }
//                CaptchaXY captchaXY = builder.build();
//                return captchaXY;
//            }
//        } catch (Exception e) {
//            log.error("解析报错:{}", e.getMessage());
//        }
//        return null;
//    }
//
//    private static Integer getLeftIndex(Integer index) {
//        if (index == 0) {
//            return 47;
//        }
//        if (index == 1) {
//            return 85;
//        }
//        if (index == 2) {
//            return 146;
//        }
//        if (index == 3) {
//            return 188;
//        }
//        if (index == 4) {
//            return 240;
//        }
//        return null;
//    }
//
//    private static CaptchaData getYanzhengma(OkHttpClient client, String captchaId) {
//        try {
////            Jedis jedis = RedisDS.create().getJedis();
////            Set<String> keys = jedis.keys("设置动态登录验证码:*");
////            int i = PreUtils.randomCommon(0, keys.size() - 1, 1)[0];
////            String yanzhengmakey = keys.stream().collect(Collectors.toList()).get(i);
////            String captchaId = jedis.get(yanzhengmakey);
//
//            String url = "https://captcha.vip.com/getURL";
//            RequestBody requestBody = new FormBody.Builder()
//                    .add("v", "1")
//                    .add("source", "0")
//                    .add("captchaId", captchaId)
//                    .add("captchaType", "7")
//                    .add("data", "{}")
//                    .build();
//            Request request = new Request.Builder().url(url)
//                    .post(requestBody)
//                    .build();
//            Response response = client.newCall(request).execute();
//            String resStr = response.body().string();
//            response.close();
//            log.info("执行getYanzhengma>>>>msg:{}", resStr);
//            if (!resStr.contains("qp") || !resStr.contains("ap")) {
//                return null;
//            }
//            JSONObject parseObject = JSON.parseObject(JSONObject.parseObject(resStr).getString("data"));
//            CaptchaData captchaData = new CaptchaData(parseObject.getString("qp"), parseObject.getString("ap"), captchaId);
//            return captchaData;
//        } catch (Exception e) {
//            log.error("执行getYanzhengma报错:{}", e.getMessage());
//        }
//        return null;
//
//    }
//
//    private static void match(OkHttpClient client) {
//        String ck = "DDBED3EB70ED6482F37BF954F3E14E67869B8D46";
//        String payUrl = getPayUrl("47516511", client, ck);
//        log.info("执行收银台");
//        String npayvid = cashier(payUrl, client, ck);
//        log.info("执行收银台设置的ck为数据msg:{}", npayvid);
//        log.info("执行订单预编译");
//        BigDecimal preview = preview(client, ck, npayvid);
//        log.info("执行预编译成功");
//        log.info("执行dopay");
//        String redirectUrl = doPay(client, ck, npayvid);
//        String tenpay = getTenpay(client, ck, npayvid, redirectUrl);
//    }
//
//    private static String getTenpay(OkHttpClient client, String ck, String npayvid, String redirectUrl) {
//        try {
//            String url = "https://npay.vip.com" + redirectUrl;
//            Request request = new Request.Builder().url(url)
//                    .addHeader("Cookie", String.format("VIP_TANK=%s;", ck) + String.format(";%s;", npayvid))
//                    .get()
//                    .build();
//            Response response = client.newCall(request).execute();
//            String resStr = response.body().string();
//            response.close();
//            log.info("执行getTenpay>>>>msg:{}", resStr);
//            if (!resStr.contains("https://wx.tenpay.com")) {
//                return null;
//            }
//            int i1 = resStr.indexOf("\"https://");
//            int i2 = resStr.indexOf("}}");
//            String tenpayUrl = resStr.substring(i1 + 1, i2 - 1);
//            log.info("获取支付腾讯链接为msg:{}", tenpayUrl);
//            return tenpayUrl;
//        } catch (Exception e) {
//            log.error("执行getTenpay报错msg:{}", e.getMessage());
//        }
//        return null;
//
//    }
//
//    private static String doPay(OkHttpClient client, String ck, String npayvid) {
//        try {
//            String url = "https://npay.vip.com/wap/cashier/api/pay";
//            RequestBody requestBody = new FormBody.Builder()
//                    .add("payId", "1118")
//                    .add("payType", "181")
//                    .build();
//            Request request = new Request.Builder().url(url)
//                    .addHeader("Cookie", String.format("VIP_TANK=%s;", ck) + String.format(";%s;", npayvid))
//                    .post(requestBody)
//                    .build();
//            Response response = client.newCall(request).execute();
//            String resStr = response.body().string();
//            log.info("执行dopay>>>>>>msg:{}", resStr);
//            response.close();
//            if (!resStr.contains("redirectUrl")) {
//                return null;
//            }
//            String redirectUrl = JSONObject.parseObject(JSONObject.parseObject(resStr).getString("data")).getString("redirectUrl");
//            return redirectUrl;
//        } catch (Exception e) {
//            log.error("执行dopay报错");
//        }
//        return null;
//    }
//
//    private static BigDecimal preview(OkHttpClient client, String ck, String npayvid) {
//        try {
//            String url = "https://npay.vip.com/wap/cashier/api/preview?payId=1118&payType=181";
//            Request request = new Request.Builder().url(url)
//                    .get()
//                    .addHeader("User-Agent", " Mozilla/5.0 (Linux; Android 8.0.0; SM-G955U Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Mobile Safari/537.36")
//                    .addHeader("Cookie", String.format("VIP_TANK=%s;", ck) + String.format(";%s;", npayvid))
//                    .build();
//            Response response = client.newCall(request).execute();
//            String resStr = response.body().string();
//            response.close();
//            log.info("执行收银数据初始化msg:{}", resStr);
//            if (!resStr.contains("totalPayAmount")) {
//                return null;
//            }
//            String totalPayAmount = JSONObject.parseObject(JSONObject.parseObject(JSONObject.parseObject(resStr).getString("data"))
//                    .getString("preview")).getString("totalPayAmount");
//            BigDecimal totalPayAmountBig = new BigDecimal(totalPayAmount);
//            //TODO 和数据对比商品价格
//            return totalPayAmountBig;
//        } catch (Exception e) {
//            log.info("预编译报错msg:{}", e);
//        }
//        return null;
//    }
//
//    public static String cashier(String payUrl, OkHttpClient client, String ck) {
//        try {
//            Request request = new Request.Builder().url(payUrl)
//                    .get()
//                    .addHeader("Cookie", String.format("VIP_TANK=%s;", ck))
//                    .build();
//            Response response = client.newCall(request).execute();
//            String resStr = response.body().string();
//            response.close();
//            List<String> headers = response.headers("set-cookie");
//            log.info("收银台数据为msg:{}", resStr);
//            if (!resStr.contains("收银台")) {
//                return null;
//            }
//            if (CollUtil.isNotEmpty(headers)) {
//                for (String header : headers) {
//                    //NPAYVID=F37811D37B32DF6E96484B65C1208AF5B7DD09C7;path=/;domain=npay.vip.com;httponly
//                    if (header.contains("NPAYVID")) {
//                        int i = header.indexOf(";");
//                        String NPAYVID = header.substring(0, i);
//                        return NPAYVID;
//                    }
//                }
//            }
//            return null;
//        } catch (Exception e) {
//            log.error("收银台报错了msg:{}", e.getMessage());
//        }
//        return null;
//    }
//
//    public static String getPayUrl(String orderId, OkHttpClient client, String ck) {
//        try {
//            String url = "https://h5.vip.com/api/virtual/VirtualPayJump/getPayUrl";
//            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
//            String body = String.format("{\"orderId\":\"%s\",\"channelId\":\"default-wap\",\"isApp\":0,\"extraParams\":{\"_show_header\":1,\"order_id\":\"%s\",\"type\":\"vipcard\"}}",
//                    orderId, orderId);
//            RequestBody requestBody = RequestBody.create(JSON, body);
//            Request request = new Request.Builder().url(url)
//                    .post(requestBody)
//                    .addHeader("Cookie", String.format("VIP_TANK=%s;", ck))
//                    .build();
//            Response response = client.newCall(request).execute();
//            String resStr = response.body().string();
//            log.info("唯品会请求跳转数据为msg:{}", resStr);
//            response.close();
//            JSONObject resJson = com.alibaba.fastjson.JSON.parseObject(resStr);
//            Integer code = resJson.getInteger("code");
//            if (code != PreConstant.ZERO) {
//                log.info("当前订单报错了，请查看日志");
//                return null;
//            }
//            String data = resJson.getString("data");
//            String payUrl = com.alibaba.fastjson.JSON.parseObject(data).getString("payUrl");
//            if (StrUtil.isNotBlank(payUrl) && payUrl.contains("appKey")) {
//                log.info("当前跳转url为msg:{}", payUrl);
//                return payUrl;
//            }
//        } catch (Exception e) {
//            log.error("当前跳转url报错");
//        }
//        return null;
//    }
//}
