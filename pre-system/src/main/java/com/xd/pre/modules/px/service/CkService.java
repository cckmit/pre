package com.xd.pre.modules.px.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xd.pre.common.constant.PreConstant;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.modules.px.vo.reqvo.TokenKeyVo;
import com.xd.pre.modules.px.vo.resvo.TokenKeyResVo;
import com.xd.pre.modules.sys.domain.JdCk;
import com.xd.pre.modules.sys.domain.JdCkZhideng;
import com.xd.pre.modules.sys.domain.JdProxyIpPort;
import com.xd.pre.modules.sys.mapper.JdCkMapper;
import com.xd.pre.modules.sys.mapper.JdCkZhidengMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
public class CkService {
    @Resource
    private JdCkMapper jdCkMapper;

    @Autowired
    private TokenKeyService TokenKeyService;

    @Resource
    private JdCkZhidengMapper jdCkZhidengMapper;

    public boolean ckAddCount(String str, Integer count, String uuid) {
        try {
            String pt_pin = PreUtils.get_pt_pin(str);
            if (StrUtil.isEmpty(pt_pin)) {
                log.info("无法找到");
                return false;
            }
            JdCk jdCk = jdCkMapper.selectOne(Wrappers.<JdCk>lambdaQuery().eq(JdCk::getPtPin, pt_pin));
            if (ObjectUtil.isNull(jdCk)) {
                JdCk build = JdCk.builder().ck(str).isEnable(1).ptPin(pt_pin).createTime(new Date()).fileName(uuid).build();
                jdCkMapper.insert(build);
            } else {
                jdCk.setCk(str);
                jdCk.setUseTimes(jdCk.getUseTimes() + count);
                jdCk.setFileName(uuid);
                if (jdCk.getFailTime() >= PreConstant.THREE) {
                    jdCk.setIsEnable(0);
                } else {
                    jdCk.setIsEnable(1);
                }
                jdCkMapper.updateById(jdCk);
            }
            return true;
        } catch (Exception e) {
            log.info("添加次数失败msg:[error:{}]", e);
        }
        return false;

    }

    @Autowired
    private ProxyProductService proxyProductService;

    public boolean ckAdd(String str, String fileName) {
        try {
            String pt_pin = PreUtils.get_pt_pin(str);
            if (StrUtil.isEmpty(pt_pin)) {
                return false;
            }
            TokenKeyVo tokenKeyVo = new TokenKeyVo();
            tokenKeyVo.setCookie(str);
            JdProxyIpPort oneIp = proxyProductService.getOneIp(1,  0, false);
            TokenKeyResVo tokenKey = TokenKeyService.getTokenKey(tokenKeyVo, oneIp, fileName);
            if (ObjectUtil.isNull(tokenKey)) {
                return false;
            }
            JdCk jdCk = jdCkMapper.selectOne(Wrappers.<JdCk>lambdaQuery().eq(JdCk::getPtPin, pt_pin));
            if (ObjectUtil.isNull(jdCk)) {
                JdCk build = JdCk.builder().ck(str).fileName(fileName).isEnable(1).ptPin(pt_pin).createTime(new Date()).build();
                jdCkMapper.insert(build);
            } else {
                jdCk.setCreateTime(new Date());
                jdCk.setCk(str);
                jdCk.setIsEnable(1);
                jdCk.setFileName(fileName);
                jdCk.setUseTimes(0);
                jdCkMapper.updateById(jdCk);
            }
            return true;
        } catch (Exception e) {
            log.error("报错,请查看msg:[error:{}]", e);
        }
        return false;
    }

    /**
     * 删除pt_pin
     *
     * @param cookie
     */
    public void deleteByCk(String cookie) {
        String pt_pin = PreUtils.get_pt_pin(cookie);
        jdCkMapper.deleteByPtPin(pt_pin);
        jdCkZhidengMapper.deleteByPtPin(pt_pin);

    }
}