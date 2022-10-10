package com.xd.pre.modules.px.psscan;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.concurrent.TimeUnit;

@Data
@Slf4j
public class PcPayQr {
    private String userIp;
    private String appck;
    private String orderId;
    private String proxyIp;
    private Integer ProxyPort;
    private String sign;

    public static void main(String[] args) throws Exception {
        /*JSONObject parseObject = JSON.parseObject("{\n" +
                "            \"package\": \"Sign=WXPay\",\n" +
                "            \"packageid\": \"wx20161941621340b45019177cc1a55a0000\",\n" +
                "            \"package_id\": \"prepay_id=wx20161941621340b45019177cc1a55a0000\",\n" +
                "            \"mch_id\": \"1607666155\",\n" +
                "            \"noncestr\": \"0v6DgbhaHIVnXVSF4MK8jKaiZA1t2o\",\n" +
                "            \"out_trade_no\": \"220920161941418634153648\",\n" +
                "            \"paySign\": \"7045139A45C0C6B235DD5D88B178BC0F\",\n" +
                "            \"appid\": \"wxbd2e9fdce15eeca5\",\n" +
                "            \"partnerid\": \"1607666155\",\n" +
                "            \"prepayid\": \"wx20161941621340b45019177cc1a55a0000\",\n" +
                "            \"timestamp\": \"1663661981\"\n" +
                "        }");

        String format = String.format("weixin://app/%s/pay/?timeStamp=%s&sign=%s&signType=MD5&partnerId=1607666155&prepayId=%s&nonceStr=%s",
                parseObject.getString("appid"), parseObject.getString("timestamp"), parseObject.getString("paySign")
                , parseObject.getString("prepayid"), parseObject.getString("noncestr")+"&package=Sign%3dWXPay"
        );
        System.out.println(format);*/


        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        OkHttpClient client = builder.connectTimeout(3, TimeUnit.SECONDS).readTimeout(3, TimeUnit.SECONDS).followRedirects(false).build();
        Request.Builder header = new Request.Builder().url("https://pcashier.jd.com/weixin/getWeixinImage.action?cashierId=1&data=7aab9b185ac35d5ce256b83e5e49a63c454494b49b8c5942d3b8aa1a5ed9b723727cc4808398f3ebc35a94682f40b25d&flag=1&appId=wenlv")
                .addHeader("Cookie", "thor=" + "D78A94FE6F1A4E6A465F9B5476785E35187AF72ACAD6D04B1F4D4D96764344103AA756A86F22795F731F893021C782AAE97024B91012EA530C53ADE77DFEEDB826AF7CCC9D0FD20E052001DE68BD2DEF6C219CF621E7A2BDF2E0F5A16D7F0DEE26E8B72A7FFDDD03BDAAC453CB6722B04F4C11E8A25EDACE726529A700DD0ED8B30D88C5D0C60248F29AECDFFA46FF930E4968A66F062A0365A93A0C7938B79A")
                .get();
        Response response = client.newCall(header.build()).execute();
        /*InputStream inputStream = response.body().byteStream();
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
                System.out.println(reader.decode(bitmap, hints).getText());
            } catch (Exception e) {
                e.printStackTrace();
            }*/
    }
}
