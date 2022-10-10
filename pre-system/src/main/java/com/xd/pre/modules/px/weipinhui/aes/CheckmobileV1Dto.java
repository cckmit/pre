package com.xd.pre.modules.px.weipinhui.aes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckmobileV1Dto {
    /**
     * {
     *     "code": 1,
     *     "data": {
     *         "extend": "{\"contact_phone\":\"15828287465\"}",
     *         "captchaType": "2",
     *         "pid": "24000-2cd30c59e00040148b849cbaca7759bd",
     *         "templateId": "https://captcha.vipstatic.com/h5_sms_inline-6dddf5b5.js",
     *         "authType": "1",
     *         "captchaId": "6iGUr4Xz80d5TsVfz4ZXsQ-j6S9DY_rTaQpBIC5M943fPjWHaFXbDY2WGPWynHqLAawhXMqtr6Bd_HkmbM-ZI9av5iJpEAJKuuYkfx70Y2odI-qzLSK0D5Y_GbdonZaH6WSPlk49z4GcTCb1swfHlQgizaCz8H9RSZZp5AJlG06o7lEL3h7qIdZo7uy-I8GPenxID2dZ6NuxM9dqzSvEPL1uNKrvbkBmuJzxFIJrqxV87LI4rxbSxFnVinoC9sebsW8IyKVRfWSRiq7Bk189DLNHQy6NxEwGTDu3NxI10TZ5Nds2Uham5nYeCaBvIA_umsw2i4oKenilE0PMTRDnew.924481255"
     *     },
     *     "msg": "success"
     * }
     */
    private String extend;
    private String captchaType;
    private String pid;
    private String templateId;
    private String authType;
    private String captchaId;
}
