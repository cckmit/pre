package com.xd.pre.modules.px.douyin.pay;


import java.net.URLEncoder;

public class PayRiskInfoAndPayInfoUtils {

    private static String buildPayRiskInfo(PayDto dto) {
        String str = String.format("[{\"key\":\"pay_risk_info\",\"value\":\"{\\\"biometric_params\\\":\\\"1\\\",\\\"is_jailbreak\\\":\\\"2\\\",\\\"openudid\\\":\\\"\\\",\\\"order_page_style\\\":0,\\\"checkout_id\\\":3,\\\"ecom_payapi\\\":true,\\\"ip\\\":\\\"%s\\\"}\",\"type\":\"text\",\"enabled\":false,\"description\":\"\"}]",
                dto.getUserIp()
        );
        return "pay_risk_info=" + URLEncoder.encode(str);
    }

    public static String buidPayUrl(PayDto dto) {
        String str = String.format("https://ec.snssdk.com/order/createpay?aid=1128&device_platform=android&device_type=SM-G955N&request_tag_from=h5&app_name=aweme&version_name=17.3.0&app_type=normal&channel=dy_tiny_juyouliang_dy_and24&version_code=170300&os=android&os_version=5.1.1&device_id=%s&iid=%s",
                dto.getDevice_id(), dto.getIid()
        );
        return str;
    }


    private static String buildPayInfo(PayDto dto) {
        String str = String.format("{\"sdk_version\":\"v2\",\"dev_info\":{\"reqIp\":\"%s\",\"os\":\"android\",\"isH5\":false,\"cjSdkVersion\":\"5.9.1\",\"aid\":\"1128\",\"ua\":\"okhttp/3.10.0.1\",\"riskUa\":\"\",\"lang\":\"zh-Hans\",\"deviceId\":\"%s\",\"osVersion\":\"5.1.1\",\"vendor\":\"\",\"model\":\"\",\"netType\":\"\",\"appVersion\":\"17.3.0\",\"appName\":\"aweme\",\"devicePlatform\":\"android\",\"deviceType\":\"SM-G955N\",\"channel\":\"dy_tiny_juyouliang_dy_and24\",\"openudid\":\"\",\"versionCode\":\"170300\",\"ac\":\"wifi\",\"brand\":\"samsung\",\"iid\":\"%s\",\"bioType\":\"1\"},\"bank_card_info\":{},\"credit_pay_info\":{\"installment\":\"1\"},\"zg_ext_param\":\"{\\\"activity_id\\\":\\\"\\\",\\\"credit_pay_param\\\":{\\\"fee_rate_per_day\\\":\\\"\\\",\\\"has_credit_param\\\":false,\\\"has_trade_time\\\":false,\\\"installment_starting_amount\\\":0,\\\"is_credit_activate\\\":false,\\\"remaining_credit\\\":0,\\\"trade_time\\\":0},\\\"decision_id\\\":\\\"659356656346136_1664429268473091\\\",\\\"jr_uid\\\":\\\"\\\",\\\"merchant_info\\\":{\\\"app_id\\\":\\\"\\\",\\\"ext_uid_type\\\":0,\\\"jh_app_id\\\":\\\"8000104428743\\\",\\\"jh_merchant_id\\\":\\\"100000010442\\\",\\\"merchant_id\\\":\\\"\\\",\\\"merchant_name\\\":\\\"上海格物致品网络科技有限公司\\\",\\\"merchant_short_to_customer\\\":\\\"抖音电商商家\\\"},\\\"promotion_ext\\\":\\\"{\\\\\\\"IsZjyFlag\\\\\\\":\\\\\\\"true\\\\\\\",\\\\\\\"ParamOrderId\\\\\\\":\\\\\\\"202209291327453749774114\\\\\\\"}\\\",\\\"promotion_process\\\":{\\\"create_time\\\":%s,\\\"process_id\\\":\\\"bc9e53afacd0681ada345bfc3935ba50ba\\\",\\\"process_info\\\":\\\"\\\"},\\\"qt_c_pay_url\\\":\\\"\\\",\\\"retain_c_pay_url\\\":\\\"\\\"}\",\"voucher_no_list\":[],\"jh_ext_info\":\"{\\\"payapi_cache_id\\\":\\\"\\\"}\"}",
                dto.getUserIp(),
                dto.getDevice_id(),
                dto.getIid(),
                System.currentTimeMillis() / 1000 + ""
        );
        return "pay_info=" + URLEncoder.encode(str);
    }

    public static String buildPayForm(PayDto dto) {
        //app_name=aweme&channel=dy_tiny_juyouliang_dy_and24&device_platform=android&iid=3743163984904813&order_id=4983651837194409539&os=android&device_id=2538093503847412&aid=1128&pay_type=1
        String str = String.format("app_name=aweme&channel=dy_tiny_juyouliang_dy_and24&device_platform=android&iid=%s&os=android&device_id=%s&aid=1128&pay_type=%s&order_id=%s",
                dto.getDevice_id(),
                dto.getIid(),
                dto.getPay_type(),
                dto.getOrderId()
        );
        return str + "&" + buildPayRiskInfo(dto) + "&" + buildPayInfo(dto);
    }

}
