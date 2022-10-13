package com.xd.pre.testPay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class tap {
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
            String[] ss = list_of_devices_attached.split("\tdevice");
            for (String s1 : ss) {
                String replace = s1.replace("\tdevice", "");
                //127.0.0.1:62025
                System.out.println(replace);
                strings.add(replace);
            }
        }
        for (String string : strings) {
            Process pr = rt.exec("adb -s " + string + " shell settings put global http_proxy 192.168.2.10:8866");
            String line = null;
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream(), "GBK"));
            StringBuilder lineTotal = new StringBuilder();
            while ((line = input.readLine()) != null) {
                lineTotal.append(line);
            }
            System.out.println(lineTotal.toString() == "" ? "执行成功" : lineTotal.toString());
        }
        if (true) {
            for (String string : strings) {
                Process pr = rt.exec("adb -s " + string + " remount");
                String line = null;
                BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream(), "GBK"));
                StringBuilder lineTotal = new StringBuilder();
                while ((line = input.readLine()) != null) {
                    lineTotal.append(line);
                }
                System.out.println(lineTotal.toString() == "" ? "执行成功" : lineTotal.toString());
            }
        }

        for (String string : strings) {
            if (false) {
                return;
            }
            String m = "adb -s " + string + "   shell input tap 270 680";
            System.out.println(m);
            Process pr = rt.exec(m);
            String line = null;
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream(), "GBK"));
            StringBuilder lineTotal = new StringBuilder();
            while ((line = input.readLine()) != null) {
                lineTotal.append(line);
            }
            System.out.println(lineTotal.toString() == null ? "执行成功" : lineTotal.toString());
        }


    }
}

