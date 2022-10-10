package com.xd.pre.modules.px.psscan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PcQRCodeDto {
    private String wlfstk_smdl;
    private String QRCodeKey;

}
