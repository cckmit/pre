package com.xd.pre.modules.px.cotroller;

import com.xd.pre.common.utils.R;
import com.xd.pre.modules.px.appstorePc.PcYoukaService;
import com.xd.pre.modules.px.psscan.BuildPcThor;
import com.xd.pre.modules.px.psscan.PcPayQr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/jd")
@RestController
@Slf4j
@CrossOrigin
public class PcController {

    @Autowired
    private PcYoukaService pcYoukaService;

    @PostMapping("/pcScan")
    public R pcScan(@RequestBody BuildPcThor buildPcThor) {
        log.info("执行pc扫码程序msg:{}",buildPcThor);
        R r = pcYoukaService.pcScan(buildPcThor);
        return r;
    }

    @PostMapping("/pcPayQr")
    public R pcPayQr(@RequestBody PcPayQr pcPayQr) {
        log.info("开始获取pc的扫码数据msg:{}",pcPayQr);
        R  r = pcYoukaService.pcPayQr(pcPayQr);
        return r;
    }

}
