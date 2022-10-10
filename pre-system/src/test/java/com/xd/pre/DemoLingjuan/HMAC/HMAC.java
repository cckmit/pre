//package com.xd.pre.DemoLingjuan.HMAC;
//
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.http.Header;
//import cn.hutool.http.HttpRequest;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.xd.pre.modules.px.vo.tmpvo.h5st.CactusVo;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.codec.binary.Hex;
//
//import javax.crypto.Mac;
//import javax.crypto.spec.SecretKeySpec;
//import java.security.MessageDigest;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * 简直看不懂我自己写是算法.难受,功能反正实现了.具体可以看 h5st.js
// */
//@Slf4j
//public class HMAC {
//
//    public static void main(String[] args) throws Exception {
////        String body = "";
////        CactusVo cactusVo = signH5st(body, "order_recycle_m");
////        System.out.println(JSON.toJSONString(cactusVo));
//        String mck = "pt_key=app_openAAJin_hgADAEiJSNd1jdPdvWeFWeS8XadSP59ECbF38h-WMjhQHzHFC-rOBmdpYhW6g0SkaP_5I; pt_pin=jd_52276334d7a18;";
//        emptyTrash("248213487560", mck);
//    }
//
//    public static void emptyTrash(String orderId, String mck) throws Exception {
//        String body = String.format("{\"orderId\": \"%s\",\"recycleType\": 2,\"tenantCode\": \"jgm\",\"bizModelCode\": \"2\",\"bizModeClientType\": \"M\",\"bizModeFramework\": \"Taro\",\"externalLoginType\": 1}", orderId);
//        CactusVo cactusVo = signH5st(body, "order_recycle_m");
//        Map<String, Object> formMap = new HashMap<>();
//        formMap.put("t", cactusVo.getTimestamp());
//        formMap.put("loginType", "2");
//        formMap.put("appid", "m_core");
//        formMap.put("client", "Win32");
//        formMap.put("clientVersion", "");
//        formMap.put("functionId", "order_recycle_m");
//        formMap.put("body", body.trim());
//        formMap.put("h5st", cactusVo.getH5st());
//        String result2 = HttpRequest.post("https://api.m.jd.com/client.action")
//                .header(Header.COOKIE, mck)
//                .header(Header.ORIGIN, "https://wqs.jd.com")
//                .header(Header.REFERER, "https://wqs.jd.com/")
//                .header(Header.HOST, "api.m.jd.com")
//                .form(formMap)//表单内容
//                .timeout(20000)//超时，毫秒
//                .execute().body();
//        System.out.println(result2);
//    }
//
//    private static CactusVo signH5st(String body, String functionId) throws Exception {
//        CactusVo cactusVo = new CactusVo();
//        getCactusVo(cactusVo);
//        cactusVo.setBodyStr(body);
//        if (StrUtil.isBlank(functionId)) {
//            cactusVo.setFunctionId("order_recycle_m");
//        } else {
//            cactusVo.setFunctionId(functionId);
//        }
////        cactusVo.setBodyStr("{\"appType\":3,\"bizType\":\"2\",\"source\":\"-1\",\"deviceUUId\":\"\",\"platform\":3,\"uuid\":\"1201797520196000\",\"sceneval\":\"2\",\"systemBaseInfo\":\"{\\\"brand\\\":\\\"iPhone\\\",\\\"model\\\":\\\"iPhone\\\",\\\"system\\\":\\\"iOS\\\",\\\"pixelRatio\\\":3.0000001192092896,\\\"screenWidth\\\":390,\\\"screenHeight\\\":844,\\\"windowWidth\\\":390,\\\"windowHeight\\\":844,\\\"version\\\":\\\"\\\",\\\"statusBarHeight\\\":null,\\\"platform\\\":\\\"Win32\\\",\\\"language\\\":\\\"zh-CN\\\",\\\"fontSizeSetting\\\":null,\\\"SDKVersion\\\":\\\"\\\",\\\"albumAuthorized\\\":false,\\\"benchmarkLevel\\\":0,\\\"bluetoothEnabled\\\":false,\\\"cameraAuthorized\\\":false,\\\"enableDebug\\\":false,\\\"locationAuthorized\\\":false,\\\"locationEnabled\\\":false,\\\"microphoneAuthorized\\\":false,\\\"notificationAlertAuthorized\\\":false,\\\"notificationAuthorized\\\":false,\\\"notificationBadgeAuthorized\\\":false,\\\"notificationSoundAuthorized\\\":false,\\\"safeArea\\\":{\\\"bottom\\\":0,\\\"height\\\":0,\\\"left\\\":0,\\\"right\\\":0,\\\"top\\\":0,\\\"width\\\":0},\\\"wifiEnabled\\\":false}\",\"orderId\":\"248244495490\",\"recycleType\":2,\"tenantCode\":\"jgm\",\"bizModelCode\":\"2\",\"bizModeClientType\":\"M\",\"bizModeFramework\":\"Taro\",\"externalLoginType\":1,\"token\":\"3852b12f8c4d869b7ed3e2b3c68c9436\",\"appId\":\"m91d27dbf599dff74\"}");
//        String bodySign = SHA(cactusVo.getBodyStr(), "SHA-256");
//        cactusVo.setBodySign(bodySign);
//        String mdStr = String.format("appid:m_core&body:%s&client:Win32&clientVersion:&functionId:%s&t:%s", cactusVo.getBodySign(), cactusVo.getFunctionId(), cactusVo.getTimestamp() + "");
//        String md = HmacSHA(mdStr, cactusVo.getAlgo(), "HmacSHA256");
//        cactusVo.setLastMd(md);
//        String h5st = String.format("%s;%s;8108f;%s;%s;3.0;%s", cactusVo.getTs(), cactusVo.getFp(), cactusVo.getTk(), md, cactusVo.getTimestamp());
//        cactusVo.setH5st(h5st);
//        log.info("当前删除订单签证信息为msg:{}", cactusVo);
//        System.out.println(JSON.toJSONString(cactusVo));
//        return cactusVo;
//    }
//
//    private static CactusVo getCactusVo(CactusVo cactusVo) {
//        try {
////            CactusVo cactusVo = new CactusVo();
//            String result2 = HttpRequest.post("https://cactus.jd.com/request_algo?g_ty=ajax")
//                    .header(Header.ORIGIN, "https://wqs.jd.com")
//                    .body(JSON.toJSONString(cactusVo))//表单内容
//                    .timeout(20000)//超时，毫秒
//                    .execute().body();
//            if (result2.contains("HmacSHA512") || result2.contains("HmacSHA256")) {
//                JSONObject signTkAndRdMap = JSON.parseObject(JSON.parseObject(JSON.parseObject(result2).getString("data")).getString("result"));
//                log.info("删除订单签证算法返回值msg:{}", signTkAndRdMap);
//                String tk = signTkAndRdMap.getString("tk");
//                String algo = signTkAndRdMap.getString("algo");
//                int start = algo.indexOf("var rd='");
//                String rd = algo.substring(start + 8, start + 12 + 8);
//                cactusVo.setTk(tk);
//                cactusVo.setRd(rd);
//                if (result2.contains("HmacSHA512")) {
//                    cactusVo.setHmacSHA("HmacSHA512");
//                    //${tk}${fp}${ts}${ai}${rd}
//                    String str = String.format("%s%s%s%s%s", cactusVo.getTk(), cactusVo.getFp(), cactusVo.getTs(), cactusVo.getAppId(), cactusVo.getRd());
//                    String algoData = HmacSHA(str, cactusVo.getTk(), "HmacSHA512");
//                    cactusVo.setAlgo(algoData);
//                }
//                if (!result2.contains("HmacSHA512") && result2.contains("SHA512")) {
//                    cactusVo.setHmacSHA("SHA512");
//                    String str = String.format("%s%s%s%s%s", cactusVo.getTk(), cactusVo.getFp(), cactusVo.getTs(), cactusVo.getAppId(), cactusVo.getRd());
//                    String algoData = SHA(str, "SHA-512");
//                    cactusVo.setAlgo(algoData);
//                }
//                if (result2.contains("HmacSHA256")) {
//                    cactusVo.setHmacSHA("HmacSHA256");
//                    String str = String.format("%s%s%s%s%s", cactusVo.getTk(), cactusVo.getFp(), cactusVo.getTs(), cactusVo.getAppId(), cactusVo.getRd());
//                    String algoData = HmacSHA(str, cactusVo.getTk(), "HmacSHA256");
//                    cactusVo.setAlgo(algoData);
//                }
//                if (!result2.contains("HmacSHA256") && result2.contains("SHA256")) {
//                    cactusVo.setHmacSHA("SHA-256");
//                    String str = String.format("%s%s%s%s%s", cactusVo.getTk(), cactusVo.getFp(), cactusVo.getTs(), cactusVo.getAppId(), cactusVo.getRd());
//                    String algoData = SHA(str, "SHA256");
//                    cactusVo.setAlgo(algoData);
//                }
//                return cactusVo;
//            } else {
//                return getCactusVo(cactusVo);
//            }
//        } catch (Exception e) {
//            log.error("获取签证订单报错了msg:{}", e.getMessage());
//        }
//        return null;
//    }
//
//
//    /**
//     * 阿帕奇的工具
//     *
//     * @param str //SHA-256 SHA-512
//     * @return
//     * @throws Exception
//     */
//    public static String SHA(String str, String shaSign) {
//        try {
//            //SHA-256 SHA-512
//            MessageDigest messageDigest = MessageDigest.getInstance(shaSign);
//            byte[] hash = messageDigest.digest(str.getBytes("UTF-8"));
//            String encdeStr = Hex.encodeHexString(hash);
//            return encdeStr;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "";
//    }
//
//    /**
//     * //HmacSHA512 HmacSHA256
//     *
//     * @param data
//     * @param key
//     * @param hmacshaSign
//     * @return
//     * @throws Exception
//     */
//    public static String HmacSHA(String data, String key, String hmacshaSign) throws Exception {
//        //HmacSHA512 HmacSHA256
//        Mac mac = Mac.getInstance(hmacshaSign);
//        mac.init(new SecretKeySpec(key.getBytes("UTF-8"), hmacshaSign));
//        byte[] signData = mac.doFinal(data.getBytes("UTF-8"));
//        StringBuilder sb = new StringBuilder();
//        for (byte item : signData) {
//            sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
//        }
//        return sb.toString();
//    }
//
//
//}
