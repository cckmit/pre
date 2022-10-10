package com.xd.pre.modules.px.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.modules.px.vo.tmpvo.coupon.CouPonParam;
import com.xd.pre.modules.px.vo.tmpvo.coupon.LingJuanCoupon;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class LingJuanDemo {
    public static void main(String[] args) throws Exception {
        String ck = "shshshfpa=fda9bed2-c22b-9e6b-4e9f-478911e8d8df-1647874052; shshshfpb=urmgXyOXIwN4Uk9Mvtsug9A; jcap_dvzw_fp=lrO_mEHYtZ5sFfoXLXKlRBWivP14ASdsA4bY0NI6tNQZXk1kDjSrFHOaWP-i5Yk_mcB71Q==; abtest=20220406165928262_68; whwswswws=; ipLoc-djd=22-1930-50948-57092; user-key=969f9442-fc02-4c20-9e45-2d8a99c159e1; __jdu=16522775120181288055253; unpl=JF8EAKpnNSttX0pWVRwET0IRTg4BW1UPTh8HamYCXVhYTlMNHANJRkd7XlVdXhRKEx9vZBRXXVNLUQ4ZACsQEUtcVV1tC0oXBmlnDFNdWHsEaxkBEnwQSFpXWW04SBczblcFU1lbSFIDHwIYExBCXVdXVAlPEwBrZTVkXVBMZDUrBxsTEU9YVF9UCnsWM25XTjpcFUtTARgBHRQUS15VXlQISB4KbmMBV1lae1U1GA; __jdv=76161171|jd.ss8899888.com|t_2014718611_a_328_12626|jingfen|603a67ea04bd49649540684146960cef|1652361274464; webp=1; mba_muid=16522775120181288055253; visitkey=56724226832606876; equipmentId=2XRK4PH7YTECS7DYZNDH764SWHELI2J2COCDRU357GLIV6TKL63PRAESJZVNTNB53M6BZABAON74E2QQEOCZO745CY; sc_width=390; wxa_level=1; qd_uid=3429e270-9dd4-47fc-abd9-485c70004968; qd_fs=1653063416335; qd_ls=1653063416335; kplTitleShow=1; wq_addr=4551928385%7C1_72_2819_0%7C%u5317%u4EAC_%u671D%u9633%u533A_%u4E09%u73AF%u5230%u56DB%u73AF%u4E4B%u95F4_%7C%u5317%u4EAC%u671D%u9633%u533A%u4E09%u73AF%u5230%u56DB%u73AF%u4E4B%u95F4%7C116.444%2C39.9219; jdAddrId=1_72_2819_0; regionAddress=1%2C72%2C2819%2C0; commonAddress=4551928385; jdAddrName=%u5317%u4EAC_%u671D%u9633%u533A_%u4E09%u73AF%u5230%u56DB%u73AF%u4E4B%u95F4_; mitemAddrId=1_72_2819_0; mitemAddrName=%u5317%u4EAC%u671D%u9633%u533A%u4E09%u73AF%u5230%u56DB%u73AF%u4E4B%u95F4; cartNum=3; wq_auth_token=E05D773ED557F11987AC2EDE230C2B2A0D35736FB4BAFC4803CA6A8AE3BCE833; qd_ts=1653066258297; qd_sq=2; mt_xid=V2_52007VwMVVl5bVFwbSRpdBW4DEVtUWVZeHE0bbFBgUEFUWg1VRhYaSVsZYgQRW0EIVAoWVUwMUWcEElEKCFcJHHkaXQZkHxNWQVtQSx5JEl8BbAMRYl9oUmoYThFdBG4KG1NfaFJTG04%3D; sbx_hot_h=null; cid=9; deviceVersion=604.1; deviceOSVersion=13.2.3; deviceOS=ios; deviceName=Safari; warehistory=\"10026503885976,11183445154,13138170874,13138170874,13138170874,13138170874,13138170874,10023403480808,10023403480808,10023403480808,10023403480808,11183343342,11183343342,11183343342,11183343342,11183343342,10022039398507,11183343342,100029826472,100015596040,\"; autoOpenApp_downCloseDate_autoOpenApp_autoPromptly=1653392003414_1; PPRD_P=UUID.16522775120181288055253-LOGID.1653392003481.469273394; sk_history=10026503885976%2C11183445154%2C13138170874%2C10023403480808%2C11183343342%2C10022039398507%2C100015596040%2C11183368356%2C; retina=1; _gia_s_local_fingerprint=16417ac2f49e12dfb3ba1ad2876b5766; fingerprint=16417ac2f49e12dfb3ba1ad2876b5766; USER_FLAG_CHECK=a8aae75c7152b397a098240db1b1f9cf; __jdc=122270672; _gia_s_e_joint={\"eid\":\"2XRK4PH7YTECS7DYZNDH764SWHELI2J2COCDRU357GLIV6TKL63PRAESJZVNTNB53M6BZABAON74E2QQEOCZO745CY\",\"ma\":\"\",\"im\":\"\",\"os\":\"Mac OS X (iPhone)\",\"ip\":\"183.221.17.76\",\"ia\":\"\",\"uu\":\"\",\"at\":\"6\"}; appCode=ms0ca95114; share_cpin=; share_open_id=; share_gpin=; shareChannel=; source_module=; erp=; 3AB9D23F7A4B3C9B=2XRK4PH7YTECS7DYZNDH764SWHELI2J2COCDRU357GLIV6TKL63PRAESJZVNTNB53M6BZABAON74E2QQEOCZO745CY; jxsid=16534970258422319882; shshshfp=fcaa3a94644c7850355f99eff54a6cc0; channel=; jxsid_s_u=https%3A//coupon.m.jd.com/center/getCouponCenter.action; downloadAppPlugIn_downCloseDate=1653541744377_1800000; __jda=122270672.16522775120181288055253.1652277512.1653538682.1653541829.34; autoOpenApp_downCloseDate_auto=1653541830579_1800000; pt_key=app_openAAJijw0BADCY5K1Rqr00ATcdaQ_shElDoCsu9SuhTYXZ_CJQQgQ7-E26oMyYEhvSDd55AB-oc6k; pt_pin=jd_ADyLEsPUnuAe; pwdt_id=jd_ADyLEsPUnuAe; sid=1fbcc0dd2ed7bede489eeb15fc602a8w; mobilev=touch; autoOpenApp_downCloseDate_jd_homePage=1653542150153_1; wqmnx1=MDEyNjM5MnRwY3JvYTYyeiAgbjJNIEsuTGVzLi9hLjE2ZmZCVkNVKA%3D%3D; __jdb=122270672.12.16522775120181288055253|34.1653541829; mba_sid=16535417443807653545229466236.12; __wga=1653542164428.1653541830438.1653537319034.1652361303824.5.22; jxsid_s_t=1653542164521; shshshsID=3df662dd3f1f384dfc549d7eeab92f23_5_1653542164712; __jd_ref_cls=MProductCoupon_Specialcoupon";
//        CheckIsLingJuanMain(ck);
    }

    public static Boolean CheckIsLingJuanMain(String ck,Proxy proxy) throws Exception {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy).build();
            Request request = new Request.Builder()
                    .url("https://coupon.m.jd.com/center/getCouponCenter.action")
                    .get()
                    .addHeader("cookie", ck)
                    .addHeader("referer", "https://coupon.m.jd.com/center/getCouponCenter.action")
                    .addHeader("user-agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1 Edg/101.0.4951.64")
                    .addHeader("cache-control", "no-cache")
                    .build();
            Response response = client.newCall(request).execute();
            String body = response.body().string();
            String P_COMM = "(?<=<script>var _recommendCoupon =).*?(?=(</script>|$))";
            Pattern pattern = Pattern.compile(P_COMM);
            Matcher matcher = pattern.matcher(body);
            List<String> juanList = new ArrayList<>();
            if (matcher.find()) {
                String group = matcher.group();
                juanList.add(group);
            }
            if (CollUtil.isEmpty(juanList)) {
                return false;
            }
            String couponStr = juanList.get(0);
            if (StrUtil.isBlank(couponStr) || !couponStr.contains("ckey") || !couponStr.contains("actId")) {
                return false;
            }
            String result = JSON.parseObject(couponStr).getString("result");
            String couponList = JSON.parseObject(result).getString("couponList");
            List<LingJuanCoupon> lingJuanCoupons = JSON.parseArray(couponList, LingJuanCoupon.class);
//        System.out.println(JSON.toJSONString(lingJuanCoupons));
            if (CollUtil.isEmpty(lingJuanCoupons)) {
                return false;
            }
            List<CouPonParam> couPonParams = new ArrayList<>();

            for (LingJuanCoupon lingJuanCoupon : lingJuanCoupons) {
                if (StrUtil.isNotBlank(lingJuanCoupon.getActId()) && StrUtil.isNotBlank(lingJuanCoupon.getCkey())) {
                    CouPonParam build = CouPonParam.builder().actId(lingJuanCoupon.getActId()).batchId(lingJuanCoupon.getBatchId()).ckey(lingJuanCoupon.getCkey()).build();
                    couPonParams.add(build);
                }
            }
            if (CollUtil.isEmpty(couPonParams)) {
                return false;
            }
            List<Boolean> isCheckLingJuan = new ArrayList<>();

            for (CouPonParam couPonParam : couPonParams) {
                Boolean lingJuan = isLingJuan(ck, couPonParam,proxy);
                isCheckLingJuan.add(lingJuan);
                if (lingJuan) {
                    log.info("msg:当前账号可以领卷msg:{}", PreUtils.get_pt_pin(ck));
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("领卷错误msg:{}", e.getMessage());
        }
        return false;
    }

    private static Boolean isLingJuan(String ck, CouPonParam couPonParam, Proxy proxy) throws Exception {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy).build();
            String param = String.format("batchid=%s&coupon=%s,%s", couPonParam.getBatchId(), couPonParam.getActId(), couPonParam.getCkey());
            Request request = new Request.Builder()
                    .url("https://s.m.jd.com/activemcenter/mcouponcenter/receivecoupon?" + param)
                    .get()
                    .addHeader("cookie", ck)
                    .addHeader("referer", "https://coupon.m.jd.com/")
                    .addHeader("user-agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1 Edg/101.0.4951.64")
                    .build();
            Response response = client.newCall(request).execute();
            String result = response.body().string();
            response.close();
            if (StrUtil.isNotBlank(result) && (result.contains("已经参加过此活动") || result.contains("领券成功"))) {
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("当前领卷错误msg:{},", e.getMessage());
        }
        return false;
    }

}
