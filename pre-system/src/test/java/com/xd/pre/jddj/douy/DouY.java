package com.xd.pre.jddj.douy;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.modules.px.douyin.buyRender.BuyRenderUtils;
import com.xd.pre.modules.px.douyin.buyRender.res.BuyRenderRoot;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.util.Map;

@Slf4j
public class DouY {
    public static void main(String[] args) {
        // String url = "https://ken.snssdk.com/order/buyRender?app_name=news_article&device_platform=android&os=android&aid=13&version_code=896&version_name=8.9.6";
//        String url = "https://ken.snssdk.com/order/buyRender?ecom_appid=7386&webcast_appid=6822&live_sdk_version=896&app_name=news_article&openlive_personal_recommend=1&device_platform=android&os=android&channel=oppo_13_64&aid=13&version_code=896&version_name=8.9.6&manifest_version_code=8960&update_version_code=89607&ab_feature=94563%2C102749&device_type=PACT00&device_brand=OPPO&language=zh&os_api=29&os_version=10";
        String url = "https://ken.snssdk.com/order/buyRender?b_type_new=3&sub_b_type=13&ecom_appid=7386&webcast_appid=6822&live_request_from_jsb=1&live_sdk_version=896&webcast_sdk_version=2070&webcast_language=zh&webcast_locale=zh_CN&webcast_gps_access=2&webcast_app_id=6822&app_name=news_article&openlive_personal_recommend=1&device_platform=android&os=android&ssmix=a&_rticket=1664102966940&cdid=4543b4be-f010-4426-8ed5-767e7a2c9aac&channel=oppo_13_64&aid=13&version_code=896&version_name=8.9.6&manifest_version_code=8960&update_version_code=89607&ab_version=668775%2C4091914%2C4394174%2C4407627%2C4689321%2C4761884%2C4778839%2C4790596%2C660830%2C4761882%2C4838690%2C668774%2C4761873%2C662176%2C4761866%2C662099%2C4761838%2C668776%2C4761874%2C1859937%2C668779%2C4761879%2C4792846%2C3540006%2C3596064&ab_group=94565%2C102755&ab_feature=94563%2C102749&resolution=1080*2200&dpi=480&device_type=PACT00&device_brand=OPPO&language=zh&os_api=29&os_version=10&ac=wifi&dq_param=0&plugin=0&client_vid=4539074%2C3194525%2C3383553%2C2827920%2C4681421&isTTWebView=0&session_id=36a1889d-b9c7-422b-8db2-8017e61b2297&host_abi=arm64-v8a&tma_jssdk_version=2.53.0&rom_version=coloros_v7.1_pact00_11_f.27&immerse_pool_type=101&iid=119169286681983&device_id=321506381413262";
        String productId = "3556357046087622442";
        String skuId = "1736502463777799";
        BuyRenderRoot buyRenderRoot = getBuyRenderRoot(url, productId, skuId);
        //1微信，2支付宝，10抖音
//        String bodyData1 = SubmitUtils.buildSubmitData(buyRenderRoot, "183.221.16.173", "13568504862", skuId, productId, 1);
        String bodyData1 = "";
        String X_SS_STUB1 = SecureUtil.md5("json_form=" + URLEncoder.encode(bodyData1)).toString().toUpperCase();
        String url1 = "https://ec.snssdk.com/order/newcreate/vtl?can_queue=1&b_type_new=3&sub_b_type=13&ecom_appid=7386&webcast_appid=6822&live_request_from_jsb=1&live_sdk_version=896&webcast_sdk_version=2070&webcast_language=zh&webcast_locale=zh_CN&webcast_gps_access=2&webcast_app_id=6822&app_name=news_article&openlive_personal_recommend=1&device_platform=android&os=android&ssmix=a&_rticket=1664083081163&cdid=4543b4be-f010-4426-8ed5-767e7a2c9aac&channel=oppo_13_64&aid=13&version_code=896&version_name=8.9.6&manifest_version_code=8960&update_version_code=89607&ab_version=668775%2C4091914%2C4394174%2C4407627%2C4689321%2C4761884%2C4778839%2C4790596%2C660830%2C4761882%2C4838690%2C668774%2C4761873%2C662176%2C4761866%2C662099%2C4761838%2C668776%2C4761874%2C1859937%2C668779%2C4761879%2C4792846%2C3540006%2C3596064&ab_group=94565%2C102755&ab_feature=94563%2C102749&resolution=1080*2200&dpi=480&device_type=PACT00&device_brand=OPPO&language=zh&os_api=29&os_version=10&ac=wifi&dq_param=0&plugin=0&client_vid=4539074%2C3194525%2C3383553%2C2827920%2C4681421&isTTWebView=0&session_id=45978c35-2594-436d-9a18-8f6a97b52cc7&host_abi=arm64-v8a&tma_jssdk_version=2.53.0&rom_version=coloros_v7.1_pact00_11_f.27&immerse_pool_type=101&iid=119169286681983&device_id=321506381413262";
        String signData1 = String.format("{\"header\": {\"X-SS-STUB\": \"%s\",\"deviceid\": \"\",\"ktoken\": \"\",\"cookie\" : \"\"},\"url\": \"%s\"}",
                X_SS_STUB1, url1
        );
        String signHt1 = HttpRequest.post("http://110.42.246.12:8191/tt1213").body(signData1).execute().body();
        log.info("msg:{}", signHt1);
        String x_gorgon1 = JSON.parseObject(signHt1).getString("x-gorgon");
        String x_khronos1 = JSON.parseObject(signHt1).getString("x-khronos");
        String tarceid = JSON.parseObject(signHt1).getString("tarceid");
        String body1 = HttpRequest.post(url1)
                .form("json_form", bodyData1)
                .header("Cookie", "d_ticket=2ea30aa765039da0cd5da48cb1a47a1f42b84; odin_tt=74824bb1c85e0b78fc4a3ed035a6663d670d4e906d5a9b7a1074624092678954ec72e2db2abb352f49302507b78a4ee6fd633744c863dc6b4650482b362f7e5f01225abdd8d85d3a200a0787ee69e5d9; odin_tt=fbc799788e48bea48c8aa7b68af23ed29178bab15cd2fc4c950f23632ccf8ff7c54ecb2c11f3159ef0adbc00a5e2993d06b0ac2b3b550c26324f34ad00d08f579d83b304e3e8830c2f15b4c7a7a27af9; n_mh=RKhIXiXn0ZGhVXqYLHo1gPmTzVBl0r5KwCdkKQjkIuo; sid_guard=936e154a11e17dd7a78293bb6d4602e6%7C1665504367%7C5184000%7CSat%2C+10-Dec-2022+16%3A06%3A07+GMT; uid_tt=b1983ebd14f7aff7327c75cee92cd7ad; uid_tt_ss=b1983ebd14f7aff7327c75cee92cd7ad; sid_tt=936e154a11e17dd7a78293bb6d4602e6; sessionid=936e154a11e17dd7a78293bb6d4602e6; sessionid_ss=936e154a11e17dd7a78293bb6d4602e6; reg-store-region=; passport_csrf_token=a2fe5fe84ab84dcbe46d1e37d001144a; passport_csrf_token_default=a2fe5fe84ab84dcbe46d1e37d001144a; install_id=154362622771982; ttreq=1$ca0e40c90f9ecca4d7c49093d774540b57da82c7")
                .header("Authorization", "Bearer act.3.VFoUyHrlFXRqSUpzzG5VY9o647bWkI9PFun65ukzx_T4Icm3N8P6y1k3z-JtcXy4EJbXks19f4UPTBIrZXgIWhjx_-JJppsbfhCeLRiuoi9pkgoHU4sRfyfft5ObVd0Ap1cue9Yfw9kW3zY9YthqYfSC6cSRtFlcw_LDFA==")
                .header("OpenId", "_0004enPLcYO_YmdkkNYGbKL6kmsNiu3bzrx")
                .header("ClientKey", "awikua6yvbqai0ht")
                .header("odin-tt", "73261b3393efd4eed429eb43a222ffd24567e10aa620bf3913567b875a5080f9341c91af36a74628f6976dafe295f7873f68a203d89a7c7166e70ec64e405915")
                .header("X-SS-REQ-TICKET", "1664083081169")
                .header("x-tt-dt", "AAA7PP535YTAB4LF5VQ6GEGA6L6TAQD6CGGT3EZFK7M7W4BMMHAVJTFJ37DH55LWAULVYDIIUDOGER4IIP3AIA4KZJ7JFGCT6DXXWXIIYYBHNIGEUUGFALSSOYI25QSYFJWBH6CDMELERRVIRACQ3DA")
                .header("sdk-version", "2")
                .header("X-Tt-Token", "008bd98afb08a175ebb2d91b6ff52c79a2008145bb8de50f956703ac153e887cee568ce94846414449163289b9c6270cf0e7b73f69e290a7f34a6c0dffacd8c5865715fdc0253fa4c73e920ba5404de399b2f269984cd6d83e8c865b6ad1f3359e080-1.0.1")
                .header("passport-sdk-version", "30858")
                .header("x-vc-bdturing-sdk-version", "2.2.1.cn")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-SS-STUB", X_SS_STUB1)
                .header("x-tt-store-region", "cn-sc")
                .header("x-tt-store-region-src", "did")
                .header("x-tt-request-tag", "s=-1;p=0")
                .header("x-tt-trace-id", tarceid)
                .header("User-Agent", "com.ss.android.article.news/8960 (Linux; U; Android 10; zh_CN; PACT00; Build/QP1A.190711.020; Cronet/TTNetVersion:68deaea9 2022-07-19 QuicVersion:12a1d5c5 2022-06-27)")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("X-Gorgon", x_gorgon1)
                .header("X-Khronos", x_khronos1)
                .execute().body();
        System.out.println(body1);
    }

    public static BuyRenderRoot getBuyRenderRoot(String url, String productId, String skuId) {
        String body = JSON.toJSONString(BuyRenderUtils.buildBuyRenderReq(productId, skuId));
        String X_SS_STUB = SecureUtil.md5("json_form=" + URLEncoder.encode(body)).toString().toUpperCase();
        String signData = String.format("{\"header\": {\"X-SS-STUB\": \"%s\",\"deviceid\": \"\",\"ktoken\": \"\",\"cookie\" : \"\"},\"url\": \"%s\"}",
                X_SS_STUB, url
        );
        String signHt = HttpRequest.post("http://110.42.246.12:8191/tt1213").body(signData).execute().body();
        log.info("msg:{}", signHt);
        String x_gorgon = JSON.parseObject(signHt).getString("x-gorgon");
        String x_khronos = JSON.parseObject(signHt).getString("x-khronos");
        Map<String, String> heads = PreUtils.buildIpMap("27.159.186.55");
        HttpRequest json_form = HttpRequest.post(url)
                .form("json_form", body);
        for (String s : heads.keySet()) {
            json_form.header(s, heads.get(s));
        }
        String douyingpreData = json_form.header("Cookie", "install_id=119169286681983;")
                //   .header("X-Argus", " VuaMaVsaJB86xESMncPQwBxB3H6S148tImXgYFikCn3oARohX6z4O4CnlY+UwlBOFKHP5lrtRUt0FOVinBgNVlstN2hk+XDO0YgY7qljBNgITCa6sBSTkNESEoA5QRH8svvlvqpLmMXSqDc0bzxVsY/YdBTYwF2s5emtuvrGdh/ouzDoQKho8oPMX3/ISDaUhVwZ/WXj3pl6XWFJZyOXzc7Rqvfa3MeaNWAKOywGMZ76Hd50DdY8Sh5PlOh2ZCJgvGoHKQEzbRhwsdhj9YJjlScq")
                // .header("X-Ladon", "GSTrDP57xlX++5h9LaDHTM6JXIU/Co+gs7TkMoVY/oCQtuB+")
                //X-Gorgon: 8404e0820000d9405aa091f604f02c5b447d6a3d8e566dd3f7e9
                //X-Khronos: 1664016695
                .header("X-Gorgon", x_gorgon)
                .header("X-Khronos", x_khronos)
                .execute().body();
        System.out.println("=========" + douyingpreData);
        return JSON.parseObject(JSON.parseObject(douyingpreData).getString("data"), BuyRenderRoot.class);
    }
}
