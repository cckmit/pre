package com.xd.pre.modules.px.weipinhui.findOrder;

import com.xd.pre.modules.px.weipinhui.create.WphCreateDto;
import com.xd.pre.modules.sys.domain.JdAppStoreConfig;
import com.xd.pre.modules.sys.domain.WphAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.Map;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderSyn {
    private JdAppStoreConfig skuConfig;
    private Map<String, String> headerMap;
    private  WphAccount wphAccountDb;
    private String phone;
    private WphCreateDto wphCreateDto;
    private String code;
    private Date createDate;
}
