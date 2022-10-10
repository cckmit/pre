package com.xd.pre.modules.px.weipinhui.yezi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xd.pre.modules.px.yezijiema.YeZiUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class YeZiGetMobileDto {
    /**
     * token	是	string	登录返回的token
     * project_id	是	string	项目ID.普通项目填普通项目的ID，专属类型填写专属项目的对接码【例：12585----xxxx】
     * loop	否	string	是否过滤项目 1过滤 2不过滤 默认不过滤
     * operator	否	string	运营商 (0=默认 1=移动 2=联通 3=电信 4=实卡 5=虚卡) 可空
     * phone_num	否	string	指定取号的话 这里填要取的手机号
     * scope	否	string	指定号段 最多支持号码前5位. 例如可以为165，也可以为16511
     * address	否	string	归属地选择 例如 湖北 甘肃 不需要带省字
     * api_id	否	string	如果是开发者,此处填写你的用户ID才有收益，注意是用户ID不是登录账号
     * scope_black	否	string	排除号段最长支持7位且可以支持多个,最多支持20个号段。用逗号分隔 比如150,1511111,15522
     * creat_time	否	string	输入整数,单位/天,用来过滤上线时间的机器.比如输入7,那么获取到的手机号最少上线了7天，范围1-60
     */
    private String token;
    private String project_id;
    private String loop;
    private String operator;
    private String phone_num;
    private String scope;
    private String address;
    private String api_id;
    private String scope_black;
    private String creat_time;

    public static String getWphRandomShiKa(StringRedisTemplate redisTemplate) {
        String token = YeZiUtils.getToken(redisTemplate);
        YeZiGetMobileDto yeZiGetMobileDto = YeZiGetMobileDto.builder()
                .token(token).project_id("10388").operator("4").build();
        return getJsonStr(yeZiGetMobileDto);
    }

    public static String getWphRandom(StringRedisTemplate redisTemplate) {
        String token = YeZiUtils.getToken(redisTemplate);
        YeZiGetMobileDto yeZiGetMobileDto = YeZiGetMobileDto.builder()
                .token(token).project_id("10388").build();
        return getJsonStr(yeZiGetMobileDto);
    }

    public static String getJinRiTouTiao(StringRedisTemplate redisTemplate) {
        String token = YeZiUtils.getToken(redisTemplate);
        YeZiGetMobileDto yeZiGetMobileDto = YeZiGetMobileDto.builder()
                .token(token).project_id("10059").build();
        return getJsonStr(yeZiGetMobileDto);
    }


    public static String getZhiDingPhoneDuanXin(String phone_num,StringRedisTemplate redisTemplate) {
        String token = YeZiUtils.getToken(redisTemplate);
        YeZiGetMobileDto yeZiGetMobileDto = YeZiGetMobileDto.builder()
                .token(token).project_id("10388").phone_num(phone_num).build();
        String jsonStr = getJsonStr(yeZiGetMobileDto);
        return jsonStr;
    }

    private static String getJsonStr(YeZiGetMobileDto yeZiGetMobileDto) {
        JSONObject paramJson = JSON.parseObject(JSON.toJSONString(yeZiGetMobileDto));
        StringBuilder sb = new StringBuilder();
        for (String key : paramJson.keySet()) {
            sb.append(key + "=" + paramJson.getString(key) + "&");
        }
        String returnSf = sb.toString();
        return returnSf.substring(0, returnSf.length() - 1);
    }
}
