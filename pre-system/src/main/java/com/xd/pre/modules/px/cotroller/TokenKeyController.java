package com.xd.pre.modules.px.cotroller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.xd.pre.common.utils.R;
import com.xd.pre.modules.px.service.ProxyProductService;
import com.xd.pre.modules.px.service.TokenKeyService;
import com.xd.pre.modules.px.vo.reqvo.TokenKeyVo;
import com.xd.pre.modules.px.vo.resvo.TokenKeyResVo;
import com.xd.pre.modules.sys.domain.JdProxyIpPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/jd")
@RestController
@Slf4j
public class TokenKeyController {

    @Autowired
    private TokenKeyService tokenKeyService;
    @Autowired
    private ProxyProductService proxyProductService;

    @PostMapping("/getTokenKey")
    public R getTokenKey(@RequestBody TokenKeyVo tokenKeyVo) throws Exception {
        log.info("当前请求的数据为:msg:[TokenKeyVo:{}]", tokenKeyVo);
        Assert.isTrue(StrUtil.isNotBlank(tokenKeyVo.getCookie()), "ck不能为空");
//        String pt_pin = PreUtils.get_pt_pin(tokenKeyVo.getCookie());
        JdProxyIpPort oneIp = proxyProductService.getOneIp(1,0,false);
        TokenKeyResVo tokenKeyResVo = tokenKeyService.getTokenKey(tokenKeyVo, oneIp,"自己");
        log.info("签证结束结果为:msg:[returnSignVoAndDto:{}]", tokenKeyResVo);
        return R.ok(tokenKeyResVo);
    }

}
