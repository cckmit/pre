package com.xd.pre.modules.px.cotroller;

import com.alibaba.fastjson.JSON;
import com.xd.pre.modules.px.vo.sys.NotifyVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jd")
@Slf4j
public class PayHtmlController {

    @PostMapping("/callbackTemp")
    public String callbackTemp(@RequestBody NotifyVo notifyVo) {
        log.info("订单号{}，接受临时通知，{}", notifyVo.getTrade_no(), JSON.toJSONString(notifyVo));
        return "success";
    }

    @GetMapping("/payHtml")
    public String dispatcher5() throws Exception {
        return "<html lang=\"zh-CN\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>支付页</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <script>\n" +
                "        var param = \"alipay_sdk=alipay-sdk-java-dynamicVersionNo&app_id=2021003139604420&biz_content=%7B%22body%22%3A%22AppStore%E5%85%85%E5%80%BC%E5%8D%A1100%E5%85%83%E7%94%B5%E5%AD%90%E5%8D%A1-AppleID%E5%85%85%E5%80%BCiOS%E5%85%85%E5%80%BC%22%2C%22business_params%22%3A%22%7B%5C%22outTradeRiskInfo%5C%22%3A%5C%22%7B%7D%5C%22%7D%22%2C%22extend_params%22%3A%7B%22specified_seller_name%22%3A%22%22%7D%2C%22out_trade_no%22%3A%22PCP2022100123484331808599439583%22%2C%22product_code%22%3A%22QUICK_MSECURITY_PAY%22%2C%22seller_id%22%3A%222088441642849480%22%2C%22subject%22%3A%22%E6%8A%96%E9%9F%B3%E7%94%B5%E5%95%86-4984498014474526275%22%2C%22time_expire%22%3A%222022-10-02+00%3A18%3A35%22%2C%22total_amount%22%3A%22100.00%22%7D&charset=utf-8&format=json&method=alipay.trade.app.pay&notify_url=https%3A%2F%2Fapi-cpc.snssdk.com%2Fgateway%2Fpayment%2Fcallback%2Falipay%2Fnotify%2Fpay&sign=kMFi7XqAGT68aeo5mXWaGM%2FOM7ldV1LgUSLIR6fb06x7KB9P7zM9CtSvH1HWuDCDxqv2S4CdTf6PMB0RAfEIOSC16MFE5LfjfGx0U42a7TRsVBTO9%2BqJWdmotm7Du44kORvtHutFQ0AP2BoOJQhZQUdFQh1aS%2BPO7n%2BHEI0q4Br%2BKLzWmGvPxaDLz6CVgOgY8EBY3WE3LDJZOGwLSVx1Sm4t8yVrTGM3Vz55F9NWPpnbooJpRpxajui0ngBqwEvicNp0C87NNqSanuIBdeQarUx0aOICAkWm4skG5XuPrt%2Fyzd%2Fm5hKUnjy%2Fg6sohsaJw8rP0KycFSRL%2Fb7sgG%2B9Xg%3D%3D&sign_type=RSA2&timestamp=2022-10-01+23%3A48%3A50&version=1.0\";\n" +
                "            document.addEventListener(\"AlipayJSBridgeReady\", function(){\n" +
                "            AlipayJSBridge.call('tradePay', {\n" +
                "                orderStr: param\n" +
                "            })\n" +
                "        }, false);\n" +
                "    </script>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
    }

}
