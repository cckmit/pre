package com.xd.pre.modules.px.psscan;

import com.xd.pre.modules.px.appstorePc.pcScan.PcThorDto;
import lombok.Data;

@Data
public class BuildPcThor {
    private String proxyIp;
    private Integer ProxyPort;
    private String appck;
    private String userIp;
    private PcThorDto pcThorDto;

    private String sign;

}
