package com.xd.pre.modules.data.tenant;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.xd.pre.common.constant.PreConstant;
import com.xd.pre.modules.security.util.JwtUtil;
import com.xd.pre.security.PreSecurityUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Classname PreTenantContextFilter
 * @Description 多租户上下文过滤器 -设置加载顺序最高获取租户
 * @Author Created by Lihaodong (alias:小东啊) lihaodongmail@163.com
 * @Date 2019-08-10 19:52
 * @Version 1.0
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PreTenantContextHolderFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 后面考虑存到redis
        String tenantId = request.getHeader(PreConstant.PRE_TENANT_KEY);
        PreSecurityUser securityUser = jwtUtil.getUserFromToken(request);

        //在没有提供tenantId的情况下返回默认的
        if (StrUtil.isNotBlank(tenantId)) {
            PreTenantContextHolder.setCurrentTenantId(Long.valueOf((tenantId)));
        } else {
            String username = request.getParameter("username");
            if (StrUtil.isNotBlank(username) && username.equals("douyin")) {
                PreTenantContextHolder.setCurrentTenantId(2L);
            } else if (ObjectUtil.isNotNull(securityUser) && securityUser.getUsername().equals("douyin")) {
                PreTenantContextHolder.setCurrentTenantId(2L);
            } else {
                PreTenantContextHolder.setCurrentTenantId(1L);
            }

        }
        filterChain.doFilter(request, response);
    }
}
