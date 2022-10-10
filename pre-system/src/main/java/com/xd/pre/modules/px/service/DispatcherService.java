//package com.xd.pre.modules.px.service;
//
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.core.util.ObjectUtil;
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.http.Header;
//import cn.hutool.http.HttpRequest;
//import cn.hutool.json.JSON;
//import cn.hutool.json.JSONObject;
//import com.baomidou.mybatisplus.core.toolkit.Wrappers;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.xd.pre.common.utils.R;
//import com.xd.pre.common.utils.px.PreUtils;
//import com.xd.pre.common.utils.px.ThreadHttpRequest;
//import com.xd.pre.modules.px.vo.reqvo.StepVo;
//import com.xd.pre.modules.px.vo.resvo.TokenKeyResVo;
//import com.xd.pre.modules.sys.domain.JdAppStoreConfig;
//import com.xd.pre.modules.sys.domain.JdCk;
//import com.xd.pre.modules.sys.domain.JdLocalUrl;
//import com.xd.pre.modules.sys.mapper.*;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;
//import java.math.BigDecimal;
//import java.util.*;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.stream.Collectors;
//
//@Service
//@Slf4j
//public class DispatcherService {
//
//    @Resource
//    private JdAppStoreConfigMapper jdAppStoreConfigMapper;
//    @Resource
//    private JdOrderPtMapper jdOrderPtMapper;
//    @Resource
//    private JdDocumentMapper documentMapper;
//    @Resource
//    private JdLocalUrlMapper jdLocalUrlMapper;
//    @Resource
//    private JdCkMapper jdCkMapper;
//    @Autowired
//    private StringRedisTemplate redisTemplate;
//
//    ExecutorService executor = Executors.newFixedThreadPool(10);
//
//
//    public R dispatcher(StepVo stepVo) {
//        JdAppStoreConfig jdAppStoreConfig = jdAppStoreConfigMapper.selectOne(Wrappers.<JdAppStoreConfig>lambdaQuery()
//                .eq(JdAppStoreConfig::getSkuPrice, stepVo.getPrice()).eq(JdAppStoreConfig::getGroupNum, stepVo.getGroupNum()));
//        log.info("查询ck实际数量的5倍");
//        Integer count = jdCkMapper.selectCount(Wrappers.<JdCk>lambdaQuery().eq(JdCk::getIsEnable, 1));
//        int size = BigDecimal.valueOf(count).divide(new BigDecimal(stepVo.getNum()), BigDecimal.ROUND_FLOOR, 0).intValue();
////        List<JdCk> jdCks = jdCkMapper.selectList();
//        int[] ints = PreUtils.randomCommon(1, size, 1);
//        Page<JdCk> jdCkPage = jdCkMapper.selectPage(new Page<>(ints[0], stepVo.getNum()), Wrappers.<JdCk>lambdaQuery().eq(JdCk::getIsEnable, 1));
//        List<JdCk> zhiDingList = jdCkPage.getRecords();
//
//        List<JdLocalUrl> jdLocalUrls = jdLocalUrlMapper.selectList(Wrappers.<JdLocalUrl>lambdaQuery().eq(JdLocalUrl::getGroupNum, stepVo.getGroupNum()).eq(JdLocalUrl::getIsEnable, 1));
//        List<JdLocalUrl> StepJdLocalUrls = jdLocalUrls.stream().sorted(Comparator.comparingInt(it -> it.getStep())).collect(Collectors.toList());
//        Map<String, JdLocalUrl> mapUrls = StepJdLocalUrls.stream().collect(Collectors.toMap(it -> it.getTag(), it -> it));
//        JdLocalUrl getTokenKey1 = mapUrls.get("getTokenKey");
//        JdLocalUrl getTokenKey2 = mapUrls.get("orderListByTokenKey");
//        List<ThreadHttpRequest> listThread = new ArrayList<>();
//        for (JdCk jdCk : zhiDingList) {
//            TokenKeyResVo tokenKeyResVo = null;
//            try {
//                String genTokenBody = getTokenBody(getTokenKey1, jdCk);
//                R r = com.alibaba.fastjson.JSON.parseObject(genTokenBody, R.class);
//                tokenKeyResVo = getTokenKeyResVo(r);
//            } catch (Exception e) {
//                continue;
//            }
//            log.info("执行线程池开始");
//            String[] stepJdLocalUrl = getTokenKey2.getUrl().split(";");
//            String urlPost = stepJdLocalUrl[new Random().nextInt(stepJdLocalUrl.length)];
//            ThreadHttpRequest threadHttpRequest = new ThreadHttpRequest(urlPost, jdAppStoreConfig.getSkuId(), jdAppStoreConfig.getSkuPrice(), tokenKeyResVo.getTokenKey());
//            listThread.add(threadHttpRequest);
//        }
//        log.info("注意++++++++++++++++获取keyToken的数量为应该获取数量stepVo：{}：" +
//                "msg:[threadHttpRequest:{}]", stepVo.getNum(), listThread.size());
//        if (CollUtil.isNotEmpty(listThread)) {
//            for (ThreadHttpRequest threadHttpRequest : listThread) {
//                executor.submit(threadHttpRequest);
//            }
//        }
//        return R.ok();
//    }
//
//    private TokenKeyResVo getTokenKeyResVo(R r) {
//        TokenKeyResVo tokenKeyResVo = null;
//        try {
//            tokenKeyResVo = com.alibaba.fastjson.JSON.parseObject(r.getData().toString(), TokenKeyResVo.class);
//            if (StrUtil.isBlank(tokenKeyResVo.getTokenKey())) {
//                return null;
//            }
//        } catch (Exception e) {
//            log.info("没有获取到tokenkey:msg:{}", com.alibaba.fastjson.JSON.toJSONString(r));
//        }
//        if (ObjectUtil.isNull(tokenKeyResVo)) {
//            return null;
//        }
//        return tokenKeyResVo;
//    }
//
//    public String getTokenBody(JdLocalUrl stepJdLocalUrl, JdCk jdCk) {
//        log.info("执行getKeny");
//        JSON paramMap = new JSONObject();
//        ((JSONObject) paramMap).put("cookie", jdCk.getCk());
//        String genTokenBody = HttpRequest.post(stepJdLocalUrl.getUrl())
//                .header(Header.HOST, "api.m.jd.com")//头信息，多个头信息多次调用此方法即可
//                .header(Header.CACHE_CONTROL, "no-cache")
//                .header(Header.USER_AGENT, "okhttp/3.12.1")
//                .body(paramMap)//表单内容
//                .timeout(10000)//超时，毫秒
//                .execute().body();
//        return genTokenBody;
//    }
//
//    private String orderListByTokenKey(JdLocalUrl getTokenKey2, JdAppStoreConfig jdAppStoreConfig, String tokenKey) {
//        log.info("----- 执行第二部tokenKey");
//        JSON paramMap = new JSONObject();
//        ((JSONObject) paramMap).put("tokenKey", tokenKey);
//        ((JSONObject) paramMap).put("skuId", jdAppStoreConfig.getSkuId());
//        ((JSONObject) paramMap).put("skuPrice", jdAppStoreConfig.getSkuPrice());
//        String genTokenBody = HttpRequest.post(getTokenKey2.getUrl())
//                .header(Header.USER_AGENT, "okhttp/3.12.1")
//                .body(paramMap)//表单内容
//                .timeout(5000)//超时，毫秒 2分钟
//                .execute().body();
//        return genTokenBody;
//    }
//
//}
