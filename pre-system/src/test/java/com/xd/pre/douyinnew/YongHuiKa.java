package com.xd.pre.douyinnew;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.jddj.douy.Douyin3;
import com.xd.pre.modules.px.douyin.buyRender.BuyRenderParamDto;
import com.xd.pre.modules.px.douyin.buyRender.res.BuyRenderRoot;
import com.xd.pre.modules.px.douyin.submit.SubmitUtils;
import com.xd.pre.pcScan.Demo;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.net.URLEncoder;
import java.util.Map;

@Slf4j
public class YongHuiKa {

    public static void main(String[] args) throws Exception {
        {
    /*    String device_id = "321506381413262";
        String iid = "119169286681983";*/

/*        String device_id = "46408460323";
        String iid = "22634572655";*/

/*            String device_id = "2538093503847412";
            String iid = "2124682890589959";*/
/*        String device_id = "136765450490936";
        String iid = "4024638567632376";*/
/*        String device_id = "611755040972302";
        String iid = "2124683041579725";*/
/*        String device_id = "1245073739350861";
        String iid = "2195051786540648";*/
        String device_id = "418240990035975";
        String iid = "1086744060517486";
            /**
             * {"server_time":1664424701,"device_id":3672795407987400,"install_id":1825615873587934,"device_id_str":"3672795407987400","install_id_str":"1825615873587934","new_user":1,"device_token":"AAAYKT2MDFAOSIWBNTT7SGY3VPLT24QSRFGSPEXPEAFGNISSRLIXGZAI3QHOJ3QMZIR33FEMX3RFD5EFRMUTAHMPGRVLR3DFCAU5XK5UCVOY6U6HGKFJ27TJLOCLI"}
             * {"device_id_str":"594162850992494","install_id_str":"4270929735074733","new_user":1,"device_token":"AAAV6RYW5VMV4TBVHCXI7ZUPYNYT572B2VYVM5ONEEVHW6HKB4GLYPPMZFH5BB4D2Q4PCH7M4E3FLRPV7ERA6NQ44EM5DQ6QJMMR2FUINWW7USNHQYOWLB3ZQN24M","server_time":1664424808,"device_id":594162850992494,"install_id":4270929735074733}
             * {"server_time":1664424915,"device_id":418240990035975,"install_id":1086744060517486,"device_id_str":"418240990035975","install_id_str":"1086744060517486","new_user":1,"device_token":"AAARDBWSSXIWXGE6NSJUNZQBQHGEYKKIQYWK435XB7GSXCWA26YWGFB7JAPJCVSBDDPOMHM4ZSSYSNTBKUP4PGKLE5GWY7B2YB4Q"}
             *{"server_time":1664425577,"device_id":3971862574672087,"install_id":189542575916536,"device_id_str":"3971862574672087","install_id_str":"189542575916536","new_user":1,"device_token":"AAAUHTD4PE4R4H434N6WVPO4TATHKVHHBJV2XANTRVK3WUSWLC2AHCWFXOE7UO3TZZKGYHD6J6UVG5C6C6C5WY4BTDTA23CPMVXQ"}
             *{""server_time":1664425667,"device_id":2670040807115277,"install_id":2634856436075208,"device_id_str":"2670040807115277","install_id_str":"2634856436075208","new_user":1}
             * {"device_id_str":"611755040972302","install_id_str":"2124683041579725","new_user":1,"device_token":"AAAY5P42IHAK5WV2SDB7MS5JY6XZ4MKK7S74JPHIWRRBP7PX3OS723X56MDCWNJSJ6NEL36M2NJAOB3ITXKHMB545SODPSPOS7YH72DMUFCH3B42GYUW6RKAVAWKI","server_time":1664425784,"device_id":611755040972302,"install_id":2124683041579725}
             *{"server_time":1664426072,"device_id":1245073739350861,"install_id":2195051786540648,"device_id_str":"1245073739350861","install_id_str":"2195051786540648","new_user":1,"device_token":"AAA2PZDSPO73NJ27LMWSFPWLIS3VWN7S2UM4X4WJKT3PSCYVFMYPDY3PLOIQ66UFZMSVAW7ZM2TW7D5FEJAD6XRCLVMBUVNWZTVKVPAHX3K7L3RB5YIGE22RRVUCC"}
             */
            Integer payType = 2;
            String payIp = "183.221.16.173";
            String ck = "install_id=119169286681983; ttreq=1$c937432add1ac5543b40dc8b95cb769bf024bf3a; passport_csrf_token=dc084fdfd9182b2006ac015d23d5094e; passport_csrf_token_default=dc084fdfd9182b2006ac015d23d5094e; d_ticket=5d8498d9c5c57a18f23083f8b948b45743690; multi_sids=659356656346136%3A140a336dd81551eaa30bc0e9e8d336fd; odin_tt=31cfa0089dd41156d7ef15dc965652aac3992c75cf21a7028e7de03cc06bad99000ef807e63e702274ccb12447d4a5d88eb3277edf255dc0b5f5e277b611155de832de5a7ffbb6acd70c6d5f7e1ad8f9; n_mh=8nysT__BxDL_VpPZTRMYKZZSN1pywPhZ9o63MSmzGLg; passport_assist_user=CkCRydX49tKRiP6NfppL8EZXqhP7I0lHXjcq-1NuFi9tetbHhO7j8WgKWcNY0u1c2_pwQmIxWsLy25zu5vuCS4y2GkgKPEawcjcdGGFhQ7XJU9Cvcme37ad7_x2LoXTiOHQl20bPqoQm-Xexq_YwQPA0X1fytaQzn-aCrETNNkRTDBDpip0NGImv1lQiAQMRQLcc; sid_guard=140a336dd81551eaa30bc0e9e8d336fd%7C1664383681%7C5183999%7CSun%2C+27-Nov-2022+16%3A48%3A00+GMT; uid_tt=1e4686eabe61b69fd57f1db3639b39b0; uid_tt_ss=1e4686eabe61b69fd57f1db3639b39b0; sid_tt=140a336dd81551eaa30bc0e9e8d336fd; sessionid=140a336dd81551eaa30bc0e9e8d336fd; sessionid_ss=140a336dd81551eaa30bc0e9e8d336fd; msToken=DefrbKNjA4krIh7tp5KF_ZXXM1__4BIGoe2_r-2pbIFokQpdlsAe8eodr9epNPRS43Yu3Wpkh4HktFYRO-i2ASuuPCj8e7LOFmIy0hm1yEw=";
//        String ck = "msToken=ozB0lzQlZSfhMO89FyaM9bIFkH2WlQSp5QsrNAB5K4JWCzI7XYXbHX9nRsbDovL60oL057eXVQPvL8hHRCFDAJHWNq5FtmjocWmsJrizJK0=; __ac_signature=_02B4Z6wo00f01q5Y1bAAAIDDJRNOU8iwKl6ueNEAAMiu25; tt_webid=7148348565085308423; ttcid=f5809ef849264c0794131bec5745f7cf24; local_city_cache=%E8%AE%B8%E6%98%8C; csrftoken=9832f0c55d57ce5944f19282e117eb29; s_v_web_id=verify_l8ldokao_uzOgirXl_kcRt_4LEP_9xYG_kddHuz8SSB4v; _tea_utm_cache_24=undefined; MONITOR_WEB_ID=2f58f1fc-395f-490b-b74b-aaeff0d42309; passport_csrf_token=49fb2e94e91a1bb46ea22b9953d85a33; passport_csrf_token_default=49fb2e94e91a1bb46ea22b9953d85a33; n_mh=zZgm57pXJ9eU1op-6-gPBKq0dTvW4KJntl42JmCxz_c; sso_auth_status=34ce743ea769dc8c3205895a69c7ae7f%2Cdc238a694f615cf1b2fd0d8fb9a9b1ea; sso_auth_status_ss=34ce743ea769dc8c3205895a69c7ae7f%2Cdc238a694f615cf1b2fd0d8fb9a9b1ea; sso_uid_tt=868d09e198c8214a51d906ad86cc53bf; sso_uid_tt_ss=868d09e198c8214a51d906ad86cc53bf; toutiao_sso_user=e5860adfdff1f820b9a58c0deae7c10b; toutiao_sso_user_ss=e5860adfdff1f820b9a58c0deae7c10b; sid_ucp_sso_v1=1.0.0-KDhlYTFmNDkzYWE1OGNkYTRhYzY5Y2Y1NDYxOTJhYjMxM2Q4MGYzZDkKFgjs2aCNtIyLBxCattCZBhgYOAJA7AcaAmxxIiBlNTg2MGFkZmRmZjFmODIwYjlhNThjMGRlYWU3YzEwYg; ssid_ucp_sso_v1=1.0.0-KDhlYTFmNDkzYWE1OGNkYTRhYzY5Y2Y1NDYxOTJhYjMxM2Q4MGYzZDkKFgjs2aCNtIyLBxCattCZBhgYOAJA7AcaAmxxIiBlNTg2MGFkZmRmZjFmODIwYjlhNThjMGRlYWU3YzEwYg; passport_auth_status=0caacba2e580049dbcf0ef990cbb5c73%2C51b7825aaf2f76f54abad7b64d92449f; passport_auth_status_ss=0caacba2e580049dbcf0ef990cbb5c73%2C51b7825aaf2f76f54abad7b64d92449f; sid_guard=92c772a01b8ab2042d76866993b43978%7C1664359195%7C5183999%7CSun%2C+27-Nov-2022+09%3A59%3A54+GMT; uid_tt=0ce88c26b2f2b929150896c4f1eb5ae9; uid_tt_ss=0ce88c26b2f2b929150896c4f1eb5ae9; sid_tt=92c772a01b8ab2042d76866993b43978; sessionid=92c772a01b8ab2042d76866993b43978; sessionid_ss=92c772a01b8ab2042d76866993b43978; sid_ucp_v1=1.0.0-KDFmZmQ2ZmMxYWI4NzI3MzgzZDQ5Y2FiMTdiYzI5ZWRlYjExOThiMDEKGAjs2aCNtIyLBxCbttCZBhgYIAw4AkDsBxoCaGwiIDkyYzc3MmEwMWI4YWIyMDQyZDc2ODY2OTkzYjQzOTc4; ssid_ucp_v1=1.0.0-KDFmZmQ2ZmMxYWI4NzI3MzgzZDQ5Y2FiMTdiYzI5ZWRlYjExOThiMDEKGAjs2aCNtIyLBxCbttCZBhgYIAw4AkDsBxoCaGwiIDkyYzc3MmEwMWI4YWIyMDQyZDc2ODY2OTkzYjQzOTc4; tt_anti_token=fbYxXJAcEtb-1ad10962e492fbb10fbc6432aa88c851e0ba89b2cc6c68bf9e1a40ef7e498c9c; tt_scid=jE5og96i1s.feae3Q8OCq-jw6ZqEzYXYj1JhrK-EA8tdMQH-v6NbgzuftQi9W8zO80a4; ttwid=1%7C_-L7IH1KUJKKL0X1Y-1GdzrclEKiLyw59T7lpq-p44s%7C1664359199%7Cf206c4def6619d119113e67910548c77772c14e43e60081348485d3994fb622d; odin_tt=b7be92a979b0939beb89ffa5e99c72cd6d00efd9a68e5d061850720fe13cdea3807a9ae3babc42ec779c89fdeae7ac5c";
//        String ck = "d_ticket=fdf75ce3a018392d8c1b291f862102b157bf7; odin_tt=b6f595b47ac99f2c309e140c9171f94f87556f76ea6248744121ee3ec3910870e05f3585c51b797116f61e2c4e42dcf15aa541a8b97c34995c37ed7f62a6ebbaa20de8b7088bb5e6b9ee8225e57120f9; odin_tt=e32015704d17d6812a5fed0bd70628dd4d23f6c5643f7d30e500180ef53bbd55ac662aa65e06eefa84d8b33f278d6f14184af73c6f049b79aed938d54b7852b6025e8561895f508c0352466506a45a8c; n_mh=BpcT0fXWusMRsoV9mric9NyA4DYFy6JJdeO7AXJe7iY; sid_guard=481ee4f6d92f3712934639804c09fffe%7C1664355573%7C5184000%7CSun%2C+27-Nov-2022+08%3A59%3A33+GMT; uid_tt=2204119c34ffcebe9b381f0cc54647ad; uid_tt_ss=2204119c34ffcebe9b381f0cc54647ad; sid_tt=481ee4f6d92f3712934639804c09fffe; sessionid=481ee4f6d92f3712934639804c09fffe; sessionid_ss=481ee4f6d92f3712934639804c09fffe; reg-store-region=; passport_csrf_token=ef23f354ff27e2528e43dd68d45aa4e2; passport_csrf_token_default=ef23f354ff27e2528e43dd68d45aa4e2; install_id=2212643683714893; ttreq=1$59856bbd46ce3a3f27637676f3a587ec28d2f865";
//        String ck = "d_ticket=b95f275111a0a4b2d84252c7b5367fbc6b6c9; odin_tt=d423243ffe4c61dc731b770c8044f12e24bc99d2a0e93890ec2be4167f731e9615f7155d1f6ae8d7a4a04c83474ccc560966a0de966fd097b6486db6068783c2298aebf923a4e9c3203276daf2513241; odin_tt=af6c104f3d27660abbd1597f064d9dfcdbc718896c4aea8b9e10fc9d30405e2219472e62458ebe26d04e62aeae52b050da77c2a8c5e388660398cf4e53d41943fdc1c1bf710790ff0b74716caa87358d; n_mh=DsNUuW2IHt2SFOzEpCiLXJQvYpZ6k3tD2WDTKwjxfhM; sid_guard=6ee0f3c751f6540810e349fb8e8a1678%7C1664031867%7C5184000%7CWed%2C+23-Nov-2022+15%3A04%3A27+GMT; uid_tt=fbb1cf1fc3b3d25a0be0e97912a67e9a; uid_tt_ss=fbb1cf1fc3b3d25a0be0e97912a67e9a; sid_tt=6ee0f3c751f6540810e349fb8e8a1678; sessionid=6ee0f3c751f6540810e349fb8e8a1678; sessionid_ss=6ee0f3c751f6540810e349fb8e8a1678; reg-store-region=; passport_csrf_token=54d1a7031e2a4a4da78ee9a42e999d6d; passport_csrf_token_default=54d1a7031e2a4a4da78ee9a42e999d6d; install_id=1069150265423672; ttreq=1$a84f14d0d3b764a09585acb1a713feddea95dd85";
//        String ck = "d_ticket=cc347104c8af39c150f54d1ddf85878c40a42; odin_tt=370b7180e150ac2f58155ec2e766de4e5535ddd45f1efa1ae9e9e5aa3ec652304a2e9f8b55c0d13203b6d9384d4cbcfd6c873a17388ea57d048e21534d0a4910df2df856e53dafef1f94565eb5d52dde; odin_tt=542480c311d93e77c80eedfcf2286248730bfd7900ad73e0f688c18bc490bf0f8ff72e7c761b3c19db5ba42121f33fa033fa7dce9d926009682872fd9436f2ecbad4d2a18ede3bb90e2c65c8c4ab2ec6; n_mh=_F5Ny6fLZ_igOUsisyoqLoC-8KYERmIuq2gBbP6QtxI; sid_guard=a25159e7fab694b64a23af73810b2427%7C1664355597%7C5184000%7CSun%2C+27-Nov-2022+08%3A59%3A57+GMT; uid_tt=ab3fc4adc1e10f83553f6419360aa82b; uid_tt_ss=ab3fc4adc1e10f83553f6419360aa82b; sid_tt=a25159e7fab694b64a23af73810b2427; sessionid=a25159e7fab694b64a23af73810b2427; sessionid_ss=a25159e7fab694b64a23af73810b2427; reg-store-region=; passport_csrf_token=5f0bea6e93e5dc497c13295271710ad7; passport_csrf_token_default=5f0bea6e93e5dc497c13295271710ad7; install_id=4147784148851319; ttreq=1$15963d544c9bb04ff72fc63735bb80124657e724";
//        String ck="d_ticket=75306e5adfabbf331e9ee3e621d6e5998c283; odin_tt=a35e932c126877f819015fb3e142793069a804e38cdd28e8ab048099164b933042ebc439a96fcec23bd37ca82d899c0023dc8d337c343d0ba253b673fb6155ab1fa66dceb7f2ad5fe4d80cc495e5fe80; odin_tt=16cadac06880ca60573600d97c7da5d9f5d9a6e3fff357e9c8226430982bfaf71559a71e2f0b1f2b73fdec88e76773e129bd2d73359357945315986d7b107dbc1fc4d01bb5a99d82bc5340f08310c204; n_mh=bl_3lYBwQftBk3BCFMgw9cJuT4x29pzAi19dKgrLTwY; sid_guard=47e4202f65af41e8d20544a4a069af0e%7C1664032010%7C5184000%7CWed%2C+23-Nov-2022+15%3A06%3A50+GMT; uid_tt=7f956790ec556afb4dcb78fee80d96aa; uid_tt_ss=7f956790ec556afb4dcb78fee80d96aa; sid_tt=47e4202f65af41e8d20544a4a069af0e; sessionid=47e4202f65af41e8d20544a4a069af0e; sessionid_ss=47e4202f65af41e8d20544a4a069af0e; reg-store-region=; passport_csrf_token=565f490a701259e63d7ea8959585e09c; passport_csrf_token_default=565f490a701259e63d7ea8959585e09c; install_id=2458932962923454; ttreq=1$5ece39a3d63335969afc9a856ba93a537798f3a1";
//        String ck = "uid_tt=bd37396cd17a4e22ae1f4fbd78402bc4;uid_tt_ss=bd37396cd17a4e22ae1f4fbd78402bc4;sid_tt=c2208ad3f596f105942d3d776c2f92d0;sid_guard=c2208ad3f596f105942d3d776c2f92d0%7C1663829498%7C5184000%7CSun%2C+16-Oct-2022+14%3A27%3A16+GMT;";
//        String ck = "d_ticket=e16e0f031db23d240aca8b7b5aa7d33094f07; odin_tt=15006a78edc3c28d546c335468ef425cd1a8591c91ad345693c61c22b993e4ba60059afb76932344e3f7480a96d56dc1d42f9a00eea8f75a6b01042a2a9a2a0bbb85b8ae6f8c83f6ef271e8bcdf7c43c; odin_tt=b7b47cbbeac89ff2dda5900c4beebee380b19729c895108da44196f97d4ebd3a2b80c5462d5cb8a7448cfddbb4c8058931c7d0696509da746a6d86d2eba4b5f58a81762e6b81376131eb244d378634d4; n_mh=3QhyXuejisEbvBdhNFrvRFIUPNYHJvke6p77A8A_olw; sid_guard=ccd75e27892a2f34171f6e9d7658062d%7C1665153278%7C5184000%7CTue%2C+06-Dec-2022+14%3A34%3A38+GMT; uid_tt=8c4896de8c9f003919565529622c9be2; uid_tt_ss=8c4896de8c9f003919565529622c9be2; sid_tt=ccd75e27892a2f34171f6e9d7658062d; sessionid=ccd75e27892a2f34171f6e9d7658062d; sessionid_ss=ccd75e27892a2f34171f6e9d7658062d; reg-store-region=; passport_csrf_token=0c1754a10d2d7a222584f2f752186ffd; passport_csrf_token_default=0c1754a10d2d7a222584f2f752186ffd; install_id=2142278207159975; ttreq=1$1192ee63b4d6b8e95620d6c9e259299d6d948014 x-tt-dt: AAAQD67JOZBM4STIVATH7FGI7N45KPOKOMLIEUNP7UMG3G223H324Y6DSQLXMP57UPYZ2BW7NVPYN4L7NEKTCUFDCNFDY3QXVU446WNT5Z4OFL2JPBXCRVKUJMMJG mac_address: 76%3A46%3A36%3A75%3A86%3A34 oaid: 29ea8f69f09e7124 uuid: 257205475488643 openudid: 79a5745c98388f23 access_key:   c3a250d9-2405-01b3-48bf-3067cb69af13  com.ss.android.article.news/8960 (Linux; U; Android 10.1.4; zh_CN; EDI-AL10; Build/EDI-AL10; Cronet/TTNetVersion:ff145923_2022-02-25QuicVersion:b314d107 2021-11-24) X-Tt-Token: 00ccd75e27892a2f34171f6e9d7658062d021df2550fd41eb4daffce576d3f756a52879bd28a34fe6a0012b6625167de6ec3659ee72c78e78f16de7669dc149846f269722423408ea48b58f4e3d1a244764ab4c82c03d8a4752203de0aefbcb387f57-1.0.1  EagleId: 77544d7f16651532779183273e;";
/*        BuyRenderParamDto buyRenderParamDto = BuyRenderParamDto.builder().product_id("3556357046087622442").sku_id("1736502463777799").author_id("4051040200033531")
                .ecom_scene_id("1041").origin_id("4051040200033531_3556357046087622442").origin_type("3002070010").new_source_type("product_detail").build();*/
/*        BuyRenderParamDto buyRenderParamDto = BuyRenderParamDto.builder().product_id("3561751789252519688").sku_id("1739136614382624").author_id("4051040200033531")
                .ecom_scene_id("1003").origin_id("4051040200033531_3561751789252519688").origin_type("3002002002").new_source_type("product_detail").build();*/

/* BuyRenderParamDto buyRenderParamDto = BuyRenderParamDto.builder().product_id("3561752220930340544").sku_id("1739136822194211").author_id("4051040200033531")
                .ecom_scene_id("").origin_id("4051040200033531_3561752220930340544").origin_type("3002002002").new_source_type("product_detail").build();
        System.err.println(JSON.toJSONString(buyRenderParamDto));*/
/*     BuyRenderParamDto buyRenderParamDto = BuyRenderParamDto.builder().product_id("3574327743640429367").sku_id("1745277214000191").author_id("4051040200033531")
                .ecom_scene_id("").origin_id("4051040200033531_3574327743640429367").origin_type("3002002002").new_source_type("product_detail").build();
        System.err.println(JSON.toJSONString(buyRenderParamDto));*/

            BuyRenderParamDto buyRenderParamDto = BuyRenderParamDto.builder().product_id("3568732766885568682").sku_id("1742545284123672").author_id("4051040200033531")
                    .ecom_scene_id("1082").origin_id("99514375927_3568732766885568682").origin_type("3002070010").new_source_type("product_detail")
                    .shop_id("EmySuHkX").build();
            String body = SubmitUtils.buildBuyRenderYongHui(buyRenderParamDto);
            Map<String, String> ipAndPort = Douyin3.getIpAndPort();
            OkHttpClient client = Demo.getOkHttpClient(ipAndPort.get("ip"), Integer.valueOf(ipAndPort.get("port")));
//        String body = "{\"address\":null,\"platform_coupon_id\":null,\"kol_coupon_id\":null,\"auto_select_best_coupons\":true,\"customize_pay_type\":\"{\\\"checkout_id\\\":1,\\\"bio_type\\\":\\\"1\\\"}\",\"first_enter\":true,\"source_type\":\"1\",\"shape\":0,\"marketing_channel\":\"\",\"forbid_redpack\":false,\"support_redpack\":true,\"use_marketing_combo\":false,\"entrance_params\":\"{\\\"order_status\\\":3,\\\"previous_page\\\":\\\"order_list_page\\\",\\\"carrier_source\\\":\\\"order_detail\\\",\\\"ecom_scene_id\\\":\\\"1041\\\",\\\"room_id\\\":\\\"\\\",\\\"promotion_id\\\":\\\"\\\",\\\"author_id\\\":\\\"\\\",\\\"group_id\\\":\\\"\\\",\\\"anchor_id\\\":\\\"4051040200033531\\\",\\\"source_method\\\":\\\"open_url\\\",\\\"ecom_group_type\\\":\\\"video\\\",\\\"discount_type\\\":\\\"\\\",\\\"full_return\\\":\\\"0\\\",\\\"is_exist_size_tab\\\":\\\"0\\\",\\\"rank_id_source\\\":\\\"\\\",\\\"show_rank\\\":\\\"not_in_rank\\\",\\\"warm_up_status\\\":\\\"0\\\",\\\"coupon_id\\\":\\\"\\\",\\\"brand_verified\\\":\\\"0\\\",\\\"label_name\\\":\\\"\\\",\\\"with_sku\\\":\\\"0\\\",\\\"is_replay\\\":\\\"0\\\",\\\"is_package_sale\\\":\\\"0\\\",\\\"is_groupbuying\\\":\\\"0\\\"}\",\"shop_requests\":[{\"shop_id\":\"GceCTPIk\",\"product_requests\":[{\"product_id\":\"3556357046087622442\",\"sku_id\":\"1736502463777799\",\"sku_num\":1,\"author_id\":\"4051040200033531\",\"ecom_scene_id\":\"1041\",\"origin_id\":\"4051040200033531_3556357046087622442\",\"origin_type\":\"3002070010\",\"new_source_type\":\"product_detail\",\"select_privilege_properties\":[]}]}]}";
            String url = "https://ken.snssdk.com/order/buyRender?b_type_new=2&request_tag_from=lynx&os_api=22&device_type=SM-G973N&ssmix=a&manifest_version_code=170301&dpi=240&is_guest_mode=0&uuid=354730528934825&app_name=aweme&version_name=17.3.0&ts=1664384063&cpu_support64=false&app_type=normal&appTheme=dark&ac=wifi&host_abi=armeabi-v7a&update_version_code=17309900&channel=dy_tiny_juyouliang_dy_and24&_rticket=1664384064117&device_platform=android&iid=" + iid + "&version_code=170300&cdid=481a445f-aeb7-4365-b0cd-4d82727bb775&os=android&is_android_pad=0&openudid=199d79fbbeff0e58&device_id=" + device_id + "&resolution=720%2A1280&os_version=5.1.1&language=zh&device_brand=samsung&aid=1128&minor_status=0&mcc_mnc=46007";
            String X_SS_STUB = SecureUtil.md5("json_form=" + URLEncoder.encode(body)).toUpperCase();
            String signData = String.format("{\"header\": {\"X-SS-STUB\": \"%s\",\"deviceid\": \"\",\"ktoken\": \"\",\"cookie\" : \"\"},\"url\": \"%s\"}",
                    X_SS_STUB, url
            );
            String signHt = HttpRequest.post("http://110.42.246.12:8191/tt1213").body(signData).execute().body();
            String x_gorgon = JSON.parseObject(signHt).getString("x-gorgon");
            String x_khronos = JSON.parseObject(signHt).getString("x-khronos");
            RequestBody requestBody = new FormBody.Builder()
                    .add("json_form", body)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("X-SS-STUB", X_SS_STUB)
                    .addHeader("Cookie", ck)
                    .addHeader("X-Gorgon", x_gorgon)
                    .addHeader("X-Khronos", x_khronos)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            Response response = client.newCall(request).execute();
            String resBody = response.body().string();
            log.info("预下单数据msg:{}", resBody);
            response.close();
            if (true) {
                //TODO 不让下单
                return;
            }
            BuyRenderRoot buyRenderRoot = JSON.parseObject(JSON.parseObject(resBody).getString("data"), BuyRenderRoot.class);
            buyRenderRoot.setPost_tel("18408282245");
            String url1 = "https://ec.snssdk.com/order/newcreate/vtl?can_queue=1&b_type_new=2&request_tag_from=lynx&os_api=22&device_type=SM-G973N&ssmix=a&manifest_version_code=170301&dpi=240&is_guest_mode=0&uuid=354730528934825&app_name=aweme&version_name=17.3.0&ts=1664384138&cpu_support64=false&app_type=normal&appTheme=dark&ac=wifi&host_abi=armeabi-v7a&update_version_code=17309900&channel=dy_tiny_juyouliang_dy_and24&device_platform=android&iid=" + iid + "&version_code=170300&cdid=481a445f-aeb7-4365-b0cd-4d82727bb775&os=android&is_android_pad=0&openudid=199d79fbbeff0e58&device_id=" + device_id + "&resolution=720*1280&os_version=5.1.1&language=zh&device_brand=samsung&aid=1128&minor_status=0&mcc_mnc=46007";
            String bodyData1 = String.format("{\"area_type\":\"169\",\"receive_type\":1,\"travel_info\":{\"departure_time\":0,\"trave_type\":1,\"trave_no\":\"\"}," +
                            "\"pickup_station\":\"\",\"traveller_degrade\":\"\",\"b_type\":3,\"env_type\":\"2\",\"activity_id\":\"\"," +
                            "\"origin_type\":\"%s\"," +
                            "\"origin_id\":\"%s\"," +
                            "\"new_source_type\":\"product_detail\",\"new_source_id\":\"0\",\"source_type\":\"0\"," +
                            "\"source_id\":\"0\",\"schema\":\"snssdk143://\",\"extra\":\"{\\\"page_type\\\":\\\"lynx\\\"," +
                            "\\\"alkey\\\":\\\"1128_99514375927_0_3556357046087622442_010\\\"," +
                            "\\\"c_biz_combo\\\":\\\"8\\\"," +
                            "\\\"render_track_id\\\":\\\"%s\\\"," +
                            "\\\"risk_info\\\":\\\"{\\\\\\\"biometric_params\\\\\\\":\\\\\\\"1\\\\\\\"" +
                            ",\\\\\\\"is_jailbreak\\\\\\\":\\\\\\\"2\\\\\\\",\\\\\\\"openudid\\\\\\\":\\\\\\\"\\\\\\\"," +
                            "\\\\\\\"order_page_style\\\\\\\":0,\\\\\\\"checkout_id\\\\\\\":1,\\\\\\\"ecom_payapi\\\\\\\":true," +
                            "\\\\\\\"ip\\\\\\\":\\\\\\\"%s\\\\\\\"," +
                            "\\\\\\\"sub_order_info\\\\\\\":[]}\\\"}\"," +
                            "\"marketing_plan_id\":\"%s\"," +
                            "\"s_type\":\"\"" +
                            ",\"entrance_params\":\"{\\\"order_status\\\":4,\\\"previous_page\\\":\\\"toutiao_mytab__order_list_page\\\"," +
                            "\\\"carrier_source\\\":\\\"order_detail\\\"," +
                            "\\\"ecom_scene_id\\\":\\\"%s\\\",\\\"room_id\\\":\\\"\\\"," +
                            "\\\"promotion_id\\\":\\\"\\\",\\\"author_id\\\":\\\"\\\",\\\"group_id\\\":\\\"\\\",\\\"anchor_id\\\":\\\"\\\"," +
                            "\\\"source_method\\\":\\\"open_url\\\",\\\"ecom_group_type\\\":\\\"\\\",\\\"module_label\\\":\\\"\\\"," +
                            "\\\"ecom_icon\\\":\\\"\\\",\\\"brand_verified\\\":\\\"0\\\",\\\"discount_type\\\":\\\"\\\",\\\"full_return\\\":\\\"0\\\"," +
                            "\\\"is_activity_banner\\\":0," +
                            "\\\"is_exist_size_tab\\\":\\\"0\\\",\\\"is_groupbuying\\\":\\\"0\\\",\\\"is_package_sale\\\":\\\"0\\\"," +
                            "\\\"is_replay\\\":\\\"0\\\",\\\"is_short_screen\\\":\\\"0\\\",\\\"is_with_video\\\":1,\\\"label_name\\\":\\\"\\\"," +
                            "\\\"market_channel_hot_fix\\\":\\\"\\\",\\\"rank_id_source\\\":\\\"\\\",\\\"show_dou_campaign\\\":0," +
                            "\\\"show_rank\\\":\\\"not_in_rank\\\",\\\"upfront_presell\\\":0,\\\"warm_up_status\\\":\\\"0\\\",\\\"auto_coupon\\\":0," +
                            "\\\"coupon_id\\\":\\\"\\\",\\\"with_sku\\\":\\\"0\\\",\\\"item_id\\\":\\\"0\\\"," +
                            "\\\"commodity_id\\\":\\\"%s\\\",\\\"commodity_type\\\":6," +
                            "\\\"product_id\\\":\\\"%s\\\",\\\"extra_campaign_type\\\":\\\"\\\"}\"," +
                            "\"sub_b_type\":\"3\",\"gray_feature\":\"PlatformFullDiscount\",\"sub_way\":0," +
                            "\"pay_type\":%d," +
                            "\"post_addr\":{\"province\":{},\"city\":{},\"town\":{},\"street\":{\"id\":\"\",\"name\":\"\"}}," +
                            "\"post_tel\":\"%s\",\"address_id\":\"0\",\"price_info\":{\"origin\":1000,\"freight\":0,\"coupon\":0," +
                            "\"pay\":1000}," +
                            "\"pay_info\":\"{\\\"sdk_version\\\":\\\"v2\\\",\\\"dev_info\\\":{\\\"reqIp\\\":\\\"39.144.42.162\\\",\\\"os\\\":\\\"android\\\"," +
                            "\\\"isH5\\\":false,\\\"cjSdkVersion\\\":\\\"6.3.5\\\",\\\"aid\\\":\\\"13\\\"," +
                            "\\\"ua\\\":\\\"com.ss.android.article.news/8960+(Linux;+U;+Android+10;+zh_CN;" +
                            "+PACT00;+Build/QP1A.190711.020;+Cronet/TTNetVersion:68deaea9+2022-07-19+QuicVersion:12a1d5c5+2022-06-27)\\\"," +
                            "\\\"riskUa\\\":\\\"\\\",\\\"lang\\\":\\\"zh-Hans\\\"," +
                            "\\\"deviceId\\\":\\\"%s\\\",\\\"osVersion\\\":\\\"10\\\"," +
                            "\\\"vendor\\\":\\\"\\\",\\\"model\\\":\\\"\\\",\\\"netType\\\":\\\"\\\"," +
                            "\\\"appVersion\\\":\\\"8.9.6\\\",\\\"appName\\\":\\\"news_article\\\"," +
                            "\\\"devicePlatform\\\":\\\"android\\\",\\\"deviceType\\\":\\\"PACT00\\\"," +
                            "\\\"channel\\\":\\\"oppo_13_64\\\",\\\"openudid\\\":\\\"\\\"," +
                            "\\\"versionCode\\\":\\\"896\\\",\\\"ac\\\":\\\"wifi\\\",\\\"brand\\\":\\\"OPPO\\\",\\\"iid\\\":\\\"%s\\\",\\\"bioType\\\":\\\"1\\\"}," +
                            "\\\"credit_pay_info\\\":{\\\"installment\\\":\\\"1\\\"},\\\"bank_card_info\\\":{},\\\"voucher_no_list\\\":[]," +
                            "\\\"zg_ext_param\\\":" +
                            "\\\"{\\\\\\\"decision_id\\\\\\\":\\\\\\\"%s\\\\\\\",\\\\\\\"qt_c_pay_url\\\\\\\":\\\\\\\"\\\\\\\"," +
                            "\\\\\\\"retain_c_pay_url\\\\\\\":\\\\\\\"\\\\\\\"}\\\"," +
                            "\\\"jh_ext_info\\\":\\\"{\\\\\\\"payapi_cache_id\\\\\\\":\\\\\\\"%s\\\\\\\"}\\\"," +
                            "\\\"sub_ext\\\":\\\"\\\",\\\"biometric_params\\\":\\\"1\\\",\\\"is_jailbreak\\\":\\\"2\\\"," +
                            "\\\"order_page_style\\\":0,\\\"checkout_id\\\":1,\\\"pay_amount_composition\\\":[]}\"," +
                            "\"render_token\":\"%s\"," +
                            "\"win_record_id\":\"\",\"marketing_channel\":\"\",\"identity_card_id\":\"\"," +
                            "\"pay_amount_composition\":[],\"user_account\":{},\"queue_count\":0,\"store_id\":\"\"," +
                            "\"shop_id\":\"%s\"," +
                            "\"combo_id\":\"%s\"," +
                            "\"combo_num\":1," +
                            "\"product_id\":\"%s\",\"buyer_words\":\"\",\"stock_info\":[{\"stock_type\":1,\"stock_num\":1," +
                            "\"sku_id\":\"%s\"" +
                            ",\"warehouse_id\":\"0\"}],\"warehouse_id\":0,\"coupon_info\":{},\"freight_insurance\":false,\"cert_insurance\":false," +
                            "\"allergy_insurance\":false,\"room_id\":\"\",\"author_id\":\"\",\"content_id\":\"0\",\"promotion_id\":\"\"," +
                            "\"ecom_scene_id\":\"%s\"," +
                            "\"shop_user_id\":\"\",\"group_id\":\"\"," +
                            "\"privilege_tag_keys\":[],\"select_privilege_properties\":[]," +
                            "\"platform_deduction_info\":{},\"win_record_info\":{\"win_record_id\":\"\",\"win_record_type\":\"\"}}",
                    buyRenderParamDto.getOrigin_type(),
                    buyRenderParamDto.getOrigin_id(),
                    buyRenderRoot.getRender_track_id(),
                    payIp,
                    buyRenderRoot.getTotal_price_result().getMarketing_plan_id(),
                    buyRenderParamDto.getEcom_scene_id(),
                    buyRenderParamDto.getProduct_id(),
                    buyRenderParamDto.getProduct_id(),
                    payType,
                    buyRenderRoot.getPost_tel(),
                    device_id,
                    iid,
                    buyRenderRoot.getPay_method().getDecision_id(),
                    buyRenderRoot.getPay_method().getPayapi_cache_id(),
                    buyRenderRoot.getRender_token(),

                    buyRenderParamDto.getShop_id(),
                    buyRenderParamDto.getSku_id(),
                    buyRenderParamDto.getProduct_id(),
                    buyRenderParamDto.getSku_id(),
                    buyRenderParamDto.getEcom_scene_id()
            );
            String X_SS_STUB1 = SecureUtil.md5("json_form=" + URLEncoder.encode(bodyData1)).toUpperCase();
            String signData1 = String.format("{\"header\": {\"X-SS-STUB\": \"%s\",\"deviceid\": \"\",\"ktoken\": \"\",\"cookie\" : \"\"},\"url\": \"%s\"}",
                    X_SS_STUB1, url1
            );
            String signHt1 = HttpRequest.post("http://110.42.246.12:8191/tt1213").body(signData1).execute().body();
            log.info("msg:{}", signHt1);
            String x_gorgon1 = JSON.parseObject(signHt1).getString("x-gorgon");
            String x_khronos1 = JSON.parseObject(signHt1).getString("x-khronos");
            String tarceid1 = JSON.parseObject(signHt1).getString("tarceid");
            RequestBody requestBody1 = new FormBody.Builder()
                    .add("json_form", bodyData1)
                    .build();
            Map<String, String> headers = PreUtils.buildIpMap(payIp);
            Request.Builder builder = new Request.Builder();
            for (String s : headers.keySet()) {
                builder.header(s, headers.get(s));
            }
            Request request1 = builder.url(url1)
                    .post(requestBody1)
                    .addHeader("Cookie", ck)
                    .addHeader("X-SS-STUB", X_SS_STUB1)
                    .addHeader("x-tt-trace-id", tarceid1)
                    .addHeader("User-Agent", "com.ss.android.article.news/8960 (Linux; U; Android 10; zh_CN; PACT00; Build/QP1A.190711.020; Cronet/TTNetVersion:68deaea9 2022-07-19 QuicVersion:12a1d5c5 2022-06-22)")
                    .addHeader("X-Gorgon", x_gorgon1)
                    .addHeader("X-Khronos", x_khronos1)
                    .build();
            Response response1 = client.newCall(request1).execute();
            String bodyRes1 = response1.body().string();
            response1.close();
            log.info("msg:{}", bodyRes1);
        }
    }
}
