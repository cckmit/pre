package com.xd.pre.modules.px.appstorePc.pcScan;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
public class Get_oi_symmetry_encrypt {

    public static String get_oi_symmetry_encrypt(int[] intput, String key) {
        Integer Len = intput.length;
        Integer v7 = Len;
        Integer v9 = (Len + 10) % 8;
        Integer v10 = (Len + 10) % 8;
        if (v9 != 0) {
            v10 = 8 - v9;
        }
        int a[] = {(0 & -8) | v10};
        int[] v40 = Fuzhu.addInt__(a, Fuzhu.byte2Int("xazcfzw".getBytes()));
        Integer v15 = v10 + 1;
        int[] v14 = Fuzhu.nullInt__(8);
        int[] v41 = Fuzhu.nullInt__(8);
        int[] v16 = v40;
        Integer v17 = 1;
        int[] v18 = v41;
        Integer v19 = 0;
        while (true) {
            Integer v19_t = v15 + 1;
            Integer v21 = v17;
            v17 = v21 + 1;
            v16 = v40;
            v15 = v19_t;
            if (v17 > 2) {
                v19 = v19_t;
                break;
            }
        }
        int[] v28 = v40;
        Integer v28_i = 0;
        v7 = v7 + v19;
        int[] output = {};
        while (true) {
            if (v7 == 0) {
                break;
            }
            if (v19 != 8) {
                for (int i = 0; i < v19; i++) {
                    int[] t = {0};
                    intput = Fuzhu.addInt__(t, intput);
                }
                v19 = 8;
            }
            v28 = Fuzhu.取字节集中间(intput, 8 * v28_i + 1, 8);
            if (v28.length != 8) {
                break;
            }

            for (int i = 0; i < 8; i++) {
                v28[i] = v28[i] ^ v18[i];
            }

            int[] v39 = v28;
            // log.info("输入msg:{}", Arrays.stream(v28).boxed().collect(Collectors.toList()));
            int[] v6 = Fuzhu.get_TeaEncryptECB(v28, Fuzhu.byte2Int(key.getBytes()));
            v28 = v39;
            for (int i = 0; i < 8; i++) {
                v6[i] = v6[i] ^ v41[i];
            }
            System.out.println();
            v41 = v39;
            v18 = v6;
            output = Fuzhu.addInt__(output, v6);
            v28_i = v28_i + 1;
            v7 = v7 - 8;
        }
        // log.info("当前output:{}", JSON.toJSONString(Arrays.stream(output).boxed().collect(Collectors.toList())));
        if (v28.length != 0) {
            int v28Le = v28.length;
            for (int i = 0; i < 8 - v28Le; i++) {
                int[] z = {0};
                v28 = Fuzhu.addInt__(v28, z);
            }
            for (int i = 0; i < 8; i++) {
                v28[i] = v28[i] ^ v18[i];
            }
            int[] v39 = v28;
            int[] v6 = Fuzhu.get_TeaEncryptECB(v28, Fuzhu.byte2Int(key.getBytes()));
            v28 = v39;
            for (int i = 0; i < 8; i++) {
//                v6 [i] ＝ 位异或 (v6 [i], v41 [i])
                v6[i] = v6[i] ^ v41[i];
            }
            output = Fuzhu.addInt__(output, v6);
            log.info("当前output:{}", JSON.toJSONString(Arrays.stream(output).boxed().collect(Collectors.toList())));
            int[] returnint = Fuzhu.addInt__(Fuzhu.byte2Int(key.getBytes()), output);
            String returnData = Base64.encodeBase64String(Fuzhu.Int2Byte(returnint));
            log.info("get_oi_symmetry_encrypt最终返回值msg:{}", returnData);
            return returnData;
        }
        return null;
    }
}
