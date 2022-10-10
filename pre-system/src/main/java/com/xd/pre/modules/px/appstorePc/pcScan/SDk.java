package com.xd.pre.modules.px.appstorePc.pcScan;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class SDk {
    private int[] this_a;
    private Integer old_length;


    public void SDKinit(int[] data) {
        int[] enc = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            enc[i] = data[i];
        }
        this.this_a = enc;
        this.old_length = data.length;
        return;
    }

    public Integer getShort() {
       /* byte ＝ REV (取字节集左边 (this_a, 2))
        this_a ＝ 取字节集右边 (this_a, 取字节集长度 (this_a) － 2)
        a ＝ 取字节集数据 (byte, #短整数型, )*/
        int[] byte1 = Fuzhu.REV(Fuzhu.取字节集左边(this.this_a, 2));
        this.this_a = Fuzhu.取字节集右边(this_a, this.this_a.length - 2);
        return get字节集Long(byte1);
    }


    public String get_String(Integer length) {
        // ret ＝ 编码_Utf8到Ansi (取字节集左边 (this_a, length))
        // this_a ＝ 取字节集右边 (this_a, 取字节集长度 (this_a) － length)
        int[] 取字节集左边 = Fuzhu.取字节集左边(this.this_a, length);
        byte[] bytes = Fuzhu.Int2Byte(取字节集左边);
        String s = bytetoString(bytes);
        // this_a ＝ 取字节集右边 (this_a, 取字节集长度 (this_a) － length)
        this.this_a = Fuzhu.取字节集右边(this.this_a, this.this_a.length - length);
        return s;
    }


    public Integer getLong() {
       /* byte ＝ REV (取字节集左边 (this_a, 2))
        this_a ＝ 取字节集右边 (this_a, 取字节集长度 (this_a) － 2)
        a ＝ 取字节集数据 (byte, #短整数型, )*/
        int[] byte1 = Fuzhu.REV(Fuzhu.取字节集左边(this.this_a, this.this_a.length - 8));
        this.this_a = Fuzhu.取字节集右边(this_a, this.this_a.length - 8);
        Integer returnLong = get字节集Long(byte1);
        return returnLong;
    }

    public Integer getInt() {
       /* byte ＝ REV (取字节集左边 (this_a, 2))
        this_a ＝ 取字节集右边 (this_a, 取字节集长度 (this_a) － 2)
        a ＝ 取字节集数据 (byte, #短整数型, )*/
        int[] byte1 = Fuzhu.REV(Fuzhu.取字节集左边(this.this_a, this.this_a.length - 4));
        this.this_a = Fuzhu.取字节集右边(this_a, this.this_a.length - 4);
        Integer returnLong = get字节集Long(byte1);
        return returnLong;
    }

    public int[] getByte() {
        // byte ＝ REV (取字节集左边 (this_a, 1))
        int[] byte1 = Fuzhu.REV(Fuzhu.取字节集左边(this.this_a, 1));
        this.this_a = Fuzhu.取字节集右边(this_a, this.this_a.length - 1);
        // System.err.println(JSON.toJSONString(Arrays.stream(this.this_a).boxed().collect(Collectors.toList())));
        return byte1;
    }

    public Integer get_length() {
        return this.old_length - this.this_a.length;
    }


    private Integer get字节集Long(int[] byte1) {
        Integer retunrlong = 0;
        for (int i = 0; i < byte1.length; i++) {
            Integer max = 1 * byte1[i];
            for (int j = 0; j < i; j++) {
                max = max * 256;
            }
            retunrlong = retunrlong + max;
        }
        return retunrlong;
    }

    /*
     * 字节数组转为普通字符串（ASCII对应的字符）
     *
     * @param bytearray
     *            byte[]
     * @return String
     */
    public static String bytetoString(byte[] bytearray) {
        try {
            String bianma = new String(bytearray, "UTF-8");//编码 如果上面的解码不对 可能出现问题
            return bianma;
        } catch (Exception e) {
            log.error("当前编码错误msg:{},bytearray:{}", e.getMessage(), bytearray);
        }
        return null;
    }
}
