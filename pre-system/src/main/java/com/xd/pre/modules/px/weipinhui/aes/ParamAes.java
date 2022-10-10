package com.xd.pre.modules.px.weipinhui.aes;

import com.alibaba.fastjson.JSON;
import com.xd.pre.modules.px.weipinhui.CaptchaXY;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Random;

@Slf4j
public class ParamAes {


    public static void main(String[] args) throws Exception {
        MinaEdataDto minaEdataDto = convenient_login_wap_img_captcha("18408282246");
        System.out.println(JSON.toJSONString(minaEdataDto));
    }

    public static String getHexByLenth(int lenth) {
        StringBuilder sb = new StringBuilder();
        String ramdomStr = "123456789abcdef";
        for (int i = 0; i < lenth; i++) {
            int ramdomStrInex = new Random().nextInt(ramdomStr.length() - 1);
            sb.append(ramdomStr.charAt(ramdomStrInex));
        }
        return sb.toString();
    }

    public static MinaEdataDto convenient_login_wap_img_captcha(String phone) {
        try {
//            String a = "event=CONVENIENT_LOGIN_WAP_IMG_CAPTCHA&biz_data={\"contact_phone\":\"13568504862\",\"cid\":\"1658589393302_1d2da58767953dc0e149a93513499ef6\"}";
            Runtime rt = Runtime.getRuntime();
            String cid_sf = System.currentTimeMillis() + "";
            String hexByLenth = getHexByLenth(32);
            String a = String.format("event=CONVENIENT_LOGIN_WAP_IMG_CAPTCHA&biz_data={\"contact_phone\":\"%s\",\"cid\":\"%s_%s\"}",
                    phone,
                    cid_sf,
                    hexByLenth);
            String replace = a.replace("\"", "\\\"");
            log.info("re:{}", replace);
            log.info("a:{}", a);
            String commde = String.format("node /usr/local/node/model/xxx.js 1 %s", a);
            if (System.getProperty("os.name").contains("Windows")) {
                commde = String.format("node C:/Users/Administrator/Desktop/唯品会/node_modules/xxx.js 1 \"%s\" ", replace);
            }
            Process pr = rt.exec(commde);
            log.info("执行命令为msg:{}", commde);
//            pr = rt.exec("dir");//open evernote program
//            Process pr = rt.exec("D:/APP/Tim/Bin/QQScLauncher.exe");//open tim program
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream(), "GBK"));
            String line = null;
            StringBuilder lineTotal = new StringBuilder();
            while ((line = input.readLine()) != null) {
                log.info("当前登录解析数据为msg:{}", line);
                lineTotal.append(line);
            }
            int exitValue = pr.waitFor();
            String mina_edata = JSON.parseObject(lineTotal.toString()).getString("mina_edata");
            log.info("加密后mima_edatamsg:{}", mina_edata);
            MinaEdataDto minaEdataDto = new MinaEdataDto(mina_edata, String.format("%s_%s", cid_sf, hexByLenth));
            return minaEdataDto;
        } catch (Exception e) {
            log.info("当前执行js报错msg:{}", e.getMessage());
        }
        return null;
    }

    public static MinaEdataDto convenient_login_wap_after_captcha(CaptchaXY captchaXY) {
        try {
            //"scene=CONVENIENT_LOGIN_WAP_AFTER_CAPTCHA
            // &mobile=15828287465
            // &biz_data=
            // {"sid":"d18d55c24dd74f7cbb733e2a3e27666f","captchaTypePwd":"7","captchaId":"","ticket":""}"
            // "scene=CONVENIENT_LOGIN_WAP_AFTER_CAPTCHA&mobile=%s&biz_data={"sid":"%s","captchaTypePwd":"7","captchaId":"%s","ticket":"%s"}"
            Runtime rt = Runtime.getRuntime();
            String a = String.format("scene=CONVENIENT_LOGIN_WAP_AFTER_CAPTCHA&mobile=%s&biz_data={\"sid\":\"%s\",\"captchaTypePwd\":\"7\",\"captchaId\":\"%s\",\"ticket\":\"%s\"}",
                    captchaXY.getPhone(),
                    captchaXY.getCaptchaRes().getSid(),
                    captchaXY.getCaptchaRes().getCaptchaId(),
                    captchaXY.getTicket()
            );
            log.info("a:{}", a);
            String replace = a.replace("\"", "\\\"");
            log.info("re:{}", replace);
            String commde = String.format("node /usr/local/node/model/xxx.js 1 %s", a);
            if (System.getProperty("os.name").contains("Windows")) {
                commde = String.format("node C:\\Users\\Administrator\\Desktop\\唯品会\\node_modules\\xxx.js 1 \"%s\" ", replace);
            }
            log.info("执行命令为msg:{}", commde);
            Process pr = rt.exec(commde);
//            pr = rt.exec("dir");//open evernote program
//            Process pr = rt.exec("D:/APP/Tim/Bin/QQScLauncher.exe");//open tim program
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream(), "GBK"));
            String line = null;
            StringBuilder lineTotal = new StringBuilder();
            while ((line = input.readLine()) != null) {
                log.info("当前加密获取验证码数据为msg:{}", line);
                lineTotal.append(line);
            }
            int exitValue = pr.waitFor();
            String mina_edata = JSON.parseObject(lineTotal.toString()).getString("mina_edata");
            MinaEdataDto minaEdataDto = new MinaEdataDto(mina_edata, "");
            return minaEdataDto;
        } catch (Exception e) {
            log.info("当前执行js报错msg:{}", e.getMessage());
        }
        return null;
    }

}