package com.xd.pre.common.utils.px;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;

@Slf4j
public class ParseWeiPayJsonUtil {


    public static  String ParseWeiPayJsonLogEntries(LogEntries logEntries) {
        for (LogEntry logEntry : logEntries) {
            if (ObjectUtil.isNotNull(logEntry) && StrUtil.isNotBlank(logEntry.getMessage()) && logEntry.getMessage().contains("weixin:")) {
                log.info(logEntry.getMessage());
                String documentURL = ParseWeiPayJsonUtil.ParseWeiPayJsonUtil(logEntry.getMessage());
                if (ObjectUtil.isNotNull(logEntry)) {
                    log.info("msg:[documentURL:{}]", documentURL);
                    return documentURL;
                }
            }
        }
        return null;
    }

    public static  String ParseWeiPayJsonUtil(String urlJson) {
        try {
            JSONObject jsonObject = JSON.parseObject(urlJson);
            JSONObject message = JSON.parseObject(jsonObject.get("message").toString());
            String url = JSON.parseObject(message.get("params").toString()).get("url").toString();
            if (StrUtil.isNotBlank(url) && url.contains("weixin://")) {
                log.info("获取微信支付的链接为:msg:[weixin:]", url);
                return url;
            }
        } catch (Exception e) {
            log.info("第一种解析失败");
        }
        try {
            JSONObject jsonObject = JSON.parseObject(urlJson);
            JSONObject message = JSON.parseObject(jsonObject.get("message").toString());
            String documentURL = JSON.parseObject(message.get("params").toString()).get("documentURL").toString();
            if (StrUtil.isNotBlank(documentURL) && documentURL.contains("weixin://")) {
                log.info("获取微信支付的链接为:msg:[weixin:]", documentURL);
                return documentURL;
            }
        } catch (Exception e) {
            log.info("第二种种解析失败");
        }
        return null;
    }
}
