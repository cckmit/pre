package com.xd.pre.modules.px.service;

import cn.hutool.core.util.StrUtil;
import com.xd.pre.common.utils.px.PreUtils;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.List;

public class LocalCookieJar implements CookieJar {


    public LocalCookieJar(String ck) {
        String[] split1 = ck.split(";");
        for (String s : split1) {
            if (StrUtil.isNotBlank(s) && s.contains("=")) {
                String[] split = s.split("=");
                if(split.length==2){
                    Cookie cookie = new Cookie.Builder()
                            .domain("jd.com").name(split[0]).value(split[1])
                            .build();
                    this.cookies.add(cookie);
                }
            }
        }
    }

    List<Cookie>  cookies = new ArrayList<>();

    public LocalCookieJar() {
        Cookie cookie1 = new Cookie.Builder()
                .domain("jd.com").name("__jda").value("122270672.16489691932711968289315.1648969193.1649258974.1649264186.14")
                .build();
        Cookie cookie3 = new Cookie.Builder()
                .domain("jd.com")
                .name("__jdc")
                .value("122270672")
                .build();
        Cookie cookie4 = new Cookie.Builder()
                .domain("jd.com")
                .name("unpl")
                .value(PreUtils.getRandomString(244))
                .build();
        Cookie cookie5 = new Cookie.Builder()
                .domain("jd.com")
                .name("retina")
                .value("1")
                .build();
        Cookie cookie6 = new Cookie.Builder()
                .domain("jd.com")
                .name("cid")
                .value("9")
                .build();
        Cookie cookie7 = new Cookie.Builder()
                .domain("jd.com")
                .name("webp")
                .value("1")
                .build();
        Cookie cookie8 = new Cookie.Builder()
                .domain("jd.com")
                .name("__jdv")
                .value("122270672|baidu-search|t_262767352_baidusearch|cpc|162667731984_0_78d8a6d3fd884766a522ef0a53205657|1648969811712")
                .build();
        Cookie cookie9 = new Cookie.Builder()
                .domain("jd.com")
                .name("visitkey")
                .value("14809670621604248")
                .build();
        Cookie cookie10 = new Cookie.Builder()
                .domain("jd.com")
                .name("shshshfpa")
                .value("285d9d5b-bc61-7af0-4de1-f11054cedade-1648969822")
                .build();
        Cookie cookie11 = new Cookie.Builder()
                .domain("jd.com")
                .name("sc_width")
                .value("360")
                .build();
        Cookie cookie12 = new Cookie.Builder()
                .domain("jd.com")
                .name("shshshfpb")
                .value("jogQgmSAFP1mkeVpxCqgnNw")
                .build();
        Cookie cookie13 = new Cookie.Builder()
                .domain("jd.com")
                .name("equipmentId")
                .value("MXGEYPETFL4XRT5VKV7ALLETBD6LKVNWNIB77XKN4DGKKOAJHG35TGE7K75AUILTIQYJ676HS64AD5IOLNOGH6MAEY")
                .build();
/*        String fingerprint = PreUtils.getRandomString("ea706beadd3dc4d4bb3f7d965420060e".length());
        Cookie cookie14 = new Cookie.Builder()
                .domain("jd.com")
                .name("fingerprint")
                .value(fingerprint)
                .build();*/
        Cookie cookie15 = new Cookie.Builder()
                .domain("jd.com")
                .name("deviceVersion")
                .value("14.8.17")
                .build();
        Cookie cookie16 = new Cookie.Builder()
                .domain("jd.com")
                .name("deviceOS")
                .value("android")
                .build();
        Cookie cookie17 = new Cookie.Builder()
                .domain("jd.com")
                .name("deviceOSVersion")
                .value("10")
                .build();
        Cookie cookie18 = new Cookie.Builder()
                .domain("jd.com")
                .name("deviceName")
                .value("Miui")
                .build();
        Cookie cookie19 = new Cookie.Builder()
                .domain("jd.com")
                .name("jcap_dvzw_fp")
                .value(PreUtils.getRandomString("fh-5-kqMMsf6o-ERCeLudQkJrk3cz9UcssypnWIAaVanUYnbqmVOPkTOCilEaU6V8qrgEg==".length()))
                .build();
        Cookie cookie20 = new Cookie.Builder()
                .domain("jd.com")
                .name("pt_token")
                .value(PreUtils.getRandomString("5m4c22sg".length()))
                .build();
        Cookie cookie21 = new Cookie.Builder()
                .domain("jd.com")
                .name("sfstoken")
                .value(PreUtils.getRandomString(92))
                .build();
        Cookie cookie22 = new Cookie.Builder()
                .domain("jd.com")
                .name("abtest")
                .value(PreUtils.getRandomString("20220403161942573_43".length()))
                .build();
        Cookie cookie23 = new Cookie.Builder()
                .domain("jd.com")
                .name("jxsid")
                .value("16492509914688387984")
                .build();
        Cookie cookie24 = new Cookie.Builder()
                .domain("jd.com")
                .name("PPRD_P")
                .value("UUID.16489691932711968289315")
                .build();
        Cookie cookie25 = new Cookie.Builder()
                .domain("jd.com")
                .name("USER_FLAG_CHECK")
                .value("137ffcc3153fc65de29c094cb98ea1ba")
                .build();
        Cookie cookie26 = new Cookie.Builder()
                .domain("jd.com")
                .name("shshshfp")
                .value("2e5bb1872a65484547998a4263e2158b")
                .build();
        Cookie cookie27 = new Cookie.Builder()
                .domain("jd.com")
                .name("autoOpenApp_downCloseDate_jd_homePage")
                .value("1649259141103_1")
                .build();
        Cookie cookie28 = new Cookie.Builder()
                .domain("jd.com")
                .name("autoOpenApp_downCloseDate_auto")
                .value("1649259158495_1800000")
                .build();
        Cookie cookie29 = new Cookie.Builder()
                .domain("jd.com")
                .name("__wga")
                .value("1649259160632.1649258999585.1649250993813.1648969821162.7.4")
                .build();
        Cookie cookie30 = new Cookie.Builder()
                .domain("jd.com")
                .name("jxsid_s_t")
                .value("1649259160974")
                .build();
        Cookie cookie31 = new Cookie.Builder()
                .domain("jd.com")
                .name("jxsid_s_u")
                .value("https://wqs.jd.com/order/n_detail_jdm.shtml")
                .build();
     /*   Cookie cookie32 = new Cookie.Builder()
                .domain("jd.com")
                .name("_gia_s_local_fingerprint")
                .value(fingerprint)
                .build();*/
    /*    Cookie cookie33 = new Cookie.Builder()
                .domain("jd.com")
                .name("_gia_s_e_joint")
                .value(String.format("{\"eid\":\"%s\",\"ma\":\"\",\"im\":\"\",\"os\":\"Android 1.x\",\"ip\":\"117.137.62.83\",\"ia\":\"\",\"uu\":\"\",\"at\":\"6\"}", PreUtils.getRandomString("MXGEYPETFL4XRT5VKV7ALLETBD6LKVNWNIB77XKN4DGKKOAJHG35TGE7K75AUILTIQYJ676HS64AD5IOLNOGH6MAEY".length())))
                .build();*/
        Cookie cookie34 = new Cookie.Builder()
                .domain("jd.com")
                .name("3AB9D23F7A4B3C9B")
                .value("MXGEYPETFL4XRT5VKV7ALLETBD6LKVNWNIB77XKN4DGKKOAJHG35TGE7K75AUILTIQYJ676HS64AD5IOLNOGH6MAEY")
                .build();
        Cookie cookie35 = new Cookie.Builder()
                .domain("jd.com")
                .name("__jdb")
                .value("122270672.4.16489691932711968289315|14.1649264186")
                .build();
        Cookie cookie36 = new Cookie.Builder()
                .domain("jd.com")
                .name("mba_sid")
                .value("16492641866649112177360776582.4")
                .build();
        Cookie cookie37 = new Cookie.Builder()
                .domain("jd.com")
                .name("__jd_ref_cls")
                .value("MGameChargeNewOrder_BuyNow")
                .build();
/*        Cookie cookie38 = new Cookie.Builder()
                .domain("jd.com")
                .name("")
                .value("")
                .build();*/
        ArrayList<Cookie> list = new ArrayList<>();
        list.add(cookie1);
        list.add(cookie3);
        list.add(cookie4);
        list.add(cookie5);
        list.add(cookie6);
        list.add(cookie7);
        list.add(cookie8);
        list.add(cookie9);
        list.add(cookie10);
        list.add(cookie11);
        list.add(cookie12);
        list.add(cookie13);
//        list.add(cookie14);
        list.add(cookie15);
        list.add(cookie16);
        list.add(cookie17);
        list.add(cookie18);
        list.add(cookie19);
        list.add(cookie20);
        list.add(cookie21);
        list.add(cookie22);
        list.add(cookie23);
        list.add(cookie24);
        list.add(cookie25);
        list.add(cookie26);
        list.add(cookie27);
        list.add(cookie28);
        list.add(cookie29);
        list.add(cookie30);
        list.add(cookie31);
//        list.add(cookie32);
//        list.add(cookie33);
        list.add(cookie34);
        list.add(cookie35);
        list.add(cookie36);
        list.add(cookie37);
        this.cookies = list;
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl arg0) {
        if (cookies != null)
            return cookies;
        return new ArrayList<Cookie>();
    }

    @Override
    public void saveFromResponse(HttpUrl arg0, List<Cookie> cookies) {
        this.cookies.addAll(cookies);
    }
}
