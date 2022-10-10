package com.xd.pre.modules.px.appstorePc.pcScan;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Data
public class Zubao {
    private List<Integer> this1;

    public static void main(String[] args) {
        int[] a1 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 7, 0, 4, 0, 100, 1, 17, 0, 0, 4, 0, 52, 48, 48, 48, 97, 48, 48, 48, 49, 48, 48, 48, 48, 48, 55, 48, 48, 48, 48, 50, 48, 68, 52, 49, 68, 56, 67, 68, 57, 56, 70, 48, 48, 66, 50, 48, 52, 69, 57, 56, 48, 48, 57, 57, 56, 69, 67, 70, 56, 52, 50, 55, 69, 0, 8, 0, 98, 0, 3, 0, 100, 0, 7, 97, 110, 100, 114, 111, 105, 100, 0, 0, 0, 5, 57, 46, 49, 46, 48, 0, 1, 42, 0, 5, 106, 100, 97, 112, 112, 0, 2, 52, 71, 0, 20, 49, 48, 55, 46, 52, 56, 57, 53, 50, 51, 95, 50, 55, 46, 55, 54, 52, 55, 53, 51, 0, 13, 45, 49, 98, 55, 56, 56, 57, 53, 97, 49, 101, 101, 55, 0, 0, 0, 1, 0, 5, 54, 46, 49, 46, 48, 0, 5, 56, 51, 55, 56, 57, 0, 5, 107, 115, 48, 48, 54, 0, 10};
        List<Integer> a = Arrays.stream(a1).boxed().collect(Collectors.toList());
        int[] vx2 = {0, 2, 96, 180, 223, 173, 0, 64, 15, 12, 124, 39, 12, 214, 145, 251, 155, 251, 33, 245, 254, 38, 103, 238, 5, 229, 132, 33, 243, 75, 235, 168, 24, 213, 48, 162, 203, 129, 73, 95, 98, 240, 34, 231, 98, 119, 51, 249, 23, 182, 103, 75, 199, 177, 246, 220, 155, 200, 17, 67, 36, 72, 164, 50, 142, 243, 227, 44, 21, 9, 30, 139};
        PutBUff(a, vx2);
        System.out.println(JSON.toJSONString(a));

    }

    public static void init_QR(List<Integer> a, Integer type) {
        PutShort(a, 0);
        PutLong(a, 1);
        PutInt(a, 1);
        PutInt(a, 1);
        PutInt(a, 0);
        PutShort(a, 7);
        PutShort(a, type);
        PutShort(a, 100);
        PutShort(a, 273);
        PutByte(a, 0);
    }

    public static List<Integer> GetAll(List<Integer> a) {
        int len = a.size();
        int[] rev = Fuzhu.REV(到字节集(len));
        int[] byte1 = Fuzhu.取字节集右边(rev, 2);
        int[] t = a.stream().mapToInt(Integer::intValue).toArray();
        int[] st = Fuzhu.取字节集右边(t, t.length - 2);
        int[] ints = Fuzhu.addInt__(byte1, st);
        a = Arrays.stream(ints).boxed().collect(Collectors.toList());
        return a;
    }

    public static int[] 到字节集(int a) {
        int[] ints = new int[4];
        int i = a / 256;
        ints[0] = a % 256;
        //80,1,0,0
        if (i > 0 && i < 256) {
            ints[1] = i;
            return ints;
        }
        return ints;
    }


    public static void PutString(List<Integer> a, String str) {
        if (StrUtil.isBlank(str)) {
            PutShort(a, 0);
            return;
        } else {
            byte[] bytes = str.getBytes();
            PutShort(a, str.length());
            for (byte aByte : bytes) {
                Integer byteToInte = Integer.valueOf(aByte);
                a.add(byteToInte);
            }
        }
    }

    public static void putStrHexVer(List<Integer> a, String str) {
        byte[] bytes = str.getBytes();
        for (byte aByte : bytes) {
            Integer byteToInte = Integer.valueOf(aByte);
            a.add(byteToInte);
        }
    }


    public static void putstrHexGuid(List<Integer> a, String str) {
        byte[] bytes = str.getBytes();
        for (byte aByte : bytes) {
            Integer byteToInte = Integer.valueOf(aByte);
            a.add(byteToInte);
        }
    }

    public static void PutBUff(List a, int[] byte1) {
        PutShort(a, byte1.length);
        for (int i = 0; i < byte1.length; i++) {
            a.add(byte1[i]);
        }
    }

    public static void PutShort(List<Integer> a, Integer value) {
        if (value >= 256) {
            int i1 = value / 256;
            int i2 = value % 256;
            a.add(i1);
            a.add(i2);
        } else if (value >= 0 && value < 256) {
            a.add(0);
            a.add(value);
        }
    }

    public static void PutInt(List<Integer> a, Integer value) {
        for (int i = 0; i < 4 - value.toString().length(); i++) {
            a.add(0);
        }
        a.add(value);
    }

    public static void PutLong(List<Integer> a, Integer value) {
        for (int i = 0; i < 8 - value.toString().length(); i++) {
            a.add(0);
        }
        a.add(value);
    }

    public static void PutByte(List<Integer> a, Integer value) {
        a.add(value);
    }


}
