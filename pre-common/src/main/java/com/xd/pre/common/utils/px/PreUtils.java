package com.xd.pre.common.utils.px;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.qrcode.BufferedImageLuminanceSource;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.common.HybridBinarizer;
import com.xd.pre.common.utils.px.dto.UrlEntity;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class PreUtils {


    /**
     * 随机指定范围内N个不重复的数
     * 最简单最基本的方法
     *
     * @param min 指定范围最小值
     * @param max 指定范围最大值
     * @param n   随机数个数
     */
    public static int[] randomCommon(int min, int max, int n) {
        if (min == 0 && max == 0 && n == 1) {
            int[] ints = new int[1];
            ints[0] = 0;
            return ints;
        }
        if (min == 0 && max == 1 && n == 1) {
            int[] ints = new int[1];
            ints[0] = 0;
            return ints;
        }
        if (n > (max - min + 1) || max < min) {
            return null;
        }
        int[] result = new int[n];
        int count = 0;
        while (count < n) {
            int num = (int) (Math.random() * (max - min)) + min;
            boolean flag = true;
            for (int j = 0; j < n; j++) {
                if (num == result[j]) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                result[count] = num;
                count++;
            }
        }
        return result;
    }

    /**
     * 随机生成字符串
     *
     * @param length
     * @return
     */
    public static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    public static String getRandomNum(int length) {
        String str = "0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(10);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    public static String getWskey(String Cookie) {
        try {
            String[] split = Cookie.split(";");
            if (split.length == 1) {
                return split[0];
            }
            for (int i = 0; i < split.length; i++) {
                String trim = split[i].trim();
                if (StrUtil.isNotBlank(trim) && trim.contains("wskey=")) {
                    return trim.split("wskey=")[1];
                }
            }
        } catch (Exception e) {
            log.error("解析ck失败msg:[Cookie:{}]", Cookie);
        }
        return null;

    }


    public static String get_pt_pin(String Cookie) {
        try {
            String[] split = Cookie.split(";");
            if (split.length == 1) {
                return split[0];
            }
            for (int i = 0; i < split.length; i++) {
                String trim = split[i].trim();
                if (StrUtil.isNotBlank(trim) && trim.contains("pt_pin=")) {
                    return trim.split("pt_pin=")[1];
                }
                if (StrUtil.isNotBlank(trim) && trim.contains("pin=")) {
                    String s = trim.split("pin=")[0];
                    if (StrUtil.isBlank(s)) {
                        return trim.split("pin=")[1];
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析ck失败msg:[Cookie:{}]", Cookie);
        }
        return null;

    }

    /**
     * 获取字符串中的数字 分段提取
     *
     * @param str
     * @return
     */
    public static List<String> getNum(String str) {
        String regex = "(\\d+)";
        List<String> nums = new LinkedList<>();
        Pattern r = Pattern.compile(regex);
        Matcher m = r.matcher(str);
        while (m.find()) {
            nums.add(m.group());
        }
        return nums;
    }

    public static Map<String, String> getCookies(String ckContext) {
        Map<String, String> cookies = new HashMap<>();
        String[] split = ckContext.split(";");
        for (String ckKey : split) {
            String[] keyAndValue = ckKey.trim().split("=");
            if (keyAndValue.length == 2) {
                cookies.put(keyAndValue[0], keyAndValue[1]);
            }
        }
        return cookies;
    }


    // 正确的IP拿法，即优先拿site-local地址
    public static InetAddress getLocalHostLANAddress() {
        try {
            InetAddress candidateAddress = null;
            // 遍历所有的网络接口
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // 在所有的接口下再遍历IP
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            // site-local类型的地址未被发现，先记录候选地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            // 如果没有发现 non-loopback地址.只能用最次选的方案
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getIPAddress(HttpServletRequest request) {
        String ip = null;
        log.info("       ip = request.getRemoteAddr()msg:{}", ip = request.getRemoteAddr());
        //X-Forwarded-For：Squid 服务代理
        String ipAddresses = request.getHeader("X-Forwarded-For");
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //Proxy-Client-IP：apache 服务代理
            ipAddresses = request.getHeader("Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //WL-Proxy-Client-IP：weblogic 服务代理
            ipAddresses = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //HTTP_CLIENT_IP：有些代理服务器
            ipAddresses = request.getHeader("HTTP_CLIENT_IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //X-Real-IP：nginx服务代理
            ipAddresses = request.getHeader("X-Real-IP");
            log.info("       request.getHeader(\"X-Real-IP\")msg:{}", request.getHeader("X-Real-IP"));
        }

        //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (ipAddresses != null && ipAddresses.length() != 0) {
            log.info("       ip = request.getRemoteAddr() msg:{}", ipAddresses);
            ip = ipAddresses.split(",")[0];
        }
        String remoteAddr = request.getRemoteAddr();
        //还是不能获取到，最后再通过request.getRemoteAddr();获取
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private static int getNum(int start, int end) {
        return (int) (Math.random() * (end - start + 1) + start);
    }

    /**
     * 返回手机号码
     */
    private static String[] telFirst = "134,135,136,137,138,139,150,151,152,157,158,159,130,131,132,155,156,133,153".split(",");

    public static String getTel() {
        int index = getNum(0, telFirst.length - 1);
        String first = telFirst[index];
        String second = String.valueOf(getNum(1, 888) + 10000).substring(1);
        String third = String.valueOf(getNum(1, 9100) + 10000).substring(1);
        return first + second + third;
    }

    /**
     * 解析url
     *
     * @param url
     * @return
     */
    public static UrlEntity parseUrl(String url) {
        try {
            UrlEntity entity = new UrlEntity();
            if (url == null) {
                return entity;
            }
            url = url.trim();
            if (url.equals("")) {
                return entity;
            }
            String[] urlParts = url.split("\\?");
            entity.baseUrl = urlParts[0];
            //没有参数
            if (urlParts.length == 1) {
                return entity;
            }
            //有参数
            String[] params = urlParts[1].split("&");
            entity.params = new HashMap<>();
            for (String param : params) {
                int i = param.indexOf("=");
//                String[] keyValue = param.split("=");
                entity.params.put(param.substring(0, i), param.substring(i + 1, param.length()));
            }
            return entity;
        } catch (Exception e) {

        }
        return null;

    }


    public static String getUseCk(String ck) {
        String[] split = ck.split(";");
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : split) {
            if (StrUtil.isBlank(s)) {
                continue;
            }
            if (s.contains("pin=") && !s.contains("pt_pin=")) {
                stringBuilder.append(s + ";");
            }
            if (s.contains("wskey=")) {
                stringBuilder.append(s + ";");
            }
            //mck
            if (s.contains("pt_pin=")) {
                stringBuilder.append(s + ";");
            }
            if (s.contains("pt_key")) {
                stringBuilder.append(s + ";");
            }

        }
        ck = stringBuilder.toString();
        if (StrUtil.isBlank(ck)) {
            return null;
        }
        return ck;
    }

    public static String jumpIosHrefUrl(String payData) {
        JSONObject parseObject = JSON.parseObject(payData);
        String appid = "wxe75a2e68877315fb";
        JSONObject body = JSON.parseObject(parseObject.get("body").toString());
        String payUrl = String.format("weixin://app/%s/pay/?nonceStr=%s&package=Sign%%3DWXPay&partnerId=%s&prepayId=%s&timeStamp=%s&sign=%s",
                appid, body.get("nonceStr"), body.get("partnerId"), body.get("prepayId"), body.get("timeStamp"), body.get("sign")
        );
        return URLEncoder.encode(payUrl);
    }

    public static String parsePayUrl(InputStream inputStream) {
        MultiFormatReader reader = null;
        BufferedImage image;
        try {
            image = ImageIO.read(inputStream);
            if (image == null) {
                throw new Exception("cannot read image from inputstream.");
            }
            final LuminanceSource source = new BufferedImageLuminanceSource(image);
            final BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            final Map<DecodeHintType, String> hints = new HashMap<DecodeHintType, String>();
            hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
            // 解码设置编码方式为：utf-8，
            reader = new MultiFormatReader();
            String payurlData = reader.decode(bitmap, hints).getText();
            return payurlData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Map<String, String> buildIpMap(String ip) {
        HashMap<String, String> ipmap = new HashMap<>();
        ipmap.put("X-Forwarded-For", ip);
        ipmap.put("Proxy-Client-IP", ip);
        ipmap.put("WL-Proxy-Client-IP", ip);
        ipmap.put("HTTP_CLIENT_IP", ip);
        ipmap.put("X-REMOTE-ADDR", ip);
        ipmap.put("X-REMOTE-IP", ip);
        ipmap.put("X-Real-IP", ip);
        ipmap.put("Client-ip", ip);
        ipmap.put("X-Client-IP", ip);
        ipmap.put("REMOTE_ADDR", ip);
        ipmap.put("X-Originating-IP", ip);
        return ipmap;
    }

    private static boolean isTab = true;

    public static void main(String[] args) {
        System.out.println(stringToFormatJSON("{\"status_code\":0,\"status_msg\":\"\",\"data\":{\"shop_product_cards\":[{\"shop_id\":\"GceCTPIk\",\"shop_insurance\":null,\"valid_product_cards\":[{\"product_id\":\"3556357046087622442\",\"title\":\"App Store 充值卡 10元（电子卡）- Apple ID 充值 / iOS 充值\",\"pic\":\"https://p3-item.ecombdimg.com/img/ecom-shop-material/v1_GceCTPIk_71074285399703063340031_33d69623d49799ab8f7f0f56d1ce387d_sx_187325_www800-800~tplv-5mmsx3fupr-resize:200:200.png\",\"sku_id\":\"1736502463777799\",\"spec_info\":[{\"name\":\"App Store 充值卡 10元（电子卡）\",\"value\":\"App Store 充值卡 10元（电子卡）\"}],\"biz_kind\":0,\"cart_id\":\"\",\"price\":1000,\"pay_type\":1,\"is_virtual\":true,\"is_presell\":false,\"is_topup\":false,\"is_multi_phase\":false,\"multi_phase\":null,\"is_cross_board\":false,\"cross_board_type\":0,\"buy_num\":1,\"stock_info\":{\"num\":977955,\"warehouse_id\":0,\"stock_items\":[{\"sku_id\":\"1736502463777799\",\"stock_type\":1,\"stock_num\":1,\"warehouse_id\":\"0\"}]},\"limit_info\":{\"min\":1,\"max\":1,\"toast_up\":\"该商品最多购买1件\",\"toast_down\":\"该商品最少购买1件\",\"limit_type\":\"\"},\"is_limited\":false,\"limit_reason\":\"\",\"limit_reason_code\":\"\",\"expect_ship_time\":\"\",\"delivery_info\":{\"color\":\"rgba(22, 24, 35, 0.75)\",\"delivery_url\":\"\"},\"service_tags\":[{\"text\":\"不支持退\",\"bg_color\":\"\",\"color\":\"\",\"type\":0,\"icon\":\"\"}],\"stock_tags\":[],\"render_biz\":0,\"extra\":\"\",\"product_category\":{\"fourth_cid\":0,\"third_cid\":28660,\"second_cid\":22105,\"first_cid\":20118},\"render_sku_extra\":\"{\\\"n_sku_id\\\":1736502463777799,\\\"act_id\\\":0}\",\"store_info\":null,\"product_biz_identity\":{\"seller_type\":\"\",\"vertical_market\":\"\",\"source\":\"\"},\"privilege_tag_keys\":[],\"select_privilege_properties\":[]}],\"invalid_product_cards\":[],\"given_product_list\":null,\"given_product_view_info\":null,\"select_privilege_properties\":[],\"valid_num\":1,\"shop_add_on_info\":null,\"tip\":null,\"shop_stock_out_handle_modes\":null}],\"all_forbidden\":false,\"all_forbidden_reason\":\"\",\"shop_info_map\":{\"GceCTPIk\":{\"shop_id\":\"GceCTPIk\",\"forbidden\":false,\"shop_name\":\"App Store 充值卡专卖店\",\"logo\":\"https://p6-item.ecombdimg.com/img/tos-cn-i-6vegkygxbk/1691ffb52ece43309b4d3bab644d6215~tplv-5mmsx3fupr-resize:200:200.png\",\"tag_tip_content\":{\"tip_tags\":[{\"id\":\"shop_tag_brand\",\"icon_urls\":[\"https://p9-jumanji.ecombdimg.com/tos-cn-i-flbuepwtip/944a74e229a9a4268ec79b9cc518d568.png~tplv-flbuepwtip-360p.image\"],\"icon_height\":16,\"icon_width\":16,\"promotion_id\":\"\",\"position\":0}],\"content\":null,\"show_type\":0,\"extra_info\":null}}},\"sku_insurance_map\":{},\"sku_campaign_info_map\":{},\"top_alert_info\":{},\"address\":null,\"leave_sub_title\":\"\",\"coupon_result\":{\"show_coupon\":false,\"tip\":\"\"},\"marquee_info\":{\"marquee_content\":[{\"avatar\":\"https://p3-ecom-commentpic.byteimg.com/tos-cn-i-fceoirpogb/60e940074453fd9367a22659bcf3f43b.png~tplv-fceoirpogb-image.image\",\"content\":\"23小时前给出五星好评！\"},{\"avatar\":\"https://p3.douyinpic.com/aweme/100x100/aweme-avatar/tos-cn-avt-0015_e70460d2b1efea69c3b3c54a8912128d.jpeg?from=4010531038\",\"content\":\"19分钟前正在抢购！\"},{\"avatar\":\"https://p3.douyinpic.com/aweme/100x100/aweme-avatar/tos-cn-avt-0015_f9132e00706faba965420d4779bf3604.jpeg?from=4010531038\",\"content\":\"22分钟前下单成功！\"}],\"show_marquee\":true},\"pay_method\":{\"pay_amount\":1000,\"pay_infos\":[{\"show_name\":\"支付宝\",\"pay_type\":2,\"sub_way\":0,\"select\":true,\"show_num\":0,\"pay_id\":\"10000\",\"support\":true,\"tag\":\"\",\"sub_methods\":null,\"pay_type_desc\":[],\"icon_url\":\"https://p3-item.ecombdimg.com/img/static-assets/4fbb3fd72b1c6911c0d44d85c17ccada.png~tplv-5mmsx3fupr-image.png\",\"extra_info\":null,\"voucher_msg_list\":[],\"more_voucher_msg_list\":null,\"unsupported_reason\":\"\",\"home_page_banner\":null,\"stable_status\":1,\"pay_change_info\":{\"toast_text\":\"\",\"toast_type\":0},\"sdk_service\":2,\"toast_msg_info\":null},{\"show_name\":\"抖音支付\",\"pay_type\":10,\"sub_way\":0,\"select\":false,\"show_num\":1,\"pay_id\":\"30000\",\"support\":true,\"tag\":\"\",\"sub_methods\":[{\"show_name\":\"添加银行卡支付\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30001\",\"support\":true,\"icon_url\":\"https://lf9-infras.bytetos.com/obj/static-assets/ec02e6caaca2e8c2491d6b07245d6213.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元起\"},\"card_type\":\"NEW_CARD\",\"bank_code\":\"\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用银行卡支付\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加农业银行储蓄卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30002\",\"support\":true,\"icon_url\":\"https://lf3-infras.bytetos.com/obj/static-assets/a6e59ded69266c18bd693b22f430f399.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"DEBIT\",\"bank_code\":\"ABC\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用农行储蓄卡\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加建设银行储蓄卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30003\",\"support\":true,\"icon_url\":\"https://lf26-infras.bytetos.com/obj/static-assets/e6aa214f1dfda334f8f4d014755f4438.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"DEBIT\",\"bank_code\":\"CCB\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用建行储蓄卡\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加邮储银行储蓄卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30004\",\"support\":true,\"icon_url\":\"https://lf6-infras.bytetos.com/obj/static-assets/7b5aa111bcd22428f26959c5b6d42183.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"DEBIT\",\"bank_code\":\"PSBC\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用邮储储蓄卡\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加中国银行储蓄卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30005\",\"support\":true,\"icon_url\":\"https://lf9-infras.bytetos.com/obj/static-assets/36185557c0282f6e84ff43193e00a191.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"DEBIT\",\"bank_code\":\"BOC\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用中行储蓄卡\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加招商银行储蓄卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30006\",\"support\":true,\"icon_url\":\"https://lf6-infras.bytetos.com/obj/static-assets/cc8da52726b220b9fac0f817313c42c9.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"DEBIT\",\"bank_code\":\"CMB\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用招行储蓄卡\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加平安银行信用卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30007\",\"support\":true,\"icon_url\":\"https://lf3-infras.bytetos.com/obj/static-assets/cf2c616bdf032d0019c2986f957d3ee7.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"CREDIT\",\"bank_code\":\"PINGAN\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用平安信用卡\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加农业银行信用卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30008\",\"support\":true,\"icon_url\":\"https://lf3-infras.bytetos.com/obj/static-assets/a6e59ded69266c18bd693b22f430f399.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"CREDIT\",\"bank_code\":\"ABC\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用农行信用卡\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加交通银行储蓄卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30009\",\"support\":true,\"icon_url\":\"https://lf3-infras.bytetos.com/obj/static-assets/802c7b0df295fc86ddad37270aaaa5af.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"DEBIT\",\"bank_code\":\"BANKCOMM\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用交行储蓄卡\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加交通银行信用卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30010\",\"support\":true,\"icon_url\":\"https://lf3-infras.bytetos.com/obj/static-assets/802c7b0df295fc86ddad37270aaaa5af.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"CREDIT\",\"bank_code\":\"BANKCOMM\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用交行信用卡\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加建设银行信用卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30011\",\"support\":true,\"icon_url\":\"https://lf26-infras.bytetos.com/obj/static-assets/e6aa214f1dfda334f8f4d014755f4438.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"CREDIT\",\"bank_code\":\"CCB\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用建行信用卡\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加广发银行储蓄卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30012\",\"support\":true,\"icon_url\":\"https://lf3-infras.bytetos.com/obj/static-assets/345c00b66ca72e7e918a37bbae7db009.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"DEBIT\",\"bank_code\":\"CGB\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用广发储蓄卡\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加广发银行信用卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30013\",\"support\":true,\"icon_url\":\"https://lf3-infras.bytetos.com/obj/static-assets/345c00b66ca72e7e918a37bbae7db009.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"CREDIT\",\"bank_code\":\"CGB\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用广发信用卡\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加中信银行储蓄卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30014\",\"support\":true,\"icon_url\":\"https://lf3-infras.bytetos.com/obj/static-assets/8c83a0079b20c03e131b1f211a70adc0.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"DEBIT\",\"bank_code\":\"CITIC\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用中信储蓄卡\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加中信银行信用卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30015\",\"support\":true,\"icon_url\":\"https://lf3-infras.bytetos.com/obj/static-assets/8c83a0079b20c03e131b1f211a70adc0.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"CREDIT\",\"bank_code\":\"CITIC\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用中信信用卡\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加招商银行信用卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30016\",\"support\":true,\"icon_url\":\"https://lf6-infras.bytetos.com/obj/static-assets/cc8da52726b220b9fac0f817313c42c9.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"CREDIT\",\"bank_code\":\"CMB\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用招行信用卡\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加民生银行储蓄卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30017\",\"support\":true,\"icon_url\":\"https://lf26-infras.bytetos.com/obj/static-assets/b78e8a9fa5d3b0ebf2ad34d2f68c1221.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"DEBIT\",\"bank_code\":\"CMBC\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用民生储蓄卡\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加华夏银行信用卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30018\",\"support\":true,\"icon_url\":\"https://lf26-infras.bytetos.com/obj/static-assets/b5ad68b26e4d2fcc86365c3e4dedd6e5.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"CREDIT\",\"bank_code\":\"HXB\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用华夏信用卡\",\"support_one_key_sign\":false},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加工商银行储蓄卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30019\",\"support\":true,\"icon_url\":\"https://lf9-infras.bytetos.com/obj/static-assets/4758e436fffa404717906b923f9c4b85.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"DEBIT\",\"bank_code\":\"ICBC\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用工行储蓄卡\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加工商银行信用卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30020\",\"support\":true,\"icon_url\":\"https://lf9-infras.bytetos.com/obj/static-assets/4758e436fffa404717906b923f9c4b85.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"CREDIT\",\"bank_code\":\"ICBC\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用工行信用卡\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加平安银行储蓄卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30021\",\"support\":true,\"icon_url\":\"https://lf3-infras.bytetos.com/obj/static-assets/cf2c616bdf032d0019c2986f957d3ee7.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"DEBIT\",\"bank_code\":\"PINGAN\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用平安储蓄卡\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加邮储银行信用卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30022\",\"support\":true,\"icon_url\":\"https://lf6-infras.bytetos.com/obj/static-assets/7b5aa111bcd22428f26959c5b6d42183.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"CREDIT\",\"bank_code\":\"PSBC\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用邮储信用卡\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加浦发银行储蓄卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30023\",\"support\":true,\"icon_url\":\"https://lf6-infras.bytetos.com/obj/static-assets/6809993839825538a00aeef9d2667f09.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"DEBIT\",\"bank_code\":\"SPDB\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用浦发储蓄卡\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}},{\"show_name\":\"添加浦发银行信用卡\",\"pay_type\":9,\"select\":false,\"pay_id\":\"30024\",\"support\":true,\"icon_url\":\"https://lf6-infras.bytetos.com/obj/static-assets/6809993839825538a00aeef9d2667f09.png\",\"sub_way\":0,\"voucher_msg_list\":[\"立减7元\"],\"extra_info\":{\"card_no\":\"\",\"bank_card_id\":\"\",\"mobile_mask\":\"\",\"pay_type_desc\":\"\",\"fee_desc\":\"\",\"balance_amount\":0,\"freezed_amount\":0,\"voucher_info\":{\"vouchers\":[{\"voucher_no\":\"B202208280109179569277953\",\"batch_no\":\"\",\"promotion_product_code\":\"\",\"voucher_type\":\"discount_voucher\",\"voucher_name\":\"\",\"reduce_amount\":700,\"random_max_reduct_amount\":0,\"reached_amount\":0,\"used_amount\":0,\"label\":\"立减7元\",\"fund_type\":\"\"}],\"vouchers_label\":\"立减7元\",\"order_sub_fixed_voucher_amount\":700,\"order_sub_fixed_voucher_label\":\"绑卡立减7元\"},\"card_type\":\"CREDIT\",\"bank_code\":\"SPDB\",\"card_add_ext\":\"{\\\"promotion_process\\\":{\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"create_time\\\":1664703341}}\",\"card_style_short_name\":\"使用浦发信用卡\",\"support_one_key_sign\":true},\"sub_methods\":null,\"unsupported_reason\":\"\",\"protocol_info\":null,\"sub_ext\":null,\"bytepay_voucher_msg_list\":[{\"label\":\"立减7元\",\"show_type\":\"text\",\"url\":null}],\"sub_pay_voucher_msg_list\":null,\"sdk_service\":10,\"pay_type_voucher_msg_v2\":{\"tag12\":[],\"tag34\":[]}}],\"pay_type_desc\":[],\"icon_url\":\"https://p3-item.ecombdimg.com/img/static-assets/703f7b6c9ef0480bd5699dbd57df32f4.png~tplv-5mmsx3fupr-image.png\",\"extra_info\":null,\"voucher_msg_list\":[\"绑卡支付立减7元\"],\"more_voucher_msg_list\":[\"支付立减7元\"],\"unsupported_reason\":\"\",\"nopwd_pay_params\":{\"status\":\"UNSIGN\",\"need_guide\":false,\"subtitle\":\"\",\"skip_sdk\":false},\"home_page_guide_text\":\"更多优惠\",\"home_page_red_dot\":true,\"home_page_guide_action\":\"more\",\"home_page_show_style\":\"single\",\"home_page_banner\":{\"icon_url\":\"\",\"banner_text\":\"添加农业银行储蓄卡，立减7元\",\"btn_text\":\"去绑卡\",\"btn_action\":\"bindcard\",\"sub_pay_type_index\":\"30002\",\"theme_color\":\"#FE2C55\",\"activate_param\":\"\",\"credit_activate_url\":\"\"},\"stable_status\":1,\"pay_change_info\":{\"toast_text\":\"\",\"toast_type\":0},\"shrink_show_info\":{\"voucher_msg\":\"抖音支付立减7元\"},\"bubble_style_text\":\"\",\"sdk_service\":10,\"show_style\":\"expand\",\"toast_msg_info\":null},{\"show_name\":\"微信支付\",\"pay_type\":1,\"sub_way\":0,\"select\":false,\"show_num\":0,\"pay_id\":\"20000\",\"support\":true,\"tag\":\"\",\"sub_methods\":null,\"pay_type_desc\":[],\"icon_url\":\"https://p3-item.ecombdimg.com/img/static-assets/4fafdff911f3077b8ffa669c4c85d5a5.png~tplv-5mmsx3fupr-image.png\",\"extra_info\":null,\"voucher_msg_list\":null,\"more_voucher_msg_list\":null,\"unsupported_reason\":\"\",\"home_page_banner\":null,\"stable_status\":1,\"pay_change_info\":{\"toast_text\":\"\",\"toast_type\":0},\"sdk_service\":1,\"toast_msg_info\":null}],\"show_num\":3,\"zg_ext_info\":\"{\\\"activity_id\\\":\\\"AC220928161548900274289189\\\",\\\"credit_pay_param\\\":{\\\"fee_rate_per_day\\\":\\\"\\\",\\\"has_credit_param\\\":false,\\\"has_trade_time\\\":false,\\\"installment_starting_amount\\\":0,\\\"is_credit_activate\\\":false,\\\"remaining_credit\\\":0,\\\"trade_time\\\":0},\\\"decision_id\\\":\\\"1121923253676519_1664703341759548\\\",\\\"merchant_info\\\":{\\\"app_id\\\":\\\"NA202208012041063016245258\\\",\\\"ext_uid_type\\\":0,\\\"jh_app_id\\\":\\\"8000104428743\\\",\\\"jh_merchant_id\\\":\\\"100000010442\\\",\\\"merchant_id\\\":\\\"8020220801671981\\\",\\\"merchant_name\\\":\\\"上海格物致品网络科技有限公司\\\",\\\"merchant_short_to_customer\\\":\\\"抖音电商商家\\\"},\\\"promotion_ext\\\":\\\"{\\\\\\\"IsZjyFlag\\\\\\\":\\\\\\\"true\\\\\\\",\\\\\\\"ParamOrderId\\\\\\\":\\\\\\\"202210021735392523255296\\\\\\\",\\\\\\\"PromotionActivityIDs\\\\\\\":\\\\\\\"AC220929171013900012599847\\\\\\\"}\\\",\\\"promotion_process\\\":{\\\"create_time\\\":1664703341,\\\"process_id\\\":\\\"bc4f2c60a5cc3efba423b01de30acccedd\\\",\\\"process_info\\\":\\\"\\\"},\\\"qt_c_pay_url\\\":\\\"\\\",\\\"retain_c_pay_url\\\":\\\"\\\"}\",\"risk_info\":\"{\\\"biometric_params\\\":\\\"\\\",\\\"is_jailbreak\\\":\\\"\\\",\\\"openudid\\\":\\\"\\\",\\\"order_page_style\\\":0,\\\"checkout_id\\\":1,\\\"ecom_payapi\\\":true,\\\"ip\\\":\\\"42.85.233.220\\\"}\",\"dev_info\":\"{\\\"reqIp\\\":\\\"42.85.233.220\\\",\\\"os\\\":\\\"android\\\",\\\"isH5\\\":false,\\\"cjSdkVersion\\\":\\\"\\\",\\\"aid\\\":\\\"1128\\\",\\\"ua\\\":\\\"okhttp/3.14.4\\\",\\\"riskUa\\\":\\\"\\\",\\\"lang\\\":\\\"zh-Hans\\\",\\\"deviceId\\\":\\\"611755040972302\\\",\\\"osVersion\\\":\\\"5.1.1\\\",\\\"vendor\\\":\\\"\\\",\\\"model\\\":\\\"\\\",\\\"netType\\\":\\\"\\\",\\\"appVersion\\\":\\\"17.3.0\\\",\\\"appName\\\":\\\"aweme\\\",\\\"devicePlatform\\\":\\\"android\\\",\\\"deviceType\\\":\\\"SM-G973N\\\",\\\"channel\\\":\\\"dy_tiny_juyouliang_dy_and24\\\",\\\"openudid\\\":\\\"\\\",\\\"versionCode\\\":\\\"170300\\\",\\\"ac\\\":\\\"wifi\\\",\\\"brand\\\":\\\"samsung\\\",\\\"iid\\\":\\\"2124683041579725\\\",\\\"bioType\\\":\\\"\\\"}\",\"jh_ext_info\":\"{\\\"payapi_cache_id\\\":\\\"20221002173541759522z7y2a0b1c2dx\\\"}\",\"trace_id\":\"20221002173541759522z7y2a0b1c2dx\",\"toast_msg\":\"\"},\"cross_board_info\":null,\"total_price_result\":{\"total_amount\":1000,\"total_origin_amount\":1000,\"total_full_reduce_amount\":0,\"total_coupon_amount\":0,\"total_freight_amount\":0,\"total_packing_charge_amount\":0,\"total_freight_after_deduction_amount\":0,\"total_tax_amount\":0,\"total_redpack_amount\":0,\"total_deduction_amount_map\":null,\"tip\":\"\",\"total_shop_discount_detail\":{\"total_shop_full_discount_amount\":0,\"total_shop_multi_piece_discount_amount\":0},\"marketing_plan_id\":\"7149846268155166720\",\"shop_sku_map\":{\"GceCTPIk\":{\"sku_list\":{\"1736502463777799\":{\"product_id\":3556357046087622442,\"sku_id\":1736502463777799,\"price\":1000,\"total_discount_amount\":0,\"tax_amount\":0,\"shop_discount\":null,\"platform_discount\":null,\"kol_discount\":null}},\"shop_discount_detail\":null,\"shop_freight_fee\":{\"amount\":0,\"packing_charge_amount\":0,\"freight_id\":\"0\"},\"shop_tax_amount\":0,\"shop_freight_discount_detail\":null,\"freight_amount_desc\":\"\"}},\"total_platform_discount_detail\":null,\"total_kol_discount_detail\":null},\"tip\":\"\",\"render_token\":\"1_cbe7a371a3eee1fd_2os3j5W1JvE8batSzBxg7nZ0sbLJF1VpUB7M+TB0IoQeDwp+d1XZJvADhzsRS0q4\",\"render_track_id\":\"202210021735410101351551570835B4D2\",\"ab_test_info\":{\"test_config\":{},\"config_keys\":\"half_render_cashier,new_address_struct,buynow_acme,buynow_address_optimize,address_new_style,highest_achievement_test,contract_phone_render_page_style\"},\"identity_info\":{\"identity_type\":0,\"info_list\":[]},\"over_layer\":null,\"service_guarantee_infos\":[]},\"log_pb\":{\"impr_id\":\"202210021735410101351551570835B4D2\",\"env\":\"prod\"}}"));
    }

    public static String stringToFormatJSON(String strJson) {
        // 计数tab的个数
        int tabNum = 0;
        StringBuffer jsonFormat = new StringBuffer();
        int length = strJson.length();

        for (int i = 0; i < length; i++) {
            char c = strJson.charAt(i);
            if (c == '{') {
                tabNum++;
                jsonFormat.append(c + "\n");
                jsonFormat.append(getSpaceOrTab(tabNum));
            } else if (c == '}') {
                tabNum--;
                jsonFormat.append("\n");
                jsonFormat.append(getSpaceOrTab(tabNum));
                jsonFormat.append(c);
            } else if (c == ',') {
                jsonFormat.append(c + "\n");
                jsonFormat.append(getSpaceOrTab(tabNum));
            } else {
                jsonFormat.append(c);
            }
        }
        return jsonFormat.toString();
    }

    // 是空格还是tab
    private static String getSpaceOrTab(int tabNum) {
        StringBuffer sbTab = new StringBuffer();
        for (int i = 0; i < tabNum; i++) {
            if (isTab) {
                sbTab.append('\t');
            } else {
                sbTab.append("    ");
            }
        }
        return sbTab.toString();
    }


}
