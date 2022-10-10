package com.xd.pre.modules.px.cotroller;

import com.xd.pre.common.utils.R;
import com.xd.pre.modules.px.service.ProxyProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@RequestMapping("/jd")
public class ProxyAddressProductController {


    @Autowired
    private ProxyProductService proxyProductService;

    @PostMapping("/productIp")
    public R productIpAndPort() {
        proxyProductService.productIpAndPort1();
        return R.ok();
    }
}
