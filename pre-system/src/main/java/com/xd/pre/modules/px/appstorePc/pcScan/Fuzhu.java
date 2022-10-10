package com.xd.pre.modules.px.appstorePc.pcScan;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;

@Slf4j
public class Fuzhu {

    public static int[] get_TeaEncryptECB(int[] data, int[] key) {
        Long result = 3816266640L;
        Long v6 = 0L;
        String hex = bin2hex_(data);
        String hex_key = bin2hex_(key);
        // v3 ＝ 进制_十六到十 (取文本左边 (hex, 8))
        long v3 = decode16TO10(取文本左边(hex, 8));
        //v4 ＝ 进制_十六到十 (取文本右边 (hex, 8))
        long v4 = decode16TO10(取文本右边(hex, 8));

       /* V8 ＝ 进制_十六到十 (取文本左边 (hex_key, 8))
        v9 ＝ 进制_十六到十 (取文本中间 (hex_key, 9, 8))
        v10 ＝ 进制_十六到十 (取文本中间 (hex_key, 17, 8))
        v11 ＝ 进制_十六到十 (取文本右边 (hex_key, 8))*/
        long V8 = decode16TO10(取文本左边(hex_key, 8));
        long v9 = decode16TO10(取文本中间(hex_key, 9 - 1, 8));
        long v10 = decode16TO10(取文本中间(hex_key, 17 - 1, 8));
        long v11 = decode16TO10(取文本右边(hex_key, 8));
     /*   v6 ＝ SUB (v6, 1640531527)
        v3 ＝ ADD (v3, Long_Xor (Long_Xor (ADD (v9, Long_Sar (v4, 5)), ADD (V8, 16 × v4)), ADD (v4, v6)))
        v4 ＝ ADD (v4, Long_Xor (Long_Xor (ADD (v10, 16 × v3), ADD (v11, Long_Sar (v3, 5))), ADD (v3, v6)))*/
        while (true) {
            if (result.equals(v6)) {
                break;
            }
            v6 = SUB(v6, 1640531527L);
            v3 = ADD(v3, ADD(v4, v6) ^ (v4 >> 5) + v9 ^ ADD(V8, 16 * v4));
            v4 = ADD(v4, ADD(v3, v6) ^ ADD(v10, 16 * v3) ^ ADD(v11, v3 >> 5));
        }
        // hex_ret ＝ 补位 (进制_十到十六 (v3)) ＋ 补位 (进制_十到十六 (v4))
        String s3 = Long.toHexString(v3);
        String s4 = Long.toHexString(v4);
        if (s3.length() < 8) {
            for (int i = 0; i < 8 - s3.length(); i++) {
                s3 = "0" + s3;
            }
        }
        if (s4.length() < 8) {
            for (int i = 0; i < 8 - s4.length(); i++) {
                s4 = "0" + s4;
            }
        }
        String s3_s4 = (s3 + s4).toUpperCase();
        int[] ints = bin2hex_(s3_s4);
        //log.info("返回值msg:{}", Arrays.stream(ints).boxed().collect(Collectors.toList()));
        return ints;
    }


    public static int[] bin2hex_(String 原始16进制文本) {
        if (原始16进制文本.length() % 2 == 1) {
            原始16进制文本 = "0" + 原始16进制文本;
        }
        byte[] bytes = 原始16进制文本.getBytes();
        int[] ints = byte2Int(bytes);
        int ki = ints.length;
        int[] 结果字节集 = new int[ki / 2];
        for (int i = 1; i < ki + 1; i++) {
            int t = 0;
            if (ints[i - 1] < 58) {
                t = ints[i - 1] - 48;
            }
            if (ints[i - 1] >= 58 && ints[i - 1] <= 80) {
                t = ints[i - 1] - 55;
            }
            if (ints[i - 1] > 80) {
                t = ints[i - 1] - 87;
            }
            if (i % 2 == 1) {
                // 结果字节集 [i ÷ 2] ＝ 结果字节集 [i ÷ 2] × 16 ＋ 临时整数
                结果字节集[i / 2] = t;
            } else {
                结果字节集[(i - 2) / 2] = 结果字节集[(i - 2) / 2] * 16 + t;
            }
        }
        return 结果字节集;
    }

    public static String bin2hex_(int[] data) {
        int[] 数组 = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70};
        int ki = data.length;
        int[] 返回字节集 = new int[ki * 2];
        for (int i = 0; i < ki * 2; i++) {
            返回字节集[i] = 0;
        }
        for (int i = 1; i < ki + 1; i++) {
            int 高4位 = data[i - 1] / 16 + 1;//4
            int 低4位 = data[i - 1] % 16 + 1;
            返回字节集[i * 2 - 1 - 1] = 数组[高4位 - 1];
            返回字节集[i * 2 - 1] = 数组[低4位 - 1];
        }
        StringBuilder stringBuilder = new StringBuilder();

        for (int i : 返回字节集) {
            stringBuilder.append((char) i);
        }
        return stringBuilder.toString();
    }

    public static int[] byte2Int(byte[] data) {
        int[] ints = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            ints[i] = data[i];
        }
        return ints;
    }

    public static byte[] Int2Byte(int[] data) {
        byte[] ints = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            ints[i] = (byte) data[i];
        }
        return ints;
    }

    public static long decode16TO10(String hexs) {
        BigInteger bigInteger = new BigInteger(hexs, 16);
        long value = bigInteger.longValue();
        return value;
    }

    public static String decode10TO16(Long num) {
        String value = Long.toHexString(num);
        return value;
    }

    public static String 取文本右边(String hex, Integer index) {
        return hex.substring(hex.length() - index, hex.length());
    }

    public static String 取文本左边(String hex, Integer index) {
        return hex.substring(0, index);
    }

    public static String 取文本中间(String hex, Integer indexstart, Integer length) {
        return hex.substring(indexstart, indexstart + length);
    }

    public static int[] 取字节集右边(int[] hex, Integer length) {
        return 取字节集中间(hex, hex.length - length + 1, length + 1);
    }

    public static int[] 取字节集左边(int[] hex, Integer length) {
        return 取字节集中间(hex, 1, length);
    }

    public static int[] 取字节集中间(int[] hex, Integer indexstart, Integer length) {
        indexstart = indexstart - 1;
        int[] ints = new int[length];
        for (int i = 0; i < length; i++) {
            if ((indexstart + i) >= hex.length) {
                int[] ints1 = new int[i];
                for (int j = 0; j < i; j++) {
                    ints1[j] = ints[j];
                }
                return ints1;
            } else {
                ints[i] = hex[indexstart + i];
            }
        }
        return ints;
    }

    /**
     * 取反
     *
     * @param a
     * @return
     */
    public static int[] REV(int[] a) {
        int[] retunrInt = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            retunrInt[i] = a[a.length - 1 - i];
        }
        return retunrInt;
    }

    public static Long SUB(Long fist, Long second) {
        return (fist - second) & 4294967295L;
    }

    public static Long ADD(Long a, Long b) {
        long l = a + b;
        String s = Long.toBinaryString(l);
        if (s.length() > 32) {
            String 取文本右边 = 取文本右边(s, 32);
            return Long.parseLong(取文本右边, 2);
        } else {
            return a + b;
        }
    }

    public static int[] addInt__(int[] a, int[] b) {
        int[] ints = new int[a.length + b.length];
        for (int i = 0; i < a.length; i++) {
            ints[i] = a[i];
        }
        for (int i = 0; i < b.length; i++) {
            ints[i + a.length] = b[i];
        }
        return ints;
    }

    public static int[] nullInt__(int length) {
        int[] ints = new int[length];
        for (int i = 0; i < length; i++) {
            ints[i] = 0;
        }
        return ints;
    }


    public static long 取字节集数据(int[] a) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            String s = decode10TO16(Long.valueOf(a[i] + ""));
            if (s.length() <= 1) {
                s = "0" + s;
            }
            if (s.length() >= 3) {
                s = s.substring(s.length() - 2);
            }
            stringBuilder.append(s);
        }
        long l = decode16TO10(stringBuilder.toString());
        return l;
    }


    public static long MUL(long a, long b) {
        long l = a * b;
        long l1 = l & 4294967295L;
        return l1;
    }

    public static int[] 易语言Base64(String data) {
        byte[] bytes = Base64.decodeBase64(data);
        int[] ints = byte2Int(bytes);
        int[] returnInt = new int[ints.length];

        for (int i = 0; i < ints.length; i++) {
            if (ints[i] < 0) {
                returnInt[i] = 256 + ints[i];
            } else {
                returnInt[i] = ints[i];
            }
        }
        return returnInt;

    }

}
