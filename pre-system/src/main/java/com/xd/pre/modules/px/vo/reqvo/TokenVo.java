package com.xd.pre.modules.px.vo.reqvo;

import com.xd.pre.modules.sys.domain.JdDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenVo {
    /**
     * tokenKey
     */
    private String tokenKey;
    //是否无界面
    private Boolean headless = true;
    private String skuId;
    private String skuPrice;
    private String documentURL;
    private JdDocument jdDocument;
    private String ck;

}
