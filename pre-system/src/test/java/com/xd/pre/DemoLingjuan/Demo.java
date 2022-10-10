package com.xd.pre.DemoLingjuan;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xd.pre.common.sign.JdSgin;
import com.xd.pre.common.utils.px.dto.SignVoAndDto;
import com.xd.pre.modules.px.cotroller.PaySign;
import com.xd.pre.modules.sys.domain.JdProxyIpPort;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

@Slf4j
public class Demo {

    public static void main(String[] args) throws Exception {
        FileReader fileReader = new FileReader("C:\\Users\\Administrator\\Desktop\\AMG.txt");
        List<String> strings = fileReader.readLines();
        for (String ck : strings) {
            HttpRequest httpRequest = HttpRequest.get("http://webapi.http.zhimacangku.com/getip?num=1&type=1&pro=&city=0&yys=0&port=1&time=1&ts=0&ys=0&cs=0&lb=1&sb=0&pb=4&mr=2&regions=");
            HttpResponse execute = httpRequest.execute();
            String body = execute.body();
            String[] split = body.trim().split(":");
            Thread.sleep(2000L);
            JdProxyIpPort oneIp = JdProxyIpPort.builder().ip(split[0]).port(split[1]).build();
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
            ck = "pin=jd_7ee93ffcf66197;wskey=AAJibywIAED4SCF4HuX3xEDSTEc0xwd2bmiy0MbSghwOl-Nl5RHg5ukAVBIG4Uepsscj9NziNOgQhGIkfxSe44fCc7Tz0gRl;";
            String token = getToken(ck, proxy);
            if (StrUtil.isBlank(token)) {
                continue;
            }
            String mck = getMck(proxy, token);
            String orderId = getOrderId(ck, proxy);
            if (StrUtil.isBlank(orderId)) {
                continue;
            }
            String payId = getPayId(ck, orderId, proxy);
            if (StrUtil.isBlank(payId)) {
                continue;
            }
            System.out.println("payId:" + payId);
            check(payId, mck, proxy);
            String payUrl = payUrl(payId, mck, proxy);
            System.out.println("-------------------------");
        }
    }

    private static String getMck(Proxy proxy, String token) throws IOException {
        String toenUrl = String.format(" https://un.m.jd.com/cgi-bin/app/appjmp?tokenKey=%s&to=https://gamerecg.m.jd.com?skuId=%s", token, "11183343342");
        OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy).followRedirects(false).build();
        Request request = new Request.Builder()
                .url(toenUrl)
                .addHeader("User-Agent", "okhttp/3.12.1")
                .build();
        Response execute1 = client.newCall(request).execute();
        List<String> headers = execute1.headers("Set-Cookie");
        StringBuilder stringBuilder = new StringBuilder();
        for (String header : headers) {
            if (StrUtil.isNotBlank(header) && (header.contains("pt_pin") || header.contains("pt_key"))) {
                String[] split = header.split(";");
                for (String s : split) {
                    if (s.contains("pt_pin") || s.contains("pt_key")) {
                        stringBuilder.append(s + ";");
                    }
                }
            }
        }
        return stringBuilder.toString();
    }


    private static String getToken(String ck, Proxy proxy) {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy).build();
            RequestBody requestBody = new FormBody.Builder()
                    .add("body", "{\"action\":\"to\",\"to\":\"https%3A%2F%2Fcard.m.jd.com%2F\"}")
                    .build();
            SignVoAndDto signVoAndDto = new SignVoAndDto("genToken", "{\"action\":\"to\",\"to\":\"https%3A%2F%2Fcard.m.jd.com%2F\"}");
            signVoAndDto = JdSgin.newSign(signVoAndDto);

            Request request = new Request.Builder()
                    .url(String.format("http://api.m.jd.com/client.action?functionId=genToken&clientVersion=9.4.4&client=android&uuid=%s&st=%s&sign=%s&sv=%s", signVoAndDto.getUuid(), signVoAndDto.getSt(),
                            signVoAndDto.getSign(), signVoAndDto.getSv()))
                    .post(requestBody)
                    .addHeader("Cookie", ck)
                    .addHeader("User-Agent", "okhttp/3.12.1")
                    .build();
            Response response = client.newCall(request).execute();
            JSONObject parseObject = JSON.parseObject(response.body().string());
            String tokenKey = parseObject.getString("tokenKey");
            if (tokenKey.length() > 60) {
                return tokenKey;
            } else if (tokenKey.length() == 3) {
                HttpRequest httpRequest = HttpRequest.get("http://webapi.http.zhimacangku.com/getip?num=1&type=1&pro=&city=0&yys=0&port=1&time=1&ts=0&ys=0&cs=0&lb=1&sb=0&pb=4&mr=2&regions=");
                HttpResponse execute = httpRequest.execute();
                String body = execute.body();
                String[] split = body.trim().split(":");
                Thread.sleep(1000L);
                JdProxyIpPort oneIp = JdProxyIpPort.builder().ip(split[0]).port(split[1]).build();
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
                return getToken(ck, proxy);
            }
            log.error("过期了");
        } catch (Exception e) {

        }
        return null;
    }

    private static String payUrl(String payId, String mck, Proxy proxy) throws Exception {
        try {
            String bodyData = String.format("{\"source\":\"mcashier\",\"origin\":\"h5\",\"page\":\"pay\",\"mcashierTraceId\":1653762486838,\"appId\":\"jd_m_pay\",\"payId\":\"%s\",\"eid\":\"2XRK4PH7YTECS7DYZNDH764SWHELI2J2COCDRU357GLIV6TKL63PRAESJZVNTNB53M6BZABAON74E2QQEOCZO745CY\"}", payId);
            RequestBody requestBody = new FormBody.Builder()
                    .add("body", bodyData)
                    .build();
            OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy).build();
            Request request = new Request.Builder()
                    .url("https://api.m.jd.com/client.action?functionId=platWapWXPay&appid=mcashier")
                    .post(requestBody)
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .addHeader("cookie", mck)
                    .addHeader("User-Agent", "jdapp;android;9.4.4;10;7316266326835303-1663034366462326;network/wifi;model/PACT00;addressid/0;aid/7ab6b850a604fd2b;oaid/;osVer/29;appBuild/87076;psn/m 0Ddoh86M2Rp emACf77VJZ2BYiaC7o|91;psq/1;adk/;ads/;pap/JA2015_311210|9.4.4|ANDROID 10;osv/10;pv/34.23;installationId/5315bb3ac03f4341bde696c7fb2aaf28;jdv/0|kong|t_1000440933_|jingfen|efde4563d64c4e18ba131fd2e011f050|1653590049;ref/com.jd.lib.ordercenter.mygoodsorderlist.view.activity.MyOrderListActivity;partner/lc031;apprpd/OrderCenter_List;eufv/1;jdSupportDarkMode/0;hasUPPay/1;hasOCPay/0;supportHuaweiPay/0;supportBestPay/0;Mozilla/5.0 (Linux; Android 10; PACT00 Build/QP1A.190711.020; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/89.0.4389.72 MQQBrowser/6.2 TBS/046010 Mobile Safari/537.36")
                    .addHeader("Origin", "https://pay.m.jd.com")
                    .addHeader("Referer", "https://pay.m.jd.com/cpay/newPay-index.html?appId=jd_android_app4&needLoginSwitch=1&payId=5dbe72b4a9e643099019616912583543&sid=0fe98f15efa19a8d714fe2330bb7de7w&un_area=22_1930_0_0")
                    .build();
            Response response = client.newCall(request).execute();
            String returnStr = response.body().string();
            log.info("response:{}", returnStr);
            return returnStr;
        } catch (Exception e) {

        }
        return null;
    }

    private static void check(String payId, String mck, Proxy proxy) throws Exception {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy).build();
            RequestBody requestBody = new FormBody.Builder()
                    .add("lastPage", "https://wqs.jd.com/")
                    .add("appId", "jd_android_app4")
                    .add("payId", payId)
                    .build();
            Request request = new Request.Builder()
                    .url("https://pay.m.jd.com/newpay/index.action")
                    .post(requestBody)
                    .addHeader("cookie", mck)
                    .addHeader("User-Agent", "jdapp;android;9.4.4;10;7316266326835303-1663034366462326;network/wifi;model/PACT00;addressid/0;aid/7ab6b850a604fd2b;oaid/;osVer/29;appBuild/87076;psn/m 0Ddoh86M2Rp emACf77VJZ2BYiaC7o|91;psq/1;adk/;ads/;pap/JA2015_311210|9.4.4|ANDROID 10;osv/10;pv/34.23;installationId/5315bb3ac03f4341bde696c7fb2aaf28;jdv/0|kong|t_1000440933_|jingfen|efde4563d64c4e18ba131fd2e011f050|1653590049;ref/com.jd.lib.ordercenter.mygoodsorderlist.view.activity.MyOrderListActivity;partner/lc031;apprpd/OrderCenter_List;eufv/1;jdSupportDarkMode/0;hasUPPay/1;hasOCPay/0;supportHuaweiPay/0;supportBestPay/0;Mozilla/5.0 (Linux; Android 10; PACT00 Build/QP1A.190711.020; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/89.0.4389.72 MQQBrowser/6.2 TBS/046010 Mobile Safari/537.36")
                    .addHeader("Origin", "https://pay.m.jd.com")
                    .addHeader("Referer", "https://pay.m.jd.com/cpay/newPay-index.html?appId=jd_android_app4&needLoginSwitch=1&payId=5dbe72b4a9e643099019616912583543&sid=0fe98f15efa19a8d714fe2330bb7de7w&un_area=22_1930_0_0")
                    .build();
            Response response = client.newCall(request).execute();
            String string = response.body().string();
            log.debug("s:{}", string);
        } catch (Exception e) {

        }
    }

    private static String getPayId(String ck, String orderId, Proxy proxy) throws IOException {
        try {
            String bodyData = String.format("{\"appId\":\"jd_android_app4\",\"fk_aid\":\"7ab6b850a604fd2b\",\"fk_appId\":\"com.jingdong.app.mall\",\"fk_terminalType\":\"02\",\"fk_traceIp\":\"192.168.2.247\",\"orderId\":\"%s\",\"orderType\":\"34\",\"orderTypeCode\":\"0\",\"paySourceId\":\"2\",\"payablePrice\":\"100.00\",\"paysign\":\"%s\"}", orderId, PaySign.getPaySign(orderId,"100.00"));
            SignVoAndDto signVoAndDto = new SignVoAndDto("genAppPayId", bodyData);
            signVoAndDto = JdSgin.newSign(signVoAndDto);
            String url = String.format("https://api.m.jd.com/client.action?functionId=genAppPayId&clientVersion=9.4.4&client=android&uuid=%s&st=%s&sign=%s&sv=120", signVoAndDto.getUuid(), signVoAndDto.getSt(), signVoAndDto.getSign());
            OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy).build();
            RequestBody requestBody = new FormBody.Builder()
                    .add("body", bodyData)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("Cookie", ck)
                    .addHeader("Charset", "UTF-8")
                    .addHeader("user-agent", "okhttp/3.12.1;jdmall;android;version/11.0.2;build/97565;")
                    .build();
            Response response = client.newCall(request).execute();
            String payIdStr = response.body().string();
            String payId = JSON.parseObject(payIdStr).getString("payId");
            return payId;
        } catch (Exception e) {

        }
        return null;

    }

    private static String getOrderId(String ck, Proxy proxy) throws IOException {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy).build();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "body=%7B%22appKey%22%3A%22android%22%2C%22brandId%22%3A%22999440%22%2C%22buyNum%22%3A1%2C%22payMode%22%3A%220%22%2C%22rechargeversion%22%3A%2210.9%22%2C%22skuId%22%3A%2211183343342%22%2C%22totalPrice%22%3A%2210000%22%2C%22type%22%3A1%2C%22version%22%3A%221.10%22%7D&undefined=");
            Request request = new Request.Builder()
                    .url("https://api.m.jd.com/client.action?functionId=submitGPOrder&clientVersion=9.4.4&client=android&uuid=adfde798149c4be6&st=1653923965732&sign=bb9bd40e7426ce4c73e9467211406438&sv=120")
                    .post(body)
                    .addHeader("Cookie", ck)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .addHeader("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 10; PACT00 Build/QP1A.190711.020)")
                    .addHeader("Host", "api.m.jd.com")
                    .addHeader("cache-control", "no-cache")
                    .build();
            Response response = client.newCall(request).execute();
            String orderStr = response.body().string();
            if (orderStr.contains("orderId")) {
                String orderId = JSON.parseObject(JSON.parseObject(orderStr).getString("result")).getString("orderId") + "";
                return orderId;
            }

        } catch (Exception e) {

        }
        return null;
    }
}
