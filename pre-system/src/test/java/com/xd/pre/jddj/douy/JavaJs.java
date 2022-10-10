package com.xd.pre.jddj.douy;

import com.xd.pre.modules.px.weipinhui.yezi.YeZiGetMobileDto;
import com.xd.pre.modules.px.yezijiema.YeZiUtils;
import com.xd.pre.pcScan.Demo;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class JavaJs {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void test2() throws Exception {
        YeZiUtils.free_mobile("17038166527", redisTemplate);
    }

    @Test
    public void test1() {
        Map<String, String> ipAndPort = Douyin3.getIpAndPort();
        OkHttpClient client = Demo.getOkHttpClient(ipAndPort.get("ip"), Integer.valueOf(ipAndPort.get("port")));
        for (int i = 0; i < 100; i++) {
            try {
                if (i % 5 == 0) {
                    ipAndPort = Douyin3.getIpAndPort();
                    client = Demo.getOkHttpClient(ipAndPort.get("ip"), Integer.valueOf(ipAndPort.get("port")));
                }
                String phone = YeZiUtils.get_mobileByDto(YeZiGetMobileDto.getJinRiTouTiao(redisTemplate), redisTemplate);
                YeZiUtils.free_mobile(phone, redisTemplate);
        /*        String mobileBase64 = initialToolbarSDK(phone);
                RequestBody requestBody = new FormBody.Builder()
                        .add("mix_mode", "1")
                        .add("type", "31")
                        .add("fixed_mix_mode", "1")
                        .add("mobile", mobileBase64)
                        .build();
                Request request = new okhttp3.Request.Builder()
                        .url("https://sso.toutiao.com/send_activation_code/v2/?aid=24&account_sdk_source=sso&language=zh")
                        .post(requestBody)
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .build();
                Response response = client.newCall(request).execute();
                String errorMsg = response.body().string();
                response.close();*/
                String errorMsg = Douycheck.check(client, phone);
                if (errorMsg.contains("访问太频繁")) {
                    continue;
                }
                log.info("当前手机msg:{},错误信息msg:{}", phone, errorMsg);
                YeZiUtils.free_mobile(phone, redisTemplate);
            } catch (Exception e) {

            }
        }
    }

    public static void main(String[] args) throws Exception {
//       C:/Users/Administrator/Desktop/抖音/jinritoutiaojs.js
        //2e3d3325343630333d3035313d3337
        initialToolbarSDK("13568504862");
    }

    /**
     * @param //    jsFile C:/Users/Administrator/Desktop/抖音/jinritoutiaojs.js
     * @param phone +86 17023985721
     */
    public static String initialToolbarSDK(String phone) {
        String jsFile = "C:/Users/Administrator/Desktop/抖音/jinritoutiaojs.js";
        String scriptResult = "";
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        try {
            //文件路径设置成绝对路径即可
            FileInputStream fileInputStream = new FileInputStream(new File(jsFile));
            Reader scriptReader = new InputStreamReader(fileInputStream, "utf-8");
            engine.eval(scriptReader);
            Invocable invocable = (Invocable) engine;
            scriptResult = (String) invocable.invokeFunction("phoneMethod", "+86" + phone);
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("当前返回的数据msg:{}", scriptResult);
        return scriptResult;
    }
}
