package com.xd.pre.pcScan;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.xd.pre.modules.px.appstorePc.pcScan.PcThorDto;
import com.xd.pre.modules.px.appstorePc.pcScan.Get_confirmQRCodeScanned;
import com.xd.pre.modules.px.appstorePc.pcScan.Get_oi_symmetry_encrypt;
import com.xd.pre.modules.px.appstorePc.pcScan.ScanUtils;
import com.xd.pre.modules.px.psscan.PcQRCodeDto;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Demo {
    public static void main(String[] args) throws Exception {
        //Java中与(&)、非(~)、或(|)、异或(^)运算_
        String Uuid = "-1b78895a1exq";
        // String QRCodeKey = "AAEAIGktrCWxr0_wVdi5yp4VwzBIH3uV6HW6TLXYtDA6yYZm";
        String Devices_ = "D41D8CD98F00B204E9800998ECF8428A";
        String key = "qvnjxuiywxlsjhxx";
        Map<String, String> ipAndPort = Connect.getIpAndPort();
        OkHttpClient client = getOkHttpClient(ipAndPort.get("ip"), Integer.valueOf(ipAndPort.get("port")));
      //  OkHttpClient client = getOkHttpClient(null,null);
        ScanUtils scanUtils = new ScanUtils();
        PcQRCodeDto pcQRCodeDto = scanUtils.getQRAndMs(client);
       //  pcQRCodeDto=PcQRCodeDto.builder().QRCodeKey("AAEAIP03jwrbZZGVFaT0mA2pZx6swLJwxxJq8ooolecguaft").wlfstk_smdl("2sqv1h6p1832sm359bhq0g4e44dmlxrn").build();
        String QRCodeKey = pcQRCodeDto.getQRCodeKey();

        //pin=xq-461339-tanjingyu3;wskey=AAJiiSTAAFBTbOTImBDZCrvFWJiXaf5X5SGy0a5uEx4Dg78KAzs1n46WF6DCoSFsmM4N5UFBe0dxUBlEYVuXf0PkbDypu9zMmcGZUk-A0gBviE0OSV-f6Q;
        //pin=BSGQJD;wskey=AAJiiSSlAEDFvCk1nc75nFdAa2FkEqp7yG-rSCEIuGBBeGn2l8uIowk6vWeOV1JcXEq_Oy0Gf5ME9Dsg7bsaF3Oa11Sw4BiB;
        //pin=jd_xxsm;wskey=AAJiiSSlAEAyA5dMmryoUPntj2AxhmmJUuIyxTtOIiXVNYcTekRRQOIqM_aivwH-aOFZPqpGpchcrIyB843e--_gdbgoo64i;
        //pin=jd_FinCRjbHMY7gF80;wskey=AAJigg5OAFCUoaNGkMfdDg7-gcOnUNOl-KkxaA_XG3sluyp2aED_sjd_fAawllEgnt0WAe1phu2da14dyez3T0yBvOUFP6x1SVh97KarKODkbvmhtZuClA==
        String pin = "jd_FinCRjbHMY7gF80";
        String wskey = "AAJigg5OAFCUoaNGkMfdDg7-gcOnUNOl-KkxaA_XG3sluyp2aED_sjd_fAawllEgnt0WAe1phu2da14dyez3T0yBvOUFP6x1SVh97KarKODkbvmhtZuClA==";
        List<Integer> confirmQRCodeScanned = Get_confirmQRCodeScanned.get_confirmQRCodeScanned(Uuid, QRCodeKey, Devices_, false, wskey, pin);
        String reqScanData = Get_oi_symmetry_encrypt.get_oi_symmetry_encrypt(confirmQRCodeScanned.stream().mapToInt(i -> i).toArray(), key);
        log.info("执行扫码操作msg:{}", reqScanData);
        String postScanData = scanUtils.postScan(client, reqScanData);
/*        int[] oi_symmetry_decrypt = get_oi_symmetry_decrypt.get_oi_symmetry_decrypt(Fuzhu.易语言Base64(postScanData), Fuzhu.byte2Int(key.getBytes()));
        LoginPcEnum tlv_decode = get_TLV_Decode.get_TLV_Decode(oi_symmetry_decrypt);
        log.info("msg:{}", tlv_decode.getValue());*/
        List<Integer> get_confirmQRCodeLogined = Get_confirmQRCodeScanned.get_confirmQRCodeScanned(Uuid, QRCodeKey, Devices_, true, wskey, pin);
        String confirmQRCodeLoginedData = Get_oi_symmetry_encrypt.get_oi_symmetry_encrypt(get_confirmQRCodeLogined.stream().mapToInt(i -> i).toArray(), key);
        log.info("执行确认登录数据msg:{}", confirmQRCodeLoginedData);
        String resConfirmData = scanUtils.postComfirmQR(client, confirmQRCodeLoginedData);
        log.info("confrimRes:{}", resConfirmData);
        String ticket = scanUtils.getTicket(client, pcQRCodeDto);
        // HttpRequest.get(String.format("https://passport.jd.com/uc/qrCodeTicketValidation?t=%s"),).header();
        PcThorDto pcThorDto = scanUtils.getPcCkByTicket(client, pcQRCodeDto, ticket);
        log.info("{}", JSON.toJSONString(pcThorDto));
        if (ObjectUtil.isNull(pcThorDto)) {
            log.error("pc扫码失败msg:{}", pin);
        }
    }






    public static OkHttpClient getOkHttpClient(String ip, Integer port) {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        if(true){
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
            builder.proxy(proxy);
        }
        return builder.connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).followRedirects(false).build();
    }

}
