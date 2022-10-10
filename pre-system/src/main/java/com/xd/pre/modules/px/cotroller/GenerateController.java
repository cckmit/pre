package com.xd.pre.modules.px.cotroller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xd.pre.common.utils.R;
import com.xd.pre.modules.px.vo.reqvo.BusinessVo;
import com.xd.pre.modules.sys.domain.JdCk;
import com.xd.pre.modules.sys.mapper.JdCkMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping("/jd")
@RestController
@Slf4j
public class GenerateController {


//    @Autowired
//    private GetWeiXinPayServiceByPostService getWeiXinPayServiceByPostService;

    @Resource
    private JdCkMapper jdCkMapper;

    @PostMapping("/business")
    public R businessWeiWinPay(@RequestBody BusinessVo businessVo) {
        Page<JdCk> jdCkPage = jdCkMapper.selectPage(new Page<>(3, 5), Wrappers.<JdCk>lambdaQuery().eq(JdCk::getIsEnable, 1));
        for (JdCk jdCk : jdCkPage.getRecords()) {
            try {
                businessVo.setCk(jdCk.getCk());
//                String url = getWeiXinPayServiceByPostService.build(businessVo);
            } catch (Exception e) {

            }
        }
        return R.ok();
    }
}
