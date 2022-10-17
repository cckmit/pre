package com.xd.pre;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.common.utils.px.dto.UrlEntity;

import java.util.Date;
import java.util.List;

public class TestDemo {


    public static void main(String[] args) {
        List<String> STRINGS = FileUtil.readLines("C:\\Users\\Administrator\\Desktop\\douyinck\\10.17 506号.txt", "UTF-8");
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : STRINGS) {
            boolean contains = line.contains("Set-Cookie:") && line.contains("sid_tt=");
            if (contains) {
                String[] split = line.split(";")[0].split(":");
                stringBuilder.append(split[1] + ";");
            }
            if (line.equals("====================================")) {
                System.out.println(stringBuilder.toString().trim());
                stringBuilder = new StringBuilder();
            }
        }
        StringBuilder stringBuilder1 = new StringBuilder();

        for (String string : STRINGS) {
            if (string.contains("https://")) {
                UrlEntity urlEntity = PreUtils.parseUrl(string);
                String device_id = urlEntity.getParams().get("device_id");
                String iid = urlEntity.getParams().get("iid");
                stringBuilder1.append(device_id + "===" + iid);
                String format = String.format("INSERT INTO douyin_device_iid ( device_id, iid,fail_reason )VALUES('%s', '%s');", device_id, iid, DateUtil.formatDateTime(new Date()));
//                System.out.println(format);
                stringBuilder1 = new StringBuilder();
            }
        }
    }
}
