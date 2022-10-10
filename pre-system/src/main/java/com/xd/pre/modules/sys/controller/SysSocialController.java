package com.xd.pre.modules.sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xd.pre.common.utils.R;
import com.xd.pre.log.annotation.SysOperaLog;
import com.xd.pre.modules.security.social.SocialRedisHelper;
import com.xd.pre.modules.sys.domain.SysSocial;
import com.xd.pre.modules.sys.service.ISysSocialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
/**
 * @Classname SysSocialController
 * @Description 社交登录
 * @Author Created by Lihaodong (alias:小东啊) lihaodongmail@163.com
 * @Date 2019-07-17 16:51
 * @Version 1.0
 */
@RestController
@RequestMapping("/social")
public class SysSocialController {

    @Autowired
    private SocialRedisHelper socialRedisHelper;
    @Autowired
    private ISysSocialService socialService;


    /**
     * 社交查询列表
     *
     * @param page
     * @return
     */
    @PreAuthorize("hasAuthority('sys:social:view')")
    @GetMapping
    public R selectSocial(Page page, SysSocial sysSocial) {
        return R.ok(socialService.selectSocialList(page,sysSocial));
    }

    /**
     * 社交登录解绑
     * @param userId
     * @param providerId
     * @return
     */
    @SysOperaLog(descrption = "解绑社交账号")
    @PostMapping("/untied")
    @PreAuthorize("hasAuthority('sys:social:untied')")
    public R untied(Integer userId, String providerId) {
        //将业务系统的用户与社交用户绑定
        socialRedisHelper.doPostSignDown(userId, providerId);
        return R.ok();
    }


}
