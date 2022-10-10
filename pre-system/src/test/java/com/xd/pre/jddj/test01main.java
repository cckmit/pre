package com.xd.pre.jddj;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URLEncoder;
import java.util.Base64;

@Slf4j
public class test01main {

    public static final String salt = "J@NcRfUjXn2r5u8x";
    public static final String viStr = "t7w!z%C*F-JaNdRg";
    public static final String H_MAC_HASH_KEY = "923047ae3f8d11d8b19aeb9f3d1bc200";


    public static void main(String[] args) throws Exception {
//        //下单
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("113.231.18.245", 4252));
////        Proxy proxy =null;
//        TimeInterval timer = DateUtil.timer();
//        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
//        if (ObjectUtil.isNotNull(proxy)) {
//            log.debug("代理设置:{}", proxy.toString());
//            builder.proxy(proxy);
//        }
//        OkHttpClient client = builder.followRedirects(false).build();
//        String mck = "pt_key=app_openAAJipIBMADABAXXoi16zbq9jgUhGrL7viOeIV4u15bkWwJ1XJPPShoG1Q5R9WARFZU6dh-bIl74; pt_pin=jd_AJfWANv;";
//        Integer amount = 1000 * 100;
//        JdDjCookie jdDjCookie = JdDjCookie.jdDjCookieBuild(mck, client);
//        System.out.println("JdDjCookie:" + timer.interval());
//        SubmitCombine submitCombine = SubmitCombine.getDjencrypt(jdDjCookie, amount);
//        submitCombine = SubmitCombine.submitOrderRequst(submitCombine, client);
//        System.out.println("submitOrderRequst:" + timer.interval());
//        PayTokenPrefixCombine payTokenPrefixCombine = PayTokenPrefixCombine.getDjencrypt(jdDjCookie, submitCombine.getOrderId());
//        payTokenPrefixCombine = PayTokenPrefixCombine.payTokenPrefixCombineRequst(payTokenPrefixCombine, client);
//        System.out.println("PayTokenPrefixCombine:" + timer.interval());
//        InitCashierCombine initCashierCombine = InitCashierCombine.getDjencrypt(payTokenPrefixCombine);
//        initCashierCombine = InitCashierCombine.InitCashierCombineRequst(initCashierCombine, client);
//        System.out.println("InitCashierCombine:" + timer.interval());
//        PayCombine payCombine = PayCombine.getDjencrypt(payTokenPrefixCombine);
//        payCombine = PayCombine.PayCombineRequst(payCombine, client);
//        log.info("支付链接为msg:{}", payCombine.getPayData());
//        System.out.println("PayCombine:" + timer.interval());
//        CancelCombine cancelCombine = CancelCombine.getDjencrypt(payCombine);
//        cancelCombine = CancelCombine.cancelCombineRequst(cancelCombine, client);
//        System.out.println("CancelCombine:" + timer.interval());
//        OrderDeleteCombine deleteCombine = OrderDeleteCombine.getDjencrypt(payCombine);
//        deleteCombine = OrderDeleteCombine.orderDeleteCombineRequst(deleteCombine, client);
//        System.out.println("OrderDeleteCombine:" + timer.interval());
//        //下单完成
        String sSrc = "IJpu5U8gjaurTVnrDv9+UWRjNc+kmtrUCUjEIkxiEJ4nd0ifKufXOwqJqHNB+721YtnVHx4SMN2krqTdf2fTbQgdobjbYYIKyg/PaMcqs9xck+kQiolI57yUcJMktfnQfeLdNrx1Jg/XB47uCsfXoVTtPcWgt6GyTcaxX9TP+meP7WwYAOoYGFjd1uoAssx9CPWb485c/oIvmJL7SANftDL6auytyjm1EUxeErMq+CxrzI8iycthf/M+3/odqqwuw9QvMJX8OR+a2+rC2839wPX2+cIEc3h+ESvQnhC4GjemfYmSGtDtLYCYZ+TZx6cxoejo8q+CQHGIKrmYHYrKpL2FJgCiOfUHFN4iwHTzfl4ni3UZhQM0gs6MmXG+PyyIJ2BoGtJjCKFqENnpMQpMy9sNmJBmfIbK0WWYYEqvd9p/YYjBs8gLc3BKXR2/ob3Bj61oJ4noFqfcjMF2GCthtmhGLxnIscpkr4ELKxioNMWsShsWuWxKQ7vZ2/UzW8EqIqYGygIi3fyGEHVwA7colDKo0OTM6OCPgFzCAdZ6B/VTrvXScNZMlTAwS8wBgllNq3c1sdCvma2UZo92E8i1FhWr7r6FXrcizfmnCIlWaKo3UhafX5lDZaJAGroAIOUl3H03x3OuXikncvAfl8CmVLRhcy12GX/JOzEu+K85Ml4yK3bkyAS/CRUGq+5aso+l001Z0n1OWQoXakd8S73S/iDCajKkT2lraCYaNqOm2AJc/mivOQXLBDLSUKfntLcoVeqks7+JQ+g6sHJXpK7FBcL+kP/DdyI6uZ5rUWHh3etvTeRB4Mb2ggPK2eJP5IjMfVNEzm+JeIW4X54H7VA1ytNMS/lTdDMIAqRDHG/3tK27O+e2r9/FqyrW+5WRduhscYpByXAKMwl7IpvDUkHLg/ymcp0grULvJwdi8RQxFH33Q5IrA9tDrWEp3IZphDp+fQB8xBmYCAdroqJbWnDqTO0TOTv9Nqlt4IYp9awNOPO4mM1soA7ZJMCvdnZvEygQcdpBWv0A9sX8m/9/97uArDqODdwMehZ7BOzcelw3oF2NVV5ftWLXTUuuI6zjqluQaW5fVJAGJGHOdEJa0udt6QgSZ0Mmmb72HskIc/WWqVVHMHoEAsfwMGb7Ex+vflhRaOUJ1hzyA7yQ8MuanF4GQhrhqXjOMyo8MmY/9jtA45NS1RGz5aEZFSmHI770PIqX";
        String sKey = "J@NcRfUjXn2r5u8x";
        String decrypt = Decrypt(sSrc, sKey);

        System.err.println("解密:" + decrypt);
        String body = JSON.parseObject(decrypt).getString("body");
        System.out.println(body);

        String encrypt = Encrypt("{\"platCode\":\"H5\",\"appName\":\"paidaojia\",\"channel\":\"\",\"appVersion\":\"8.22.5\",\"body\":\"{\\\"token\\\":\\\"jWRiXjn+lwMbAXP8tWFQRWVcoUIK5Fy4OchoZCzQETRliU8k7bKBX1mppnAAVK3p66FgxTrKJ9gLt2wURY6DpeAMrXlEG4hd+dR8ckzK5NkQ5jua/X7J920JaLrCTkewpevQIh9Egfo/8LMFuM4hnpUi3uOL+ektntLjRUO8BLI=\\\",\\\"orderId\\\":2219755377000283,\\\"payMode\\\":10,\\\"payModeType\\\":0,\\\"pageSource\\\":\\\"pay\\\",\\\"ctp\\\":\\\"pay\\\",\\\"refPar\\\":\\\"\\\"}\",\"pageId\":\"e83d761ea8325289e9bcf44cf202b273\",\"lng\":104.02688,\"lat\":30.498213,\"city_id\":1930,\"poi\":\"格林城\",\"jda\":\"122270672.1660663421259212305761.1660663421.1660663421.1660663421.1\",\"traceId\":\"H5_DEV_29D50D27-63DF-4D2A-A29B-E5321B6209561660721812427\",\"globalPlat\":\"2\",\"deviceId\":\"H5_DEV_29D50D27-63DF-4D2A-A29B-E5321B620956\",\"signNeedBody\":1,\"_jdrandom\":1660721812427,\"signKeyV1\":\"b6dc447f3076d2d4fc5fb27f5e1aad65088672e93f1dfd05734faf3d887ef6dd\"}");
        System.out.println(encrypt);

    }


    /**
     * 解密算法
     *
     * @param sSrc
     * @param sKey
     * @return
     * @throws Exception
     */
    public static String Decrypt(String sSrc, String sKey) throws Exception {
        try {
            // 判断Key是否正确
            if (sKey == null) {
                System.out.print("Key为空null");
                return null;
            }
            // 判断Key是否为16位
            if (sKey.length() != 16) {
                System.out.print("Key长度不是16位");
                return null;
            }
            byte[] raw = sKey.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec("t7w!z%C*F-JaNdRg".getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = Base64.getDecoder().decode(sSrc);//先用base64解密
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original);
                return originalString;
            } catch (Exception e) {
                System.out.println(e.toString());
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return null;
        }
    }
    //AES加解密算法
    public static String Encrypt(String sSrc) throws Exception {
        byte[] raw = salt.getBytes("utf-8");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//"算法/模式/补码方式"
        IvParameterSpec iv = new IvParameterSpec(viStr.getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes());
        return URLEncoder.encode(new BASE64Encoder().encode(encrypted));//此处使用BASE64做转码功能，同时能起到2次加密的作用。
    }

}
