package com.xd.pre.modules.px.appstorePc.pcScan;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Get_TeaDecryptECB {
    public static void main(String[] args) {
        int[] enc_data = {158, 142, 24, 191, 101, 0, 172, 4};
        int[] keyIntArr = Fuzhu.byte2Int("qvnjxuiywxlsjhfa".getBytes());
        get_TeaDecryptECB(enc_data, keyIntArr);
    }

    public static int[] get_TeaDecryptECB(int[] enc_data, int[] key) {
        // int[] rev = Fuzhu.REV(vx1);
        long v12 = Fuzhu.取字节集数据(Fuzhu.取字节集中间(key, 13, 4));
        // v11 ＝ 取字节集数据 (REV (取字节集中间 (key, 9, 4)), #长整数型, )
        //v9 ＝ 取字节集数据 (REV (取字节集中间 (key, 1, 4)), #长整数型, )
        //v10 ＝ 取字节集数据 (REV (取字节集中间 (key, 5, 4)), #长整数型, )
        //v4 ＝ 取字节集数据 (REV (取字节集中间 (enc_data, 1, 4)), #长整数型, )
        //v5 ＝ 取字节集数据 (REV (取字节集中间 (enc_data, 5, 4)), #长整数型, )
        long v11 = Fuzhu.取字节集数据(Fuzhu.取字节集中间(key, 9, 4));
        long v9 = Fuzhu.取字节集数据(Fuzhu.取字节集中间(key, 1, 4));
        long v10 = Fuzhu.取字节集数据(Fuzhu.取字节集中间(key, 5, 4));
        long v4 = Fuzhu.取字节集数据(Fuzhu.取字节集中间(enc_data, 1, 4));
        long v5 = Fuzhu.取字节集数据(Fuzhu.取字节集中间(enc_data, 5, 4));
        long v7 = 3816266640L;
        while (true) {
            if (v7 == 0) {
                break;
            }
            v5 = Fuzhu.SUB(v5, Fuzhu.ADD(v12, v4 >> 5) ^ Fuzhu.ADD(v11, Fuzhu.MUL(16, v4)) ^ Fuzhu.ADD(v7, v4));
            //    v4 ＝ SUB (v4, Long_Xor (Long_Xor (ADD (v10, Long_Sar (v5, 5)), ADD (v9, MUL (16, v5))), ADD (v5, v7)))
            v4 = Fuzhu.SUB(v4, Fuzhu.ADD(v10, v5 >> 5) ^ Fuzhu.ADD(v9, Fuzhu.MUL(16, v5)) ^ Fuzhu.ADD(v5, v7));
            v7 = Fuzhu.ADD(v7, 1640531527L);
        }
        String v4Str = Fuzhu.decode10TO16(v4);

        Integer v4StrL = v4Str.length();
        for (int i = 0; i < 8 - v4StrL; i++) {
            v4Str = "0" + v4Str;
        }

        String v5Str = Fuzhu.decode10TO16(v5);
        Integer v5StrL = v5Str.length();
        for (int i = 0; i < 8-v5StrL; i++) {
            v5Str = "0" + v5Str;
        }

        int[] ints = Fuzhu.bin2hex_(v4Str + v5Str);
        // TODO
        // log.info("get_TeaDecryptECB:返回值{}", JSON.toJSONString(Arrays.stream(ints).boxed().collect(Collectors.toList())));
        return ints;
    }
}
