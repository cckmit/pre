package com.xd.pre.register;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.nosql.redis.RedisDS;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xd.pre.common.utils.px.PreUtils;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class TestRegister1 {
    public static Db db = Db.use();

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            try {
                String s = buildExec();
                log.info("当前循环次数:{},结果值:{}", i, s);
                if (StrUtil.isBlank(s)) {
                    continue;
                }
                JSONObject parseObject = JSON.parseObject(s);
//                DouyinDeviceIid build = DouyinDeviceIid.builder().deviceId(parseObject.getString("device_id"))
//                        .iid(parseObject.getString("install_id")).failReason(DateUtil.formatDateTime(new Date())).build();
                db.use().insert(Entity.create("douyin_device_iid")
                        .set("device_id", parseObject.getString("device_id"))
                        .set("iid", parseObject.getString("iid"))
                        .set("fail_reason", DateUtil.formatDateTime(new Date())));
            } catch (Exception e) {
                log.info("当前循环次数:{},不晓得啥鬼错:{}", i, e.getMessage());
            }
        }
    }

    public static String buildExec() {
        try {
            JSONObject ipAndPort = getIp();
            String ip = ipAndPort.getString("ip");
            Integer port = ipAndPort.getInteger("port");
            Runtime rt = Runtime.getRuntime();
            String format = String.format("python  E:\\px\\dy\\TikTok-register-device_id\\demo.py %s %s", ip, port + "");
            Process pr = rt.exec(format);
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream(), "GBK"));
            String line = null;
            StringBuilder lineTotal = new StringBuilder();
            while ((line = input.readLine()) != null) {
                lineTotal.append(line);
            }
            String s = lineTotal.toString();
            if (s.contains("device_token") && s.contains("device_id")) {
                String replace = s.replace("'", "\"");
                return replace;
            }
        } catch (Exception e) {
            log.info("报错了");
        }
        return null;
    }

    public static Jedis jedis = RedisDS.create().getJedis();

    public static JSONObject getIp() {
        try {
            Set<String> keys = jedis.keys("IP缓存池注册:*");
            if (CollUtil.isEmpty(keys) || keys.size() < 20) {
                String proxyData = HttpUtil.get("http://webapi.http.zhimacangku.com/getip?num=20&type=2&pro=&city=0&yys=0&port=1&time=1&ts=1&ys=0&cs=0&lb=1&sb=0&pb=4&mr=1&regions=");
                JSONObject parseObject = JSON.parseObject(proxyData);
                String data = parseObject.getString("data");
                List<JSONObject> jsonObjects = JSON.parseArray(data, JSONObject.class);
                for (JSONObject jsonObject : jsonObjects) {
                    DateTime expire_time = DateUtil.parseDateTime(jsonObject.getString("expire_time"));
                    String ip = jsonObject.getString("ip");
                    Integer port = jsonObject.getInteger("port");
                    long expireSecond = (expire_time.getTime() - new Date().getTime()) / 1000;
                    jedis.set("IP缓存池注册:" + ip, JSON.toJSONString(jsonObject));
                    jedis.expire("IP缓存池注册:" + ip, BigDecimal.valueOf(expireSecond).intValue());
                }
            } else {
                int pageIndex = PreUtils.randomCommon(0, keys.size() - 1, 1)[0];
                String s1 = jedis.get(keys.stream().collect(Collectors.toList()).get(pageIndex));
                if (s1.contains("ip")) {
                    return JSON.parseObject(s1);
                }
            }
        } catch (Exception e) {
            log.error("不晓得啥错。请查看原因:{}", e.getMessage());
        }
        return null;
    }

}
