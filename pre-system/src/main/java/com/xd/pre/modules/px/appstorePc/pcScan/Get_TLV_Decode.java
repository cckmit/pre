package com.xd.pre.modules.px.appstorePc.pcScan;

import com.xd.pre.common.config.LoginPcEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Get_TLV_Decode {
    public static void main(String[] args) {
        int[] data = {0,227,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,1,21,17,221,183,0,7,0,4,0,100,1,17,11,0,3,0,192,0,1,0,11,0,169,68,111,101,115,32,110,111,116,32,101,120,105,115,116,32,116,105,99,107,101,116,32,97,50,58,48,48,48,50,54,48,98,52,100,102,97,100,48,48,52,48,48,102,48,99,55,99,50,55,48,99,100,54,57,49,102,98,57,98,102,98,50,49,102,53,102,101,50,54,54,55,101,101,48,53,101,53,56,52,50,49,102,51,52,98,101,98,97,56,49,56,100,53,51,48,97,50,99,98,56,49,52,57,53,102,54,50,102,48,50,50,101,55,54,50,55,55,51,51,102,57,49,55,98,54,54,55,52,98,99,55,98,49,102,54,100,99,57,98,99,56,49,49,52,51,50,52,52,56,97,52,51,50,56,101,102,51,101,51,50,99,49,53,48,57,49,101,56,98,0,15,232,175,183,233,135,141,230,150,176,231,153,187,229,189,149};
        get_TLV_Decode(data);

    }

    public static LoginPcEnum get_TLV_Decode(int[] data) {
        SDk sdk = new SDk();
        sdk.SDKinit(data);
        int v2 = sdk.getOld_length();
        sdk.getShort();
        sdk.getLong();
        sdk.getInt();
        sdk.getInt();
        sdk.getInt();
        sdk.getShort();
        sdk.getShort();
        sdk.getShort();
        sdk.getShort();
        sdk.getByte();
        Integer v9 = 4;
        Integer v1_1 = sdk.get_length();
        int[] byte1 = {0};
        while (true) {
            if (v1_1 >= v2) {
                break;
            }
            long flag = sdk.getShort();
            if (flag == 3) {
                v1_1 = sdk.getShort();
                sdk.getShort();
                sdk.getShort();
                Integer v4 = sdk.getShort();
                String string = sdk.get_String(v4);
                log.info("flag == 3 sdk返回字符串为msg:{}", string);
                Integer v5_1 = sdk.getShort();
                string = sdk.get_String(v5_1);
                log.info("flag == 3 sdk返回字符串为msg:{}", string);
                log.info("验证码过期");
                return LoginPcEnum.过期;
            }
            if (flag == 34) {
                v1_1 = sdk.getShort();
                sdk.getByte();
                sdk.getInt();
                if (v1_1 <= 5) {
                    v1_1 = sdk.get_length();
                    continue;
                }
            }
            if (flag == 50) {
                int length = sdk.getShort();
                String string = sdk.get_String(length);
                log.info("flag == 50 sdk返回字符串为msg:{}", string);
                v1_1 = sdk.get_length();
                return LoginPcEnum.请电脑端确认;
            }
        }
        return LoginPcEnum.未知情况;
    }
}
