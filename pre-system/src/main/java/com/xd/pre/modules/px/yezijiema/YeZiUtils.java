package com.xd.pre.modules.px.yezijiema;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xd.pre.modules.px.weipinhui.yezi.YeZiGetMobileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

@Slf4j
public class YeZiUtils {
    public static void main(String[] args) throws Exception {
//        System.out.println(getToken());
//        String wphRandomShiKa = YeZiGetMobileDto.getWphRandomShiKa();
//        String phone_num = get_mobileByDto(wphRandomShiKa);
//        System.out.println(phone_num);
//        YeZiUtils yeZiUtils = new YeZiUtils();
//        String mobileByDto = yeZiUtils.get_message(phone_num);
//        Boolean free_mobile = YeZiUtils.free_mobile(phone_num);
    }

    public static final String bashUrl = "http://api.sqhyw.net:81";

    public static String getToken(StringRedisTemplate redisTemplate) {
//        Jedis jedis = RedisDS.create().getJedis();
        //String username = jedis.get("接码平台:账号");
        //String password = jedis.get("接码平台:密码");

        String token = redisTemplate.opsForValue().get("接码平台:token");
        if (StrUtil.isNotBlank(token)) {
            token = JSON.parseObject(token).getString("token");
            return token;
        }
        log.info("开始登录获取token");
        String username = redisTemplate.opsForValue().get("接码平台:账号");
        String password = redisTemplate.opsForValue().get("接码平台:密码");
        String loginStr = HttpUtil.get(bashUrl + String.format("/api/logins?username=%s&password=%s", username, password));
        log.info("登录返回token信息msg:{}", loginStr);
        if (StrUtil.isBlank(loginStr) || !loginStr.contains("登录成功")) {
            log.info("登录失败，请查看日志msg:{}", loginStr);
            return null;
        }
        token = JSON.parseObject(loginStr).getString("token");
        redisTemplate.opsForValue().set("接码平台:token", loginStr, 7200, TimeUnit.SECONDS);
        return token;
    }

    public static String get_mobileByDto(String yeziStr, StringRedisTemplate redisTemplate) {
//        Jedis jedis = RedisDS.create().getJedis();
        String sufNum = redisTemplate.opsForValue().get("接码平台:可以获取手机号数");
        if (StrUtil.isNotBlank(sufNum) && Integer.valueOf(sufNum) <= 10) {
            log.info("当前接码过多。请释放号码");
            return null;
        }
        String mobileStr = HttpUtil.get(bashUrl + "/api/get_mobile?" + yeziStr);
        log.info("获取手机号码返回值msg:{}", mobileStr);
        if (!mobileStr.contains("ok")) {
            log.error("获取短信报错，请查看日志");
        }
        JSONObject mobileJson = JSON.parseObject(mobileStr);
        String num = mobileJson.getString("1分钟内剩余取卡数:");
        redisTemplate.opsForValue().set("接码平台:可以获取手机号数", num, 60, TimeUnit.SECONDS);
        return mobileJson.getString("mobile");
    }

    public String get_message(String phone_num, StringRedisTemplate redisTemplate) {
        try {
            String zhiDingPhoneDuanXin = YeZiGetMobileDto.getZhiDingPhoneDuanXin(phone_num, redisTemplate);
            for (int i = 0; i < 20; i++) {
                String mobileStr = HttpUtil.get(bashUrl + "/api/get_message?" + zhiDingPhoneDuanXin);
                log.info("当前获取短信为msg:{}", mobileStr);
                if (!mobileStr.contains("code")) {
                    log.info("等待msg:phone，{}，index:{}", phone_num, i);
//                    Thread.sleep(2000);
//                    continue;
                    return null;
                } else {
                    String code = JSON.parseObject(mobileStr).getString("code");
                    log.info("返回的验证码为msg:{}", code);
                    return code;
                }
            }
            log.info("开始释放短信");
//            Boolean freeMobile = free_mobile(phone_num, redisTemplate);
//            log.info("结束释放短信，释放结果msg:{}", freeMobile);
            log.error("当前手机没有获取到短信释放");
        } catch (Exception e) {
            log.error("获取短信报错msg:{}", e.getMessage());
        }
        return null;
    }

    /**
     * 释放手机号
     *
     * @param phone_num
     * @return
     */
    public static Boolean free_mobile(String phone_num, StringRedisTemplate redisTemplate) {
        String zhiDingPhoneDuanXin = YeZiGetMobileDto.getZhiDingPhoneDuanXin(phone_num, redisTemplate);
        String mobileStr = HttpUtil.get(bashUrl + "/api/free_mobile?" + zhiDingPhoneDuanXin);
        log.info("释放短信msg:{}", mobileStr);
        if (StrUtil.isNotBlank(mobileStr) && mobileStr.contains("ok")) {
            return true;
        }
        return false;

    }

}
