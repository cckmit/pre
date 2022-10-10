package com.xd.pre.socekt.s2;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.xd.pre.modules.px.appstorePc.pcScan.Fuzhu;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64InputStream;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] args) {
        byte []  a  ={49,56,51,46,50,50,49,46,49,56,46,49,56,48};
        List<Integer> collect = Arrays.stream(Fuzhu.byte2Int(a)).boxed().collect(Collectors.toList());
        System.out.println(JSON.toJSONString(collect));
        String s = new String(a);
        System.out.println(s);

    }
}
