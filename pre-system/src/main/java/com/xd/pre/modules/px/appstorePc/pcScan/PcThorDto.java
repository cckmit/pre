package com.xd.pre.modules.px.appstorePc.pcScan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PcThorDto {
    private String DeviceSeq;
    private String TrackID;
    private String thor;
    private String pinId;
    private String pin;
    private String unick;
    private String _tp;
    private String logining;
    private String _pst;
}
