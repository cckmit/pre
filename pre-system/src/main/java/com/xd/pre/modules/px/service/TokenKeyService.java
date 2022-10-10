package com.xd.pre.modules.px.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xd.pre.common.sign.JdSgin;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.common.utils.px.dto.SignVoAndDto;
import com.xd.pre.modules.px.vo.reqvo.TokenKeyVo;
import com.xd.pre.modules.px.vo.resvo.TokenKeyResVo;
import com.xd.pre.modules.sys.domain.JdProxyIpPort;
import com.xd.pre.modules.sys.domain.JdSignParam;
import com.xd.pre.modules.sys.mapper.JdCkMapper;
import com.xd.pre.modules.sys.mapper.JdProxyIpPortMapper;
import com.xd.pre.modules.sys.mapper.JdSignParamMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URLDecoder;

@Slf4j
@Service
public class TokenKeyService {


    @Resource
    private JdSignParamMapper jdSignParamMapper;
    @Resource
    private JdCkMapper jdCkMapper;
    @Autowired
    private CkService ckService;


    @Autowired
    private ProxyProductService proxyProductService;

    @Resource
    private JdProxyIpPortMapper jdProxyIpPortMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public TokenKeyResVo getTokenKey(TokenKeyVo tokenKeyVo, JdProxyIpPort oneIp, String uuid) {
        try {
            if (tokenKeyVo.getCookie().contains("unionwsws") && tokenKeyVo.getCookie().contains("devicefinger") && tokenKeyVo.getCookie().contains("jmafinger")) {
                log.debug("表示组装过一次的");
                log.info("删除");
                int unionwsws = tokenKeyVo.getCookie().indexOf("unionwsws");
                String replace = tokenKeyVo.getCookie().substring(0, unionwsws).replace("��", "");
                tokenKeyVo.setCookie(replace);
                for (int i = 0; i < 10; i++) {
                    if (tokenKeyVo.getCookie().substring(tokenKeyVo.getCookie().length() - 1, tokenKeyVo.getCookie().length()).equals(";")
                            || tokenKeyVo.getCookie().substring(tokenKeyVo.getCookie().length() - 1, tokenKeyVo.getCookie().length()).equals("；")) {
                        replace = replace.substring(0, replace.length() - 1);
                        tokenKeyVo.setCookie(replace);
                    }
                }
            }
            if (tokenKeyVo.getCookie().contains("pt_pin=") && tokenKeyVo.getCookie().contains("pt_key=") && !tokenKeyVo.getCookie().contains("wskey")) {
                log.info("当前ck不包含appck，不参与签证");
                return null;
            }
//            String newCk = tokenKeyVo.getCookie() + ";unionwsws=" + PreUtils.addCkFiger();
            String newCk = tokenKeyVo.getCookie() + ";";
            tokenKeyVo.setCookie(newCk);
            Assert.isTrue(StrUtil.isNotBlank(tokenKeyVo.getCookie()), "ck不能为空");
            JdSignParam genTokenDb = jdSignParamMapper.selectOne(Wrappers.<JdSignParam>lambdaQuery().eq(JdSignParam::getMark, "genToken"));
            if (StrUtil.isNotBlank(tokenKeyVo.getBody())) {
                genTokenDb.setBody(tokenKeyVo.getBody());
            }
            genTokenDb.setUuid(PreUtils.getRandomString("55bs162e53b926e1".length()));
            for (int i = 0; i < genTokenDb.getTimes(); i++) {
                TokenKeyResVo tokenKeyResVo = getTokenKeyResVo(tokenKeyVo, genTokenDb, oneIp);
                if (ObjectUtil.isNotNull(tokenKeyResVo) && genTokenDb.getLengthStr() == tokenKeyResVo.getTokenKey().length()) {
                    log.info("当前请求的token数据是msg;[tokenKeyResVo]", tokenKeyResVo);
                    return tokenKeyResVo;
                } else if (ObjectUtil.isNotNull(tokenKeyResVo) && tokenKeyResVo.getTokenKey().length() != 3) {
                    log.info("过期直接删除");
                    ckService.deleteByCk(tokenKeyVo.getCookie());
                } else if (ObjectUtil.isNotNull(tokenKeyResVo) && tokenKeyResVo.getTokenKey().length() == 3) {
                    log.debug("当前代理有问题。删除代理");
                    this.jdProxyIpPortMapper.deleteById(oneIp.getId());
                    redisTemplate.delete("IP缓存池:" + oneIp.getId());
                }
                log.info("tokekey的长度msg:{}", tokenKeyResVo);
            }
            log.info("Ck失效msg:[ptpin:{}]", URLDecoder.decode(PreUtils.get_pt_pin(tokenKeyVo.getCookie())));
        } catch (Exception e) {
            log.error("msg:{}", e.getMessage());
        }
        return null;
    }


    private TokenKeyResVo getTokenKeyResVo(TokenKeyVo tokenKeyVo, JdSignParam genTokenDb, JdProxyIpPort oneIp) {
        try {
            String pt_pin = PreUtils.get_pt_pin(tokenKeyVo.getCookie());
            log.info("记录使用存在记录的次数,如果不存在就添加msg:[pt_pin:{}]", pt_pin);
            if (StrUtil.isBlank(tokenKeyVo.getBody())) {
                String body = genTokenDb.getBody();
                tokenKeyVo.setBody(body);
            }
            JSONObject paramMap = JSON.parseObject(genTokenDb.getBody());
            if (!paramMap.containsKey("body")) {
                paramMap.put("body", tokenKeyVo.getBody());
            }
            SignVoAndDto signVoAndDto = new SignVoAndDto(genTokenDb.getFunctionId(), paramMap.get("body").toString(), genTokenDb.getClientVersion(), genTokenDb.getUuid());
            log.info("开始签证");
            TimeInterval timer = DateUtil.timer();
            signVoAndDto = JdSgin.newSign(signVoAndDto);
//            signVoAndDto = RunSignUtils.signMain(signVoAndDto, path.getJdApk());
            System.out.println("--2--------------------------" + timer.interval());
            log.info("结束签证");
            String urlParamTokenKey = tokenKeyVo.getUrlParamTokenKey(signVoAndDto);
            String tokenKeyUrl = genTokenDb.getUrl() + urlParamTokenKey;
            Proxy proxy = null;
            if (ObjectUtil.isNotNull(oneIp)) {
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
            }
            TokenKeyResVo tokenKeyResVo = getTokenKeyResVo(tokenKeyVo, pt_pin, paramMap, timer, tokenKeyUrl, proxy);
            if (tokenKeyResVo != null) {
                return tokenKeyResVo;
            } else {
                log.error("当前的代理超时了删除IP代理池");
                redisTemplate.delete("IP缓存池:" + oneIp.getId());
            }
        } catch (Exception e) {
            log.error("报错msg:[getTokenKeyResVo:{}]", e);
        }
        return null;
    }

    private TokenKeyResVo getTokenKeyResVo(TokenKeyVo tokenKeyVo, String pt_pin, JSONObject paramMap, TimeInterval timer, String tokenKeyUrl, Proxy proxy) {
        try {
            HttpRequest rq = HttpRequest.post(tokenKeyUrl)
                    .header(Header.HOST, "api.m.jd.com")//头信息，多个头信息多次调用此方法即可
                    .header(Header.CACHE_CONTROL, "no-cache")
                    .header(Header.USER_AGENT, "okhttp/3.12.1;jdmall;android;version/10.4.6;build/95105;api.m.jd.com")
//                    .setProxy(proxy)
                    .form(paramMap)//表单内容
                    .timeout(3000)//超时，毫秒
                    .header(Header.COOKIE, tokenKeyVo.getCookie().trim())
                    .header("jdc-backup", tokenKeyVo.getCookie().trim());
            if (ObjectUtil.isNotNull(proxy)) {
                rq.setProxy(proxy);
            }
            String genTokenBody = rq.execute().body();
            log.info("获取当前数据为:[genTokenBody:{}]", genTokenBody);
            System.out.println("--3--------------------------" + timer.interval());
            TokenKeyResVo tokenKeyResVo = JSON.parseObject(genTokenBody, TokenKeyResVo.class);
            tokenKeyResVo.setPt_pin(pt_pin);
            tokenKeyResVo.setCookie(tokenKeyVo.getCookie());
            if ("0".equals(tokenKeyResVo.getCode())) {
                return tokenKeyResVo;
            }
        } catch (Exception e) {
            log.error("请求错误。msg:[error:{}]", e.getMessage());
        }
        return null;
    }


}
