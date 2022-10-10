package com.xd.pre.modules.px.mendian.submit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SkuList {
    private long skuId;
    private int skuCount;
}
