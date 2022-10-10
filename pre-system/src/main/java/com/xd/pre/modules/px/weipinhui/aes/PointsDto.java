package com.xd.pre.modules.px.weipinhui.aes;

import com.xd.pre.modules.px.weipinhui.CaptchaXY;


public class PointsDto {
    public static String dataPoints(CaptchaXY captchaXY) {
        String data = String.format("{  " +
                        "  \"cid\": \"%s\",  " +
                        "  \"mars\": {\"cid\": \"%s\"},  " +
                        "  \"type\": [\"browser\", \"screen\", \"mars\", \"bootstrap\"],  " +
                        "  \"browser\": {\"ua\": \"Mozilla/5.0 (Linux; Android 8.0.0; SM-G955U Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Mobile Safari/537.36\"},  " +
                        "  \"screen\": {\"width\": 360,\"height\": 740},  " +
                        "  \"bootstrap\": {\"version\": \"vsc-49c18970.js\"  " +
                        "  } , " +
                        "  \"points\": [\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\"],  " +
                        "  \"antiCacheTime\": %s  " +
                        "}", captchaXY.getCaptchaRes().getMinaEdataDto().getMars_cid(), captchaXY.getCaptchaRes().getMinaEdataDto().getMars_cid(),
                captchaXY.getX1(), captchaXY.getY1(), captchaXY.getX2(), captchaXY.getY2(), captchaXY.getX3(), captchaXY.getY3()
                , System.currentTimeMillis() + ""
        );
        return data;
    }
    public static String dataCaptchaCode(CaptchaXY captchaXY) {
        String format = String.format("{  " +
                        "  \"cid\": \"%s\",  " +
                        "  \"type\": [\"browser\", \"screen\", \"mars\", \"bootstrap\"],  " +
                        "  \"browser\": {\"ua\": \"Mozilla/5.0 (Linux; Android 8.0.0; SM-G955U Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Mobile Safari/537.36\"},  " +
                        "  \"screen\": {\"width\": 360,\"height\": 740},  " +
                        "  \"mars\": {\"cid\": \"%s\"},  " +
                        "  \"bootstrap\": {\"version\": \"vsc-49c18970.js\"},  " +
                        "  \"captchaCode\": \"%s\",  " +
                        "  \"antiCacheTime\": %s  " +
                        "}", captchaXY.getCaptchaRes().getMinaEdataDto().getMars_cid(), captchaXY.getCaptchaRes().getMinaEdataDto().getMars_cid(),
                captchaXY.getCaptchaCode(),
                System.currentTimeMillis() + ""
        );
        return format;
    }

}
