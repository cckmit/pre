package com.xd.pre.modules.px.weipinhui;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xd.pre.modules.px.weipinhui.baiduyun.WordsResult;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 获取token类
 */
@Slf4j
public class AuthService {

    public static void main(String[] args) {
//        getAuth();
    }

    /**
     * 获取权限token
     *
     * @return 返回示例：
     * {
     * "access_token": "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567",
     * "expires_in": 2592000
     * }
     */
    public static String getAuth(StringRedisTemplate redisTemplate) {
//        Jedis jedis = RedisDS.create().getJedis();
        // 官网获取的 API Key 更新为你注册的
//        String appid = "26763422";
//        String clientId = "qXTMjw6zpo4NYv41Dr8haaGB";
//        // 官网获取的 Secret Key 更新为你注册的
//        String clientSecret = "6rZlG34PxMzIo01x9UNyPfoliw3h7sCj";
        String appid = redisTemplate.opsForValue().get("百度云识别:appid");
        String clientId = redisTemplate.opsForValue().get("百度云识别:clientId");
        String clientSecret = redisTemplate.opsForValue().get("百度云识别:clientSecret");
        if (StrUtil.isBlank(appid) || StrUtil.isBlank(clientId) || StrUtil.isBlank(clientSecret)) {
            redisTemplate.opsForValue().set("百度云识别:appid", "26763422");
            redisTemplate.opsForValue().set("百度云识别:clientId", "qXTMjw6zpo4NYv41Dr8haaGB");
            redisTemplate.opsForValue().set("百度云识别:clientSecret", "6rZlG34PxMzIo01x9UNyPfoliw3h7sCj");
            appid = "26763422";
            clientId = "qXTMjw6zpo4NYv41Dr8haaGB";
            clientSecret = "6rZlG34PxMzIo01x9UNyPfoliw3h7sCj";
        }
        String access_token = redisTemplate.opsForValue().get("百度云access_token:" + appid);
        if (StrUtil.isNotBlank(access_token)) {
            return access_token;
        }
        log.info("获取redis中的token信息");
        access_token = getAuth(clientId, clientSecret);
        redisTemplate.opsForValue().set("百度云access_token:" + appid, access_token, 7200, TimeUnit.SECONDS);
        return access_token;
    }

    public static List<WordsResult> parseCapData(String url, String picData, StringRedisTemplate redisTemplate) {
        try {
            String access_token = AuthService.getAuth(redisTemplate);
            String baiduUrl = "https://aip.baidubce.com/rest/2.0/ocr/v1/general?access_token=" + access_token;
            OkHttpClient clientLocal = new OkHttpClient().newBuilder().followRedirects(false).build();

            FormBody.Builder builder = new FormBody.Builder();
            if (StrUtil.isNotBlank(url)) {
                builder.add("url", url);
            }
            if (StrUtil.isNotBlank(picData)) {
                builder.add("image", picData);
                builder.add("language_type", "ENG");
            }
            FormBody requestBody = builder.build();
            Request request = new Request.Builder()
                    .post(requestBody)
                    .url(baiduUrl)
                    .build();
            Response response = clientLocal.newCall(request).execute();
            String resStr = response.body().string();
            response.close();
            log.info("百度云解析qp数据为:{}", resStr);
            if (!resStr.contains("words_result_num")) {
                log.error("当前解析报错msg:{}");
            }
            List<WordsResult> words_result = JSON.parseArray(JSON.parseObject(resStr).getString("words_result"), WordsResult.class);
            return words_result;
        } catch (Exception e) {
            log.error("解析失败msg:{}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取API访问token
     * 该token有一定的有效期，需要自行管理，当失效时需重新获取.
     *
     * @param ak - 百度云官网获取的 API Key
     * @param sk - 百度云官网获取的 Secret Key
     * @return assess_token 示例：
     * "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567"
     */
    public static String getAuth(String ak, String sk) {
        // 获取token地址
        String authHost = "https://aip.baidubce.com/oauth/2.0/token?";
        String getAccessTokenUrl = authHost
                // 1. grant_type为固定参数
                + "grant_type=client_credentials"
                // 2. 官网获取的 API Key
                + "&client_id=" + ak
                // 3. 官网获取的 Secret Key
                + "&client_secret=" + sk;
        try {
            URL realUrl = new URL(getAccessTokenUrl);
            // 打开和URL之间的连接
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            // 定义 BufferedReader输入流来读取URL的响应
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String result = "";
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            /**
             * 返回结果示例
             */
            JSONObject jsonObject = JSON.parseObject(result);
            String access_token = jsonObject.getString("access_token");
            log.info("access_token:{}", access_token);
            return access_token;
        } catch (Exception e) {
            System.err.printf("获取token失败！");
            e.printStackTrace(System.err);
        }
        return null;
    }

}