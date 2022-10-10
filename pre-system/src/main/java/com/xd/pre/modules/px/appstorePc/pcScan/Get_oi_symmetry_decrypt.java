package com.xd.pre.modules.px.appstorePc.pcScan;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
public class Get_oi_symmetry_decrypt {

    public static void main(String[] args) {
        int[] input = {74,26,33,60,237,185,103,186,19,25,134,48,113,197,213,21,124,10,144,217,216,138,192,188,175,63,97,76,109,155,150,98,65,70,201,7,195,133,247,223,38,187,97,220,14,142,224,103,173,207,230,81,131,45,64,184,23,91,226,7,70,47,201,154,151,207,130,161,30,208,31,236,203,188,250,14,137,164,92,178,202,68,1,0,222,182,4,111};
        int[] key = Fuzhu.byte2Int("qvnjxuiywxlsjhfa".getBytes());
        int[] oi_symmetry_decrypt = get_oi_symmetry_decrypt(input, key);
        System.out.println(oi_symmetry_decrypt);
    }

    public static int[] get_oi_symmetry_decrypt(int[] input, int[] key) {
        Integer length = input.length;
        int v7 = length & 7;
        int[] v28 = Get_TeaDecryptECB.get_TeaDecryptECB(Fuzhu.取字节集左边(input, 8), key);
        int v12 = v28[0] & 7;
        int v13 = length - v12 - 10;
        int[] v29 = new int[8];
        int[] v14 = new int[8];
        int[] v15 = Fuzhu.取字节集中间(input, 9, 8);
        int v15_i = 0;
        int v16 = v12 + 1;
        int v17 = 1;
        int[] v8 = input;
        int[] output = {};
        int[] ret = {};
        int v21 = -1;
        int v22 = -1;
        Boolean LABEL_17 = false;
        while (true) {
            if (v16 == 8) {

                break;
            }
            v16 = v16 + 1;
            v17 = v17 + 1;
            if (v17 > 2) {
                int v20 = v13;
                mark1:
                while (true) {
                    while (true) {
                        if (v20 == 0) {
                            break mark1;
                        }
                        if (v16 == 8) {
                            int v23 = 0;
                            while (true) {
                                // 判断循环首 (v23 ＋ v7 ＜ length)
                                if (v23 + v7 >= length) {
                                    break;
                                }
                                //v28 [v23 ＋ 1] ＝ 位异或 (v28 [v23 ＋ 1], v15 [v23 ＋ 1])
                                v28[v23] = v28[v23] ^ v15[v23];
                                // System.err.println(JSON.toJSONString(Arrays.stream(v28).boxed().collect(Collectors.toList())));
                                v23 = v23 + 1;
                                if (v23 == 8) {
                                    int v24 = v20;
                                    v7 = v7 + 8;
                                    v28 = Get_TeaDecryptECB.get_TeaDecryptECB(v28, key);
                                    ret = Fuzhu.addInt__(ret, v28);
                                    v14 = Fuzhu.取字节集中间(input, 8 * v15_i + 1, 8);
                                    v16 = 0;
                                    v8 = Fuzhu.取字节集中间(input, 8 * (1 + v15_i) + 1, 8);
                                    v20 = v24;
                                    v15_i = v15_i + 1;
                                    v15 = Fuzhu.取字节集中间(input, 8 * (1 + v15_i) + 1, 8);
                                    // log.info("v15:{}", JSON.toJSONString(Arrays.stream(v15).boxed().collect(Collectors.toList())));
                                    LABEL_17 = true;
                                    break;
                                }
                            }
                        }
                        if (LABEL_17) {
                            v21 = v14[v16];
                            v22 = v28[v16];
                            int[] t = {(v21 ^ v22) % 256};
                            output = Fuzhu.addInt__(output, t);
                            v20 = v20 - 1;
                            v16 = v16 + 1;
                            LABEL_17 = false;
                            break;
                        }

                        v21 = v14[v16];
                        v22 = v28[v16];
                        int[] t = {(v21 ^ v22) % 256};
                        output = Fuzhu.addInt__(output, t);
                        v20 = v20 - 1;
                        v16 = v16 + 1;

                    }
                }
                int V25 = 1;
                while (true) {
                    if (v16 == 8) {
                        break;
                    }
                    int[] v26 = v15;
                    v16 = v16 + 1;
                    V25 = V25 + 1;
                    v15 = v8;
                    if (V25 > 7) {
                        log.info("output:{}", JSON.toJSONString(Arrays.stream(output).boxed().collect(Collectors.toList())));
                        return output;
                    }
                    v8 = v15;
                    v15 = v26;
                }
            }


        }
        return null;
    }
}
