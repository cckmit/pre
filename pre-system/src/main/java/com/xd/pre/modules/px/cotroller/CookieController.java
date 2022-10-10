package com.xd.pre.modules.px.cotroller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xd.pre.common.utils.R;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.modules.px.service.CkService;
import com.xd.pre.modules.px.service.NewWeiXinPayUrl;
import com.xd.pre.modules.px.service.ProxyProductService;
import com.xd.pre.modules.px.service.TokenKeyService;
import com.xd.pre.modules.sys.domain.JdCk;
import com.xd.pre.modules.sys.domain.JdOrderPt;
import com.xd.pre.modules.sys.mapper.JdCkMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.Queue;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/jd")
@RestController
@Slf4j
public class CookieController {

    @Autowired
    private CkService ckService;

    @Autowired
    private TokenKeyService TokenKeyService;
    @Autowired
    private NewWeiXinPayUrl newWeiXinPayUrl;
    @Resource
    private JdCkMapper jdCkMapper;
    @Autowired
    private TokenKeyService tokenKeyService;

    @Autowired
    private ProxyProductService proxyProductService;

    @GetMapping("checkFind")
    public R checkList(@Param("uuid") String uuid) {
        List<JdCk> jdCks = jdCkMapper.selectList(Wrappers.<JdCk>lambdaQuery().eq(JdCk::getFileName, uuid));
        return R.ok(jdCks);
    }


    @PostMapping("checkList")
    public R checkList(@RequestParam("file") MultipartFile file, String skuId) throws Exception {
        byte[] bytes = file.getBytes();
        String trim = new String(bytes).trim();
        String[] split = trim.split("\n");
        String originalFilename = file.getOriginalFilename();
        Map<String, Object> returnMap = new HashMap<>();
        for (int i = 0; i < split.length; i++) {
            String str = split[i].trim();
            try {
                if (str.contains("pt_pin=")) {
                    Integer indexof = str.indexOf("pt_pin=");
                    str = str.substring(indexof).trim();
                    str.replace("pt_pin=","pin=");
                }
                if (str.contains("pin=")) {
                    Integer indexof = str.indexOf("pin=");
                    str = str.substring(indexof).trim();
                }
                JdCk jdCk = new JdCk();
                jdCk.setCk(str.trim());
                jdCk.setSkuId(skuId);
                jdCk.setFileName(originalFilename);
                log.info("添加成功msg:{}", i);
                this.sendMessageNotTime(this.check_data_queue, JSON.toJSONString(jdCk));
            } catch (Exception e) {
                log.error("....");
            }
        }
        return R.ok(returnMap);
    }

    @PostMapping("/ckCheckOut")
    public R ckCheck(@RequestBody JdCk jdCk) {
        log.info("检查外部账号msg:", jdCk);
        String pt_pin = null;
        try {
            pt_pin = PreUtils.get_pt_pin(jdCk.getCk().trim());
        } catch (Exception e) {
            return R.error("请使用这种格式必须有pt_pin,格式如下:guid=e1a098a7262a3406e55ee3bad30f37914b52bac51f3aae38994aee44a1736329; thor1=; pt_key=app_openAAJiITgbADBVgFBzmN5mz1cPOSEsiv6afMmauRfA8nw073hnjX8NwZjeRn8BCitwblrjRzeOzv0; pt_pin=jd_XNlJHZPQStIs; pwdt_id=jd_XNlJHZPQStIs; sid=548fc2ee2a95850f1aaff7b3a437021w; pin=jd_XNlJHZPQStIs; wskey=AAJhtEwXAEC02UD_sK-zevRl4HQhIKpO-xgaZDRuTTWJyls72K1_maYPxVDur1P7AyiPP8iD64RSz4giuSsWGtv2-j0gAGNB;unionwsws={\"devicefinger\":\"VQge9zH83L6VXxLRt5obgqxAqp6vNvbthjVnYeIxsfzcERPqbT1aPrYzalsVdnOd3XQ1GO8l3YJqX1kaLXTeqgT72atAhIXeO872azhX27fMKkQKfpA2Ex\",\"jmafinger\":\"V91yEmzU9OkRlATBXyYgcIU9hnh80em4NCdX6mviXeF9BHtX3UO3tXPaqK3TKjn3gdPZvk7fDscZbeCO8f4HV2z\"};");
        }
        if (ObjectUtil.isNull(pt_pin)) {
            return R.error("请使用这种格式必须有pt_pin,格式如下:pin=jd_fmmUjXFcxFEC;wskey=AAJiTYEhAEBLM0lxl2ncZDCoo5cfrFVlh6F3NOR7sZ9klcYhY7uImrUctHH6sAaPCYwhzIbcCoeojyMnHCYUMzWyQLJ5IzNN;whwswswws=hDIhPcuZeqhMmvIhafxfowISXSTK7xXQQEn9Ur53R2WoRabdHT1JL8y98rlsmi9qh8c5eZRNPpoJbup08JT-cSw;unionwsws=");
        }
        JdCk jdCkDb = jdCkMapper.selectOne(Wrappers.<JdCk>lambdaQuery().eq(JdCk::getPtPin, pt_pin));
        if (ObjectUtil.isNotNull(jdCkDb)) {
            jdCkDb.setCk(jdCk.getCk());
            jdCkMapper.updateById(jdCkDb);
        } else {
            JdCk build = JdCk.builder().ptPin(pt_pin).createTime(new Date()).isEnable(1).ck(jdCk.getCk()).build();
            jdCkMapper.insert(build);
        }
        List<JdOrderPt> jdOrderPts = newWeiXinPayUrl.checkCkAndMatch(jdCk.getCk(), null, null);
        if (CollUtil.isEmpty(jdOrderPts)) {
            return null;
        }
        for (JdOrderPt jdOrderPt : jdOrderPts) {
            jdCkDb = jdCkMapper.selectOne(Wrappers.<JdCk>lambdaQuery().eq(JdCk::getPtPin, pt_pin));
        }
        return R.ok(jdOrderPts);
    }

    @PostMapping("/ckCheck")
    public R ckCheck(@RequestParam("id") Integer id, String skuId) {
        List<JdCk> jdCks = jdCkMapper.selectList(Wrappers.<JdCk>lambdaQuery().gt(JdCk::getId, id));
        for (JdCk jdCk : jdCks) {
            jdCk.setSkuId(skuId);
            this.sendMessageNotTime(this.check_data_queue, JSON.toJSONString(jdCk));
            System.out.println("++++++++++++++++++++");
        }
        return R.ok();
    }

    @Resource(name = "check_data_queue")
    private Queue check_data_queue;

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    // 发送消息，destination是发送到的队列，message是待发送的消息
    private void sendMessageNotTime(Destination destination, final String message) {
        jmsMessagingTemplate.convertAndSend(destination, message);
    }

}
