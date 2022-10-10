package com.xd.pre.common.h5st;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 简直看不懂我自己写是算法.难受,功能反正实现了.具体可以看 h5st.js
 */
@Slf4j
public class HMAC {

    public static void main(String[] args) throws Exception {
//        String body = "";
//        CactusVo cactusVo = signH5st(body, "order_recycle_m");
//        System.out.println(JSON.toJSONString(cactusVo));
        String mck = "pt_key=app_openAAJin_hgADAEiJSNd1jdPdvWeFWeS8XadSP59ECbF38h-WMjhQHzHFC-rOBmdpYhW6g0SkaP_5I; pt_pin=jd_52276334d7a18;";
        String functionId = "order_recycle_m";
        Boolean aBoolean = emptyTrash("248244495490", mck, functionId);
        System.out.println(aBoolean);
    }

    public static Boolean emptyTrash(String orderId, String mck, String functionId) {
        try {
            String body = String.format("{\"orderId\": \"%s\",\"recycleType\": 2,\"tenantCode\": \"jgm\",\"bizModelCode\": \"2\",\"bizModeClientType\": \"M\",\"bizModeFramework\": \"Taro\",\"externalLoginType\": 1}", orderId);
            CactusVo cactusVo = signH5st(body, functionId);
            log.info("当前删除订单签证信息为msg:{}", cactusVo);
            Map<String, Object> formMap = new HashMap<>();
            formMap.put("t", cactusVo.getTimestamp());
            formMap.put("loginType", "2");
            formMap.put("appid", "m_core");
            formMap.put("client", "Win32");
            formMap.put("clientVersion", "");
            formMap.put("functionId", functionId);
            formMap.put("body", body.trim());
            formMap.put("h5st", cactusVo.getH5st());
            String result2 = HttpRequest.post("https://api.m.jd.com/client.action")
                    .header(Header.COOKIE, mck)
                    .header(Header.ORIGIN, "https://wqs.jd.com")
                    .header(Header.REFERER, "https://wqs.jd.com/")
                    .header(Header.HOST, "api.m.jd.com")
                    .form(formMap)//表单内容
                    .timeout(20000)//超时，毫秒
                    .execute().body();
            log.info("当前删除订单信息为msg:{},orderId:{},mck:{}", result2, orderId, mck);
            String resultBody = JSON.parseObject(result2).getString("body");
            if (StrUtil.isNotBlank(resultBody) && resultBody.equals("true")) {
                return Boolean.TRUE;
            }
        } catch (Exception e) {
            log.error("删除订单出现未知错误msg:{}", e.getMessage());
        }
        return Boolean.FALSE;
    }

    private static CactusVo signH5st(String body, String functionId) throws Exception {
        CactusVo cactusVo = new CactusVo();
        getCactusVo(cactusVo);
        cactusVo.setBodyStr(body);
        if (StrUtil.isBlank(functionId)) {
            cactusVo.setFunctionId("order_recycle_m");
        } else {
            cactusVo.setFunctionId(functionId);
        }
        String bodySign = SHA(cactusVo.getBodyStr(), "SHA-256");
        cactusVo.setBodySign(bodySign);
        String mdStr = String.format("appid:m_core&body:%s&client:Win32&clientVersion:&functionId:%s&t:%s", cactusVo.getBodySign(), cactusVo.getFunctionId(), cactusVo.getTimestamp() + "");
        String md = HmacSHA(mdStr, cactusVo.getAlgo(), "HmacSHA256");
        cactusVo.setLastMd(md);
        String h5st = String.format("%s;%s;8108f;%s;%s;3.0;%s", cactusVo.getTs(), cactusVo.getFp(), cactusVo.getTk(), md, cactusVo.getTimestamp());
        cactusVo.setH5st(h5st);
        return cactusVo;
    }

    private static CactusVo getCactusVo(CactusVo cactusVo) {
        try {
//            CactusVo cactusVo = new CactusVo();
            String result2 = HttpRequest.post("https://cactus.jd.com/request_algo?g_ty=ajax")
                    .header(Header.ORIGIN, "https://wqs.jd.com")
                    .body(JSON.toJSONString(cactusVo))//表单内容
                    .timeout(20000)//超时，毫秒
                    .execute().body();
            if (result2.contains("HmacSHA512") || result2.contains("HmacSHA256")) {
                JSONObject signTkAndRdMap = JSON.parseObject(JSON.parseObject(JSON.parseObject(result2).getString("data")).getString("result"));
                String tk = signTkAndRdMap.getString("tk");
                String algo = signTkAndRdMap.getString("algo");
                int start = algo.indexOf("var rd='");
                String rd = algo.substring(start + 8, start + 12 + 8);
                cactusVo.setTk(tk);
                cactusVo.setRd(rd);
                if (result2.contains("HmacSHA512")) {
                    cactusVo.setHmacSHA("HmacSHA512");
                    //${tk}${fp}${ts}${ai}${rd}
                    String str = String.format("%s%s%s%s%s", cactusVo.getTk(), cactusVo.getFp(), cactusVo.getTs(), cactusVo.getAppId(), cactusVo.getRd());
                    String algoData = HmacSHA(str, cactusVo.getTk(), "HmacSHA512");
                    cactusVo.setAlgo(algoData);
                }
                if (!result2.contains("HmacSHA512") && result2.contains("SHA512")) {
                    cactusVo.setHmacSHA("SHA512");
                    String str = String.format("%s%s%s%s%s", cactusVo.getTk(), cactusVo.getFp(), cactusVo.getTs(), cactusVo.getAppId(), cactusVo.getRd());
                    String algoData = SHA(str, "SHA-512");
                    cactusVo.setAlgo(algoData);
                }
                if (result2.contains("HmacSHA256")) {
                    cactusVo.setHmacSHA("HmacSHA256");
                    String str = String.format("%s%s%s%s%s", cactusVo.getTk(), cactusVo.getFp(), cactusVo.getTs(), cactusVo.getAppId(), cactusVo.getRd());
                    String algoData = HmacSHA(str, cactusVo.getTk(), "HmacSHA256");
                    cactusVo.setAlgo(algoData);
                }
                if (!result2.contains("HmacSHA256") && result2.contains("SHA256")) {
                    cactusVo.setHmacSHA("SHA-256");
                    String str = String.format("%s%s%s%s%s", cactusVo.getTk(), cactusVo.getFp(), cactusVo.getTs(), cactusVo.getAppId(), cactusVo.getRd());
                    String algoData = SHA(str, "SHA256");
                    cactusVo.setAlgo(algoData);
                }
                return cactusVo;
            } else {
                return getCactusVo(cactusVo);
            }
        } catch (Exception e) {
            log.error("获取签证订单报错了msg:{}", e.getMessage());
        }
        return null;
    }


    /**
     * 阿帕奇的工具
     *
     * @param str //SHA-256 SHA-512
     * @return
     * @throws Exception
     */
    public static String SHA(String str, String shaSign) {
        try {
            //SHA-256 SHA-512
            MessageDigest messageDigest = MessageDigest.getInstance(shaSign);
            byte[] hash = messageDigest.digest(str.getBytes("UTF-8"));
            String encdeStr = Hex.encodeHexString(hash);
            return encdeStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * //HmacSHA512 HmacSHA256
     *
     * @param data
     * @param key
     * @param hmacshaSign
     * @return
     * @throws Exception
     */
    public static String HmacSHA(String data, String key, String hmacshaSign) throws Exception {
        //HmacSHA512 HmacSHA256
        Mac mac = Mac.getInstance(hmacshaSign);
        mac.init(new SecretKeySpec(key.getBytes("UTF-8"), hmacshaSign));
        byte[] signData = mac.doFinal(data.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte item : signData) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }


}
