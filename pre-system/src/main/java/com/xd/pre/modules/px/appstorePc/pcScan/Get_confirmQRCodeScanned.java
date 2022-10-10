package com.xd.pre.modules.px.appstorePc.pcScan;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Get_confirmQRCodeScanned {

    public static List<Integer> get_confirmQRCodeScanned(String Uuid, String QRCodeKey, String Devices_, Boolean isLogin, String wskey, String pin) {
        Integer index = 0;
        while (true) {
            List<Integer> a = new ArrayList<>();
     /*       String Uuid = "-1b78895a1ee7";
            String QRCodeKey = "AAEAIFcDN2kjBXCJuaqtHoKpD24dHgmp9ZmMSIPdRaA0WG9w";
            String Devices_ = "D41D8CD98F00B204E9800998ECF8427E";*/
            String ClientType = "android";
            String appname = "jdapp";
            String Area = "107.489523_27.764753";
            // Area ＝ “107.48” ＋ 文本_取随机数字 (2) ＋ “23_27.76” ＋ 文本_取随机数字 (2) ＋ “53
            for (int i = 0; i < index; i++) {
                Area = Area + "0";
            }
            String DwAppClientVer = "9.1.0";
            //TODO 这地方要随机获取wifi方法.版本 2  network ＝ 多项选择 (取随机数 (1, 4), “wifi”, “4G”, “3G”, “2G”)
            String network = "4G";
            Integer DwAppID = 100;
            String Build = "83789";
            Integer dwGetSig = 1;
            String Partner = "ks006";
            String dwversion = "6.1.0";
            String OsVer = "";
            String Screen = "*";
            if (!isLogin) {
                Zubao.init_QR(a, 3);
            } else {
                Zubao.init_QR(a, 4);
            }
            //第二部

            Zubao.PutShort(a, 4);
            Zubao.PutShort(a, 52);
            Zubao.putStrHexVer(a, "000a0001000007000020");//固定值
            Zubao.putstrHexGuid(a, Devices_);//固定值
            // 取字节集长度 (到字节集 (ClientType))
            // ＋ 取字节集长度 (到字节集 (appname)) ＋ 30 ＋ 取字节集长度 (到字节集 (Area)) ＋ 取字节集长度 (到字节集 (DwAppClientVer)) ＋
            // 取字节集长度 (到字节集 (network)) ＋ 取字节集长度 (到字节集 (OsVer)) ＋ 取字节集长度 (到字节集 (Screen)) ＋
            // 取字节集长度 (到字节集 (Uuid)) ＋ 取字节集长度 (到字节集 (dwversion)) ＋ 取字节集长度 (到字节集 (Build)) ＋ 取字节集长度 (到字节集 (Partner))
            int v0 = ClientType.length()
                    + appname.length() + 30 + Area.length() + DwAppClientVer.length() +
                    network.length() + OsVer.length() + Screen.length()
                    + Uuid.length() + dwversion.length() + Build.length() + Partner.length();
            Zubao.PutShort(a, 8);
            Zubao.PutShort(a, v0);
            Zubao.PutShort(a, 3);
            Zubao.PutShort(a, DwAppID);
            Zubao.PutString(a, ClientType);
            Zubao.PutString(a, OsVer);
            Zubao.PutString(a, DwAppClientVer);
            Zubao.PutString(a, Screen);
            Zubao.PutString(a, appname);
            Zubao.PutString(a, network);
            Zubao.PutString(a, Area);
            Zubao.PutString(a, Uuid);
            Zubao.PutInt(a, dwGetSig);
            Zubao.PutString(a, dwversion);
            Zubao.PutString(a, Build);
            Zubao.PutString(a, Partner);
            /**
             * 是否是登录
             */
            if (isLogin) {
                Zubao.PutShort(a, 10);
                log.info("开始执行LoginMsg:{}", wskey);
                wskey = wskey.replace("-", "+");
                wskey = wskey.replace("_", "/");
                wskey = wskey.replace(".", "=");
                byte[] bytes = Base64.decodeBase64(wskey);
                int[] ints = Fuzhu.byte2Int(bytes);
                for (int i = 0; i < ints.length; i++) {
                    if (ints[i] < 0) {
                        ints[i] = 256 + ints[i];
                    }
                }
                Zubao.PutBUff(a, ints);
                Zubao.PutShort(a, 16);
                Zubao.PutString(a, pin);
            }
            Zubao.PutShort(a, 36);
            Zubao.PutString(a, QRCodeKey);
            a = Zubao.GetAll(a);
            index = (a.size() + 10) % 8;
            if (index == 0) {
                log.info("返回数据为msg：{}", JSON.toJSONString(a));
                return a;
            }
        }
    }
}
