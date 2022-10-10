package com.xd.pre.modules.px.cotroller;

import com.xd.pre.common.sign.JdSgin;
import com.xd.pre.common.utils.R;
import com.xd.pre.common.utils.px.dto.SignVoAndDto;
import com.xd.pre.modules.px.service.ProxyProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/jd")
@RestController
@Slf4j
public class SignController {

    @Autowired
    private ProxyProductService proxyProductService;

    @PostMapping("/sign")
    public R sign(@RequestBody SignVoAndDto signVoAndDto) throws Exception {
        log.info("签证参数为:msg:[signVoAndDto:{}]", signVoAndDto);
        log.info("proxyProductService:{}", proxyProductService);
        signVoAndDto = JdSgin.newSign(signVoAndDto);
//        SignVoAndDto returnSignVoAndDto = RunSignUtils.signMain(signVoAndDto,proxyProductService.getPath().getJdApk());
        log.info("签证结束结果为:msg:[returnSignVoAndDto:{}]", signVoAndDto);
        return R.ok(signVoAndDto);
    }
}
