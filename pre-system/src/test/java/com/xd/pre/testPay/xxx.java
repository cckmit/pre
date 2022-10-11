package com.xd.pre.testPay;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class xxx {
    public static void main(String[] args) throws Exception {
        Runtime rt = Runtime.getRuntime();

        ArrayList<String> strings = new ArrayList<>();
        if (true) {
            Process pr = rt.exec("adb devices");
            String line = null;
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream(), "GBK"));
            StringBuilder lineTotal = new StringBuilder();
            while ((line = input.readLine()) != null) {
                lineTotal.append(line);
            }
            String list_of_devices_attached = lineTotal.toString().replace("List of devices attached", "");
            String[] ss = list_of_devices_attached.split("127.0.0.1:");
            for (String s1 : ss) {
                if (StrUtil.isNotBlank(s1.trim())) {
                    String replace = s1.replace("\tdevice", "");
                    //127.0.0.1:62025
                    strings.add("127.0.0.1:" + replace);
                }
            }
        }
        System.out.println("当前设备号:" + JSON.toJSONString(strings));
        for (String string : strings) {
            Process pr = rt.exec("adb -s " + string + " shell settings put global http_proxy 192.168.2.149:8866");
            String line = null;
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream(), "GBK"));
            StringBuilder lineTotal = new StringBuilder();
            while ((line = input.readLine()) != null) {
                lineTotal.append(line);
            }
            System.out.println(StrUtil.isBlank(lineTotal.toString()) ? "执行成功" : lineTotal.toString());
        }
        if (true) {
            Process pr = rt.exec("adb remount");
            String line = null;
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream(), "GBK"));
            StringBuilder lineTotal = new StringBuilder();
            while ((line = input.readLine()) != null) {
                lineTotal.append(line);
            }
            System.out.println(StrUtil.isBlank(lineTotal.toString()) ? "执行成功" : lineTotal.toString());
        }

        for (String string : strings) {
            String m = "adb -s " + string + "  push C:\\Users\\Administrator\\Desktop\\269953fb.0 /system/etc/security/cacerts/";
            System.out.println(m);
            Process pr = rt.exec(m);
            String line = null;
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream(), "GBK"));
            StringBuilder lineTotal = new StringBuilder();
            while ((line = input.readLine()) != null) {
                lineTotal.append(line);
            }
            System.out.println(StrUtil.isBlank(lineTotal.toString()) ? "执行成功" : lineTotal.toString());
        }
        for (String string : strings) {
            if (false) {
                return;
            }
            String m = "adb -s " + string + "  install D:\\desk\\抖音\\jinri.apk";
            System.out.println(m);
            Process pr = rt.exec(m);
            String line = null;
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream(), "GBK"));
            StringBuilder lineTotal = new StringBuilder();
            while ((line = input.readLine()) != null) {
                lineTotal.append(line);
            }
            System.out.println(StrUtil.isBlank(lineTotal.toString()) ? "执行成功" : lineTotal.toString());
        }

    }
}
