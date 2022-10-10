package com.xd.pre.modules.px.vo.tmpvo.sysvo;

import lombok.Data;

@Data
public class ProxyAnalysisVo {
    //    {"code":0,"data":[{"ip":"115.239.102.237","port":4278,"expire_time":"2022-04-18 17:43:25","city":"浙江省嘉兴市"}],"msg":"0","success":true}
    private String ip;
    private Integer port;
    private String outip;
    private String expire_time;
    private String city;
}
