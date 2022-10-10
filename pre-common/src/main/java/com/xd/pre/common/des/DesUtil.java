package com.xd.pre.common.des;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xd.pre.common.constant.PreConstant;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.Security;
import java.util.List;

@Slf4j
public class DesUtil {

    private final static String ALGORITHM_DES = "DES/ECB/PKCS7Padding";

    //设置java支持PKCS7Padding
    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    //获取加密或解密的Cipher对象：负责完成加密或解密工作
    private static Cipher GetCipher(int opmode, String key) {
        try {
            //根据传入的秘钥内容生成符合DES加密解密格式的秘钥内容
            DESKeySpec dks = new DESKeySpec(key.getBytes());
            //获取DES秘钥生成器对象
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            // 生成秘钥：key的长度不能够小于8位字节
            Key secretKey = keyFactory.generateSecret(dks);
            //获取DES/ECB/PKCS7Padding该种级别的加解密对象
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
            //初始化加解密对象【opmode:确定是加密还是解密模式；secretKey是加密解密所用秘钥】
            cipher.init(opmode, secretKey);
            return cipher;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * DES算法，加密
     *
     * @param data 待加密字符串
     * @param key  加密私钥，长度不能够小于8位
     * @return 加密后的字节数组，一般结合Base64编码使用
     * @throws Exception
     */
    private static String encode(String data, String key) {
        if (data == null || data.isEmpty())
            return null;
        try {
            //获取加密对象【Cipher.ENCRYPT_MODE：指定加密模式为1】
            Cipher cipher = GetCipher(Cipher.ENCRYPT_MODE, key);
            if (cipher == null) {
                return null;
            } else {
                //设置加密的字符串为utf-8模式并且加密，返回加密后的byte数组。
                byte[] byteHex = cipher.doFinal(data.getBytes("UTF-8"));
                return byteToHexString(byteHex);//对加密后的数组进制转换
            }
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }

    /**
     * DES算法，解密
     *
     * @param data 待解密字符串
     * @param key  解密私钥，长度不能够小于8位
     * @return 解密后的字节数组
     * @throws Exception
     */
    public static String decode(String data, String key) throws Exception {
        if (data == null || data.isEmpty()) {
            return null;
        }
        try {
            //先把待解密的字符串转成Char数组类型，然后进行进制转换。
            byte[] b = Base64.decode(data);
            //获取解密对象【Cipher.DECRYPT_MODE：指定解密模式为2】
            Cipher cipher = GetCipher(Cipher.DECRYPT_MODE, key);
            if (cipher != null) {
                //进行解密返回utf-8类型的字符串
                return new String(cipher.doFinal(b), StandardCharsets.UTF_8);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }

    private static String byteToHexString(byte[] bytes) {
        StringBuffer sb = new StringBuffer(bytes.length);
        String sTemp;
        for (int i = 0; i < bytes.length; i++) {
            sTemp = Integer.toHexString(0xFF & bytes[i]);
            if (sTemp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    private static String encode(byte[] src) {
        String strHex = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < src.length; n++) {
            strHex = Integer.toHexString(src[n] & 0xFF);
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex); // 每个字节由两个字符表示，位数不够，高位补0
        }
        return sb.toString().trim();
    }

    private static byte[] hex2byte(String hex) throws IllegalArgumentException {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("invalid hex string");
        }
        char[] arr = hex.toCharArray();
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
            String swap = "" + arr[i++] + arr[i];
            int byteint = Integer.parseInt(swap, 16) & 0xFF;
            b[j] = new Integer(byteint).byteValue();
        }
        return b;
    }

    public static GaDataDto DecodeDes(String body) {
        try {
            JSONObject parseObject = JSON.parseObject(body);
            String result = parseObject.getString("result");
            GaDataDto gaDataDto = JSON.parseObject(result, GaDataDto.class);
            if (gaDataDto.getOrderStatus() == PreConstant.EIGHT) {
                log.info("当前订单完成,解析订单的卡密");
                String cardInfos = gaDataDto.getCardInfos();
                String decode = decode(cardInfos, "2E1ZMAF88CCE5EBE551FR3E9AA6FF322");
                if (StrUtil.isNotBlank(decode)) {
                    List<CardNoDto> cardNoDtos = JSON.parseArray(decode, CardNoDto.class);
                    gaDataDto.setCardNoDtos(cardNoDtos);
                }
            }
            return gaDataDto;
        } catch (Exception e) {
            log.error("当前解析订单报错msg:{}", e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        String a = "{\n" +
                "\t\"message\": \"响应成功\",\n" +
                "\t\"result\": {\n" +
                "\t\t\"orderId\": 247578289990,\n" +
                "\t\t\"skuId\": 10022039398507,\n" +
                "\t\t\"logo\": \"https://img10.360buyimg.com/N4/jfs/t1/149927/28/11354/104823/5f8cf6d3Ea6d46516/44061311cb8b9618.jpg\",\n" +
                "\t\t\"title\": \"App Store 充值卡 10元（电子卡）Apple ID 充值\",\n" +
                "\t\t\"jdPrice\": 1000.0,\n" +
                "\t\t\"buyNum\": 1,\n" +
                "\t\t\"orderStatus\": 8,\n" +
                "\t\t\"orderStatusStr\": \"交易完成\",\n" +
                "\t\t\"totalPrice\": 1000.0,\n" +
                "\t\t\"couponPay\": 0.0,\n" +
                "\t\t\"payMode\": 0,\n" +
                "\t\t\"jBeanPay\": 0,\n" +
                "\t\t\"onlinePay\": 1000.0,\n" +
                "\t\t\"chargeType\": 1,\n" +
                "\t\t\"cardInfos\": \"BsBfLCzOTlBGoq5gIBRaujJdEwKB65/cgkWBJDz8QlwJIyRuRlJdmTqD2SwziHIJP4oFp6omMlCn8f6ysH5RnFxQ2VF287irFgf1hfDcjLBlqx3jlqZ+ppGA/LSZ8fD89OtWnpDhel352bQn77KOQk2ysxtQjCRPnKbB28C+iFYMVBKTH+CN8VeUMylDQ6Fe\",\n" +
                "\t\t\"created\": \"2022-05-23 17:33:37\",\n" +
                "\t\t\"payBackUrl\": \"https://newcz.m.jd.com/payback.html\",\n" +
                "\t\t\"venderId\": 634345,\n" +
                "\t\t\"qualificationFileUrl\": \"https://newcz.m.jd.com/vender.html?venderId=634345\",\n" +
                "\t\t\"totalPriceStr\": \"10.00\",\n" +
                "\t\t\"onlinePayStr\": \"10.00\",\n" +
                "\t\t\"orderStatusName\": \"交易完成\",\n" +
                "\t\t\"payTypeShow\": \"在线支付\",\n" +
                "\t\t\"cancelFlag\": false,\n" +
                "\t\t\"showInfo\": [\n" +
                "\t\t\t[\n" +
                "\t\t\t\t\"商品编号\",\n" +
                "\t\t\t\t10022039398507\n" +
                "\t\t\t],\n" +
                "\t\t\t[\n" +
                "\t\t\t\t\"商品名称\",\n" +
                "\t\t\t\t\"App Store 充值卡 10元（电子卡）Apple ID 充值\"\n" +
                "\t\t\t],\n" +
                "\t\t\t[\n" +
                "\t\t\t\t\"购买数量\",\n" +
                "\t\t\t\t1\n" +
                "\t\t\t]\n" +
                "\t\t]\n" +
                "\t},\n" +
                "\t\"code\": \"0\"\n" +
                "}";
        //            String str = "BsBfLCzOTlCJYGcU0bI51kIn9XCiRIhAwyahZsJXgMkJIyRuRlJdmfvt+BtEOIE0pjLYgEOvakdp0TPt2wOaP1xQ2VF287irFgf1hfDcjLBlqx3jlqZ+ppGA/LSZ8fD89OtWnpDhel352bQn77KOQk2ysxtQjCRPuQgxYlNIl+KhfpRLpTgwW1eUMylDQ6Fe";
        //            System.out.println(decode(str, "2E1ZMAF88CCE5EBE551FR3E9AA6FF322"));
        GaDataDto gaDataDto = DecodeDes(a);
        System.out.println(JSON.toJSONString(gaDataDto));
    }

}
