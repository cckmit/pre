package com.xd.pre.testPay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class xxx {
    public static void main(String[] args) {
    String a =     "{\"st\":0,\"msg\":\"\",\"data\":{\"data\":{\"is_queued\":false,\"sdk_info\":{\"anti_fraud_code\":\"1\",\"anti_fraud_msg\":\"为了您的资金安全，请谨防以刷单兼职、先下单后返利、色情服务等套号交易诈骗。请核实确认后，再继续支付\",\"order_info\":\"success\",\"sign\":\"ECP4988064847380395508\",\"sign_type\":\"RSA\",\"url\":\"alipay_sdk=alipay-sdk-java-dynamicVersionNo\\u0026app_id=2021003139604420\\u0026biz_content=%7B%22body%22%3A%22AppStore%E5%85%85%E5%80%BC%E5%8D%A1200%E5%85%83%E7%94%B5%E5%AD%90%E5%8D%A1-AppleID%E5%85%85%E5%80%BCiOS%E5%85%85%E5%80%BC%22%2C%22business_params%22%3A%22%7B%5C%22outTradeRiskInfo%5C%22%3A%5C%22%7B%7D%5C%22%7D%22%2C%22extend_params%22%3A%7B%22specified_seller_name%22%3A%22%22%7D%2C%22out_trade_no%22%3A%22PCP2022101114415102068526435409%22%2C%22product_code%22%3A%22QUICK_MSECURITY_PAY%22%2C%22seller_id%22%3A%222088441642849480%22%2C%22subject%22%3A%22%E6%8A%96%E9%9F%B3%E7%94%B5%E5%95%86-%E5%A4%B4%E6%9D%A14988064847380264436%22%2C%22time_expire%22%3A%222022-10-11+14%3A58%3A01%22%2C%22total_amount%22%3A%22200.00%22%7D\\u0026charset=utf-8\\u0026format=json\\u0026method=alipay.trade.app.pay\\u0026notify_url=https%3A%2F%2Fapi-cpc.snssdk.com%2Fgateway%2Fpayment%2Fcallback%2Falipay%2Fnotify%2Fpay\\u0026sign=KGl%2FNYlOllabQVBI34gi1HOU3wjm%2Bv5iKLGNl0j2fcUZvMDe9pwKQANbfkYcxfcFbpVFbR5LfG0GlZbsFude3Dj56lGI1eiFh7IWKClycdUaADp5L0ppTU0l5sB9lTH5RHYEJAyWVAgwVS8%2FUrl5K33yV1NkF04utKTrnNk8hvwlkO8SZUUbWEkFg3Se4p1jhm9X9IZPXTe0D7V28Usp%2BES%2ByJDoSYHs6hGL9XLlqdv8dckDs%2FAgtnb2qkxIF%2F8%2Beqwz%2F1%2FGoz32kwu93QQBOwrEjtf3LzGOGzhDL0kUWTChDUxjGNRbtodnxmVI4%2F0LvFQsOHzv6W7CKxz%2FTIAJvQ%3D%3D\\u0026sign_type=RSA2\\u0026timestamp=2022-10-11+14%3A41%3A55\\u0026version=1.0\"},\"trade_info\":{\"tt_sign\":\"sMXKsfgGbYamMHcZtTrzRnWsytb/cgq9z43VIiXmz+FZTWqES3CMQ1hnAs2vtxBqipC0x5guQhQcnVBbjGVwACP9lk6rcv6Vu4x1TgXe9uAWtsKVp6FkG+CSGuTeNvGk4Wrxk3Uxn+tIzXImf/Szs29AGBAe/ZO/RjxvAfjofcA=\",\"tt_sign_type\":\"RSA\",\"way\":\"2\"}},\"message\":\"success\"},\"log_pb\":{\"impr_id\":\"2022101114415501014104514904DBE948\",\"env\":\"prod\"}}";
        JSONObject parseObject = JSON.parseObject(a);
        System.out.println(JSON.toJSONString(parseObject));
    }
}
