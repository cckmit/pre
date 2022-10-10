package com.xd.pre;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Slf4j
public class CMDMain {
    public static void main(String[] args) throws Exception {

        for (int i = 0; i < 1000000; i++) {
            Thread.sleep(1000L);
            Runtime rt = Runtime.getRuntime();
            if (i % 2 == 0) {
                Process pr = rt.exec("adb push C:\\Users\\Administrator\\Desktop\\269953fb.0 /system/etc/security/cacerts/");
                BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream(), "GBK"));
                String line = null;
                StringBuilder lineTotal = new StringBuilder();
                while ((line = input.readLine()) != null) {
                    lineTotal.append(line);
                }
                log.info(lineTotal.toString());
            }
            if (i % 2 == 1) {
                Process pr = rt.exec("adb shell settings put global http_proxy 192.168.2.149:8866");
                BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream(), "GBK"));
                String line = null;
                StringBuilder lineTotal = new StringBuilder();
                while ((line = input.readLine()) != null) {
                    lineTotal.append(line);
                }
                log.info(lineTotal.toString());
            }
        }
    }
}
