package com.xd.pre.modules.px.cotroller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.xd.pre.common.utils.R;
import com.xd.pre.modules.px.service.WeiXinPayUrlService;
import com.xd.pre.modules.px.vo.reqvo.TokenVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/jd")
@RestController
@Slf4j
public class ObtainPayUrlController {

    @Autowired
    private WeiXinPayUrlService weiXinPayUrlService;

    @PostMapping("/orderListByTokenKey")
    public R getOrderListByTokenKey(@RequestBody TokenVo tokenVo) throws Exception {
        Assert.isTrue(StrUtil.isNotBlank(tokenVo.getSkuId()), "skuId不能为空");
        Assert.isTrue(StrUtil.isNotBlank(tokenVo.getTokenKey()), "tokenKey不能为空");
        Assert.isTrue(StrUtil.isNotBlank(tokenVo.getSkuPrice()), "getSkuPrice不能为空");
        TokenVo jdDocument = weiXinPayUrlService.obtainPayUrl(tokenVo);
        log.info("执行msg:[tokenVo:{}]", tokenVo);
        tokenVo.setDocumentURL(jdDocument.getDocumentURL());
        log.info("返回:[tokenVo:{}]", tokenVo);
        return R.ok(tokenVo);
    }
    @PostMapping("/orderListByTokenKey1")
    public R orderListByTokenKey1(@RequestBody TokenVo tokenVo) throws Exception {
        Assert.isTrue(StrUtil.isNotBlank(tokenVo.getSkuId()), "skuId不能为空");
        Assert.isTrue(StrUtil.isNotBlank(tokenVo.getTokenKey()), "tokenKey不能为空");
        Assert.isTrue(StrUtil.isNotBlank(tokenVo.getSkuPrice()), "getSkuPrice不能为空");
        TokenVo jdDocument = weiXinPayUrlService.obtainPayUrl1(tokenVo);
        log.info("执行msg:[tokenVo:{}]", tokenVo);
        tokenVo.setDocumentURL(jdDocument.getDocumentURL());
        log.info("返回:[tokenVo:{}]", tokenVo);
        return R.ok(tokenVo);
    }
}