package com.xd.pre.modules.sys.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xd.pre.common.constant.PreConstant;
import com.xd.pre.common.utils.R;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.modules.data.tenant.PreTenantContextHolder;
import com.xd.pre.modules.px.douyin.huadan.ReadDto;
import com.xd.pre.modules.px.douyin.huadan.ReadYonghuiDto;
import com.xd.pre.modules.px.service.CkService;
import com.xd.pre.modules.px.service.NewWeiXinPayUrl;
import com.xd.pre.modules.px.task.ProductProxyTask;
import com.xd.pre.modules.px.vo.resvo.*;
import com.xd.pre.modules.px.vo.sys.CkFindListVO;
import com.xd.pre.modules.px.vo.tmpvo.appstorevo.JdMchOrderAndCard;
import com.xd.pre.modules.security.util.JwtUtil;
import com.xd.pre.modules.sys.domain.*;
import com.xd.pre.modules.sys.dto.ExcelCarMyDto;
import com.xd.pre.modules.sys.mapper.*;
import com.xd.pre.modules.sys.service.impl.JdService;
import com.xd.pre.modules.sys.vo.WriteOffCodeStaVo;
import com.xd.pre.security.PreSecurityUser;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.jms.Queue;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ck")
@Slf4j
public class JdCkSysController {
    @Resource
    private JdCkMapper jdCkMapper;
    @Autowired
    private CkService ckService;

    @Resource
    private JdAppStoreConfigMapper jdAppStoreConfigMapper;
    @Resource
    private JdLocalUrlMapper jdLocalUrlMapper;
    @Resource
    private ProxyAddressProductMapper proxyAddressProductMapper;
    @Resource
    private JdProxyIpPortMapper jdProxyIpPortMapper;
    @Autowired
    private JdService jdService;
    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;
    @Resource
    private JdTenantMapper jdTenantMapper;

    @Resource(name = "product_proxy_task")
    private Queue product_proxy_task;

    @Resource
    private JdMchOrderMapper jdMchOrderMapper;

    @Resource
    private JdOrderPtMapper jdOrderPtMapper;

    @Autowired
    private NewWeiXinPayUrl weiXinPayUrl;

    @Autowired
    private ProductProxyTask productProxyTask;
    @Resource
    private JdLogMapper jdLogMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource
    private JdCkZhidengMapper jdCkZhidengMapper;
    @Resource
    private DouyinAppCkMapper douyinAppCkMapper;

    @Resource
    private DouyinDeviceIidMapper douyinDeviceIidMapper;

    @Resource
    private DouyinRechargePhoneMapper douyinRechargePhoneMapper;

    @Resource
    private DouyinHexiaoPhoneMapper douyinHexiaoPhoneMapper;

    @GetMapping("douyinYonghuiAble")
    public R douyinYonghuiAble(Integer id, Integer isEnable) {
        DouyinHexiaoPhone douyinHexiaoPhone = douyinHexiaoPhoneMapper.selectById(id);
        douyinHexiaoPhone.setIsEnable(isEnable);
        this.douyinHexiaoPhoneMapper.updateById(douyinHexiaoPhone);
        return R.ok();
    }

    @GetMapping("douyinYonghuiPage")
    public R douyinYonghuiPage(Page<DouyinHexiaoPhone> page, DouyinHexiaoPhone douyinHexiaoPhone) {
        LambdaQueryWrapper<DouyinHexiaoPhone> objectLambdaQueryWrapper = Wrappers.lambdaQuery();
        if (ObjectUtil.isNotNull(douyinHexiaoPhone) && StrUtil.isNotBlank(douyinHexiaoPhone.getHexiaoPhone())) {
            objectLambdaQueryWrapper.like(DouyinHexiaoPhone::getHexiaoPhone, douyinHexiaoPhone.getHexiaoPhone());
        }
        if (ObjectUtil.isNotNull(douyinHexiaoPhone) && ObjectUtil.isNotNull(douyinHexiaoPhone.getHexiaoPhoneStart())) {
            objectLambdaQueryWrapper.ge(DouyinHexiaoPhone::getHexiaoPhoneStart, douyinHexiaoPhone.getHexiaoPhoneStart());
        }
        if (ObjectUtil.isNotNull(douyinHexiaoPhone) && ObjectUtil.isNotNull(douyinHexiaoPhone.getHexiaoPhoneEnd())) {
            objectLambdaQueryWrapper.le(DouyinHexiaoPhone::getHexiaoPhoneEnd, douyinHexiaoPhone.getHexiaoPhoneEnd());
        }
        page = douyinHexiaoPhoneMapper.selectPage(page, objectLambdaQueryWrapper);
        return R.ok(page);
    }

    @PostMapping("/uploadYonghui")
    @Transactional
    public R uploadYonghui(@RequestParam("file") MultipartFile file) throws Exception {
        Long currentTenantId = PreTenantContextHolder.getCurrentTenantId();
        log.info("上传永辉卡当前租户msg:{}", currentTenantId);
        String originalFilename = file.getOriginalFilename();
        ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
        List<ReadYonghuiDto> readDtos = reader.readAll(ReadYonghuiDto.class);
        List<DouyinHexiaoPhone> douyinRechargePhones = this.douyinHexiaoPhoneMapper.
                selectList(Wrappers.<DouyinHexiaoPhone>lambdaQuery().eq(DouyinHexiaoPhone::getBatchNum, readDtos.get(0).get批号()));
        if (CollUtil.isNotEmpty(douyinRechargePhones)) {
            for (DouyinHexiaoPhone douyinRechargePhone : douyinRechargePhones) {
                douyinRechargePhone.setIsEnable(PreConstant.ZERO);
                douyinHexiaoPhoneMapper.updateById(douyinRechargePhone);
            }
        }
        for (ReadYonghuiDto readDto : readDtos) {
            DateUtil.parse(readDto.get有效时间开始时间().trim());
            DateUtil.parse(readDto.get有效结束时间().trim());
            DouyinHexiaoPhone build = DouyinHexiaoPhone.builder().hexiaoPhone(readDto.get手机号码())
                    .isEnable(readDto.get是否有效()).hexiaoPhoneStart(readDto.get有效时间开始时间())
                    .hexiaoPhoneEnd(readDto.get有效结束时间().trim()).batchNum(readDto.get批号().trim()).createTime(new Date()).fileName(originalFilename)
                    .hexiaoName(readDto.get核销人员()).build();
            douyinHexiaoPhoneMapper.insert(build);
        }
        return R.ok();
    }


    @GetMapping("douyinPhoneDelete")
    public R douyinPhonePage(Integer id) {
        this.douyinRechargePhoneMapper.deleteById(id);
        return R.ok();
    }


    @GetMapping("douyinPhonePage")
    public R douyinPhonePage(Page<DouyinRechargePhone> page, DouyinRechargePhone douyinRechargePhone) {
        LambdaQueryWrapper<DouyinRechargePhone> objectLambdaQueryWrapper = Wrappers.lambdaQuery();
        if (StrUtil.isNotBlank(douyinRechargePhone.getRechargePhone())) {
            objectLambdaQueryWrapper.like(DouyinRechargePhone::getRechargePhone, douyinRechargePhone.getRechargePhone());
        }
        page = douyinRechargePhoneMapper.selectPage(page, objectLambdaQueryWrapper);
        return R.ok(page);
    }

    @PostMapping("/uploadPhone")
    @Transactional
    public R uploadPhone(@RequestParam("file") MultipartFile file) throws Exception {
        Long currentTenantId = PreTenantContextHolder.getCurrentTenantId();
        log.info("上传当前租户msg:{}", currentTenantId);
        byte[] bytes = file.getBytes();
        String originalFilename = file.getOriginalFilename();
        ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
        List<ReadDto> readDtos = reader.readAll(ReadDto.class);
        List<DouyinRechargePhone> douyinRechargePhones = this.douyinRechargePhoneMapper.
                selectList(Wrappers.<DouyinRechargePhone>lambdaQuery().eq(DouyinRechargePhone::getBatchNum, readDtos.get(0).get批号()));
        if (CollUtil.isNotEmpty(douyinRechargePhones)) {
            for (DouyinRechargePhone douyinRechargePhone : douyinRechargePhones) {
                douyinRechargePhone.setIsEnable(PreConstant.ZERO);
                douyinRechargePhoneMapper.updateById(douyinRechargePhone);
            }
        }
        for (ReadDto readDto : readDtos) {
            DouyinRechargePhone build = DouyinRechargePhone.builder().rechargePhone(readDto.get手机号码().trim()).price(readDto.get充值金额()).orderStatus(PreConstant.ZERO)
                    .createTime(new Date()).fileName(originalFilename.trim()).isEnable(readDto.get是否充值()).batchNum(readDto.get批号()).build();
            douyinRechargePhoneMapper.insert(build);
        }
        return R.ok();
    }

    //adddeviceIdAndIid
    //adddeviceIdAndIid
//    adddeviceIdAndIid
    @PostMapping("/adddeviceIdAndIid")
    public R AdddeviceIdAndIid(@RequestBody DouyinDeviceIid douyinDeviceIid) {
        if (StrUtil.isBlank(douyinDeviceIid.getDeviceId()) || StrUtil.isBlank(douyinDeviceIid.getIid())) {
            return R.error("缺少参数");
        }
        douyinDeviceIid.setId(null);
        this.douyinDeviceIidMapper.insert(douyinDeviceIid);
        return R.ok("添加成功");
    }

    @GetMapping("douyinDeviceIdPage")
    public R douyinDeviceIdPage(Page<DouyinDeviceIid> page, DouyinDeviceIid deviceIid) {
        LambdaQueryWrapper<DouyinDeviceIid> objectLambdaQueryWrapper = Wrappers.lambdaQuery();
        if (StrUtil.isNotBlank(deviceIid.getDeviceId())) {
            objectLambdaQueryWrapper.like(DouyinDeviceIid::getDeviceId, deviceIid.getDeviceId());
        }
        if (StrUtil.isNotBlank(deviceIid.getIid())) {
            objectLambdaQueryWrapper.like(DouyinDeviceIid::getIid, deviceIid.getIid());
        }
        page = douyinDeviceIidMapper.selectPage(page, objectLambdaQueryWrapper);
        return R.ok(page);
    }


    @GetMapping("douyinDeviceEnAble")
    public R douyinDeviceDelete(Integer id, Integer isEnable) {
        DouyinDeviceIid douyinDeviceIid = douyinDeviceIidMapper.selectById(id);
        douyinDeviceIid.setIsEnable(isEnable);
        douyinDeviceIidMapper.updateById(douyinDeviceIid);
        return R.ok();
    }

    @PostMapping("/uploadDy")
    @Transactional
//    @PreAuthorize("hasAuthority('ck:douyin:upload')")
    public R uploadDy(@RequestParam("file") MultipartFile file) throws Exception {
        Long currentTenantId = PreTenantContextHolder.getCurrentTenantId();
        log.info("上传当前租户msg:{}", currentTenantId);
        byte[] bytes = file.getBytes();
        String originalFilename = file.getOriginalFilename();
        String trim = new String(bytes).trim();
        String[] split = trim.split("\n");
        OkHttpClient client = new OkHttpClient();
        for (int i = 0; i < split.length; i++) {
            String douYinAppCk = split[i].trim();
            try {
                log.info("当前抖音ckmsg:{}，开始查询uid检验是否重复", douYinAppCk);
                Request request = new Request.Builder()
                        .url("https://ecom.snssdk.com/aweme/v2/commerce/mall/favorite/feed?count=1")
                        .get()
                        .addHeader("Cookie", douYinAppCk)
                        .build();
                Response response = client.newCall(request).execute();
                String uidData = response.body().string();
                String item_cardsStr = JSON.parseObject(uidData).getString("item_cards");
                List<JSONObject> item_cards = JSON.parseArray(item_cardsStr, JSONObject.class);
                String productStr = item_cards.get(PreConstant.ZERO).getString("product");
                String recommend_info = JSON.parseObject(productStr).getString("recommend_info");
                if (StrUtil.isNotBlank(recommend_info) && recommend_info.contains("uid") && recommend_info.contains("gid")) {
                    String uid = JSON.parseObject(recommend_info, JSONObject.class).getString("uid");
                    DouyinAppCk build = DouyinAppCk.builder().uid(uid).ck(douYinAppCk).isEnable(PreConstant.ONE).fileName(originalFilename).createTime(new Date()).build();
                    DouyinAppCk douyinAppCkDb = douyinAppCkMapper.selectOne(Wrappers.<DouyinAppCk>lambdaQuery().eq(DouyinAppCk::getUid, uid));
                    if (ObjectUtil.isNotNull(douyinAppCkDb)) {
                        log.info("已经存在msg:{}", JSON.toJSONString(douyinAppCkDb));
                        douyinAppCkMapper.deleteById(douyinAppCkDb.getId());
                        douyinAppCkMapper.insert(build);
                    } else {
                    }
                    douyinAppCkMapper.insert(build);
                }
            } catch (Exception e) {
                log.error("当前ck报错msg:{},ck:{}", e.getMessage(), douYinAppCk);
            }
        }
        return R.ok();
    }

    @GetMapping("douyinPage")
    public R douyinPage(Page<DouyinAppCk> page, DouyinAppCk douyinAppCk) {
        LambdaQueryWrapper<DouyinAppCk> wrapper = Wrappers.lambdaQuery();
        if (StrUtil.isNotBlank(douyinAppCk.getUid())) {
            wrapper.like(DouyinAppCk::getUid, douyinAppCk.getUid());
        }
        if (StrUtil.isNotBlank(douyinAppCk.getCk())) {
            wrapper.like(DouyinAppCk::getCk, douyinAppCk.getCk());
        }
        page = douyinAppCkMapper.selectPage(page, wrapper);
        return R.ok(page);
    }

    @GetMapping("douyinAble")
    public R deleteDouyin(Integer id, Integer isEnable) {
        DouyinAppCk douyinAppCk = douyinAppCkMapper.selectById(id);
        if (ObjectUtil.isNull(douyinAppCk)) {
            return R.error("当前账号不存在");
        }
        log.info("抖音ck保存数据:{}", JSON.toJSONString(douyinAppCk));
        douyinAppCk.setIsEnable(isEnable);
        douyinAppCkMapper.updateById(douyinAppCk);
        log.info("删除抖音账号id{}");
        return R.ok();
    }


    @PostMapping("uploadZhideng")
    @Transactional
    public R zhidengFileName(@RequestParam("file") MultipartFile file) throws Exception {
        byte[] bytes = file.getBytes();
        String originalFilename = file.getOriginalFilename();
        String trim = new String(bytes).trim();
        String[] split = trim.split("\n");
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            log.info("开始查重:{}", i);
            String[] allSplit = s.split("----");
            if (allSplit.length != 4) {
                log.error("当前账号有问题");
                return R.error(s + "这行有问题------" + (i + 1));
            }
        }
        for (int i = 0; i < split.length; i++) {
            log.info("开始组装入库代码:{}", i);
            String s = split[i];
            String[] allSplit = s.split("----");
            //姓名。账号 密码。ck
            String s0 = allSplit[0];
            String s1 = allSplit[1];
            String s2 = allSplit[2];
            String s3 = allSplit[3];
            s3 = s3.replace("\r", "");
            s3 = s3.replace(" ", "");
            s3 = s3.trim();
            String pt_pin = PreUtils.get_pt_pin(s3);
            JdCkZhideng jdCkZhideng = JdCkZhideng.builder().ptPin(pt_pin).accountName(s0).account(s1).password(s2).mck(s3)
                    .mckTime(new Date()).isEnable(PreConstant.ONE).createTime(new Date()).fileName(originalFilename).build();
            JdCkZhideng jdCkZhidengDb = this.jdCkZhidengMapper.selectOne(Wrappers.<JdCkZhideng>lambdaQuery().eq(JdCkZhideng::getPtPin, pt_pin));
            if (ObjectUtil.isNotNull(jdCkZhidengDb)) {
                jdCkZhideng.setId(jdCkZhidengDb.getId());
                jdCkZhidengMapper.deleteById(jdCkZhidengDb.getId());
                log.info("更新账号msg:{}", jdCkZhidengDb);
            }

            log.info("账号入库msg:{}", jdCkZhideng);
            this.jdCkZhidengMapper.insert(jdCkZhideng);
        }
        return R.ok(split[0]);
    }

    @GetMapping("zhidengFileName")
    public R zhidengFileName() {
        LambdaQueryWrapper<JdCkZhideng> wrapper = Wrappers.<JdCkZhideng>lambdaQuery().groupBy(JdCkZhideng::getFileName);
        wrapper.select(JdCkZhideng::getFileName);
        List<JdCkZhideng> jdCkZhidengs = jdCkZhidengMapper.selectList(wrapper);
        if (CollUtil.isNotEmpty(jdCkZhidengs)) {
            List<String> fileNames = jdCkZhidengs.stream().map(it -> it.getFileName()).distinct().collect(Collectors.toList());
            return R.ok(fileNames);
        }
        return R.ok();
    }

    @GetMapping("writeOffById")
    @Transactional
    public R writeOffById(JdCkZhideng jdCkZhideng) {
        if (ObjectUtil.isNull(jdCkZhideng.getId())) {
            return R.error("核销id为空");
        }
        JdCkZhideng zhideng = this.jdCkZhidengMapper.selectById(jdCkZhideng.getId());
        if (ObjectUtil.isNull(zhideng.getWriteOffCode())) {
            return R.error("当前账号不能核销");
        }
        zhideng.setIsWriteOffCode(1);
        zhideng.setWriteOffCodeTime(new Date());
        this.jdCkZhidengMapper.updateById(zhideng);
        return R.ok("核销完成");

    }

    @GetMapping("batchWriteOff")
    @Transactional
    public R batchWriteOff(JdCkZhideng jdCkZhideng) {
        if (StrUtil.isBlank(jdCkZhideng.getFileName())) {
            return R.error("批量核销名字为空,或者核销数据为空");
        }
        LambdaQueryWrapper<JdCkZhideng> wrapper = Wrappers.<JdCkZhideng>lambdaQuery();
        if (StrUtil.isNotBlank(jdCkZhideng.getFileName())) {
            wrapper.eq(JdCkZhideng::getFileName, jdCkZhideng.getFileName());
        }
        List<JdCkZhideng> jdCkZhidengs = jdCkZhidengMapper.selectList(wrapper);
        if (CollUtil.isEmpty(jdCkZhidengs)) {
            return R.error("文件名查不到数据");
        }
        List<String> pinOffCodes = jdCkZhidengMapper.batchNotProductOffCode(jdCkZhideng.getFileName());
        if (CollUtil.isNotEmpty(pinOffCodes)) {
            log.info("处理生成了订单的数据,生成待核销的数据msg:{}", pinOffCodes);
            for (String pinOffCode : pinOffCodes) {
                JdCkZhideng zhideng = this.jdCkZhidengMapper.selectOne(Wrappers.<JdCkZhideng>lambdaQuery().eq(JdCkZhideng::getPtPin, pinOffCode));
                Integer offCode = jdCkZhidengMapper.selectOffCode();
                zhideng.setWriteOffCode(offCode);
                this.jdCkZhidengMapper.updateById(zhideng);
            }
            log.info("处理没有生成订单的核销编码直接下架");
            List<JdCkZhideng> notOrder = jdCkZhidengs.stream().filter(it -> !pinOffCodes.contains(it)).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(notOrder)) {
                for (JdCkZhideng zhideng : notOrder) {
                    zhideng.setIsEnable(PreConstant.THREE);
                    jdCkZhidengMapper.updateById(zhideng);
                }
            }
        } else {
            for (JdCkZhideng ckZhideng : jdCkZhidengs) {
                ckZhideng.setIsEnable(PreConstant.THREE);
                jdCkZhidengMapper.updateById(ckZhideng);
            }
        }
        return R.ok();

    }

    @GetMapping("zhidengOffCode")
    public R zhidengOffCode(Page<JdCkZhideng> page, JdCkZhideng jdCkZhideng) {
        LambdaQueryWrapper<JdCkZhideng> wrapper = Wrappers.<JdCkZhideng>lambdaQuery();
        wrapper.orderByDesc(JdCkZhideng::getWriteOffCode);
        wrapper.isNotNull(JdCkZhideng::getWriteOffCode);
        if (ObjectUtil.isNotNull(jdCkZhideng.getIsWriteOffCode())) {
            wrapper.eq(JdCkZhideng::getIsWriteOffCode, jdCkZhideng.getIsWriteOffCode());
        }
        if (ObjectUtil.isNotNull(jdCkZhideng.getWriteOffCode())) {
            wrapper.like(JdCkZhideng::getWriteOffCode, jdCkZhideng.getWriteOffCode());
        }
        if (StrUtil.isNotBlank(jdCkZhideng.getFileName())) {
            wrapper.eq(JdCkZhideng::getFileName, jdCkZhideng.getFileName());
        }
        if (StrUtil.isNotBlank(jdCkZhideng.getAccount())) {
            wrapper.like(JdCkZhideng::getAccount, jdCkZhideng.getAccount());
        }
        R r = getR(page, wrapper);
        return r;
    }

    @GetMapping("zhidengByFileName")
    public R zhidengByFileName(Page<JdCkZhideng> page, JdCkZhideng jdCkZhideng) {
        LambdaQueryWrapper<JdCkZhideng> wrapper = Wrappers.<JdCkZhideng>lambdaQuery();
        if (StrUtil.isNotBlank(jdCkZhideng.getFileName())) {
            wrapper.eq(JdCkZhideng::getFileName, jdCkZhideng.getFileName());
        }
        if (ObjectUtil.isNotNull(jdCkZhideng.getIsEnable())) {
            wrapper.eq(JdCkZhideng::getIsEnable, jdCkZhideng.getIsEnable());
        }
        if (StrUtil.isNotBlank(jdCkZhideng.getAccount())) {
            wrapper.like(JdCkZhideng::getAccount, jdCkZhideng.getAccount());
        }
        wrapper.isNull(JdCkZhideng::getWriteOffCode);
        wrapper.orderByDesc(JdCkZhideng::getWriteOffCode);
        log.info("查询订单账号信息");
        return getR(page, wrapper);
    }

    private R getR(Page<JdCkZhideng> page, LambdaQueryWrapper<JdCkZhideng> wrapper) {
        page = jdCkZhidengMapper.selectPage(page, wrapper);
        if (CollUtil.isNotEmpty(page.getRecords())) {
            List<String> pins = page.getRecords().stream().map(it -> it.getPtPin()).collect(Collectors.toList());
            List<JdOrderPt> jdOrderPts = jdOrderPtMapper.selectList(Wrappers.<JdOrderPt>lambdaQuery().in(JdOrderPt::getPtPin, pins));
            Map<String, List<JdOrderPt>> mapbypin = jdOrderPts.stream().collect(Collectors.groupingBy(JdOrderPt::getPtPin));
            List<JdCkZhideng> jdCkZhidengs = page.getRecords();
            for (JdCkZhideng jdCkZhidengOne : jdCkZhidengs) {
                if (CollUtil.isNotEmpty(mapbypin)) {
                    List<JdOrderPt> jdOrderPtIns = mapbypin.get(jdCkZhidengOne.getPtPin());
                    if (CollUtil.isNotEmpty(jdOrderPtIns)) {
                        List<JdOrderPt> collect0 = jdOrderPtIns.stream().filter(it -> it.getIsWxSuccess() == PreConstant.ZERO).distinct().collect(Collectors.toList());
                        List<JdOrderPt> collect1 = jdOrderPtIns.stream().filter(it -> it.getIsWxSuccess() == PreConstant.ONE && ObjectUtil.isNull(it.getPaySuccessTime())).distinct().collect(Collectors.toList());
                        List<JdOrderPt> collect2 = jdOrderPtIns.stream().filter(it -> it.getIsWxSuccess() == PreConstant.ONE && ObjectUtil.isNotNull(it.getPaySuccessTime())).distinct().collect(Collectors.toList());
                        jdCkZhidengOne.setFailPay(collect0.size());
                        jdCkZhidengOne.setToBePay(collect1.size());
                        jdCkZhidengOne.setSuccessPay(collect2.size());
                        jdOrderPtIns = jdOrderPtIns.stream().sorted(Comparator.comparing(JdOrderPt::getCreateTime).reversed()).collect(Collectors.toList());
                        jdCkZhidengOne.setJdOrderPtList(jdOrderPtIns);
                        BigDecimal reduce = jdOrderPtIns.stream()
                                .filter(it -> it.getIsWxSuccess() == PreConstant.ONE && ObjectUtil.isNotNull(it.getPaySuccessTime())).
                                        map(it -> it.getSkuPrice()).reduce(BigDecimal.ZERO, BigDecimal::add);
                        int successMoney = reduce.intValue();
                        jdCkZhidengOne.setSuccessMoney(successMoney);
                    }
                }
            }
        }
        return R.ok(page);
    }

    @GetMapping("writeOffCodestatistics")
    public R writeOffCodestatistics(String start, String end) {
        try {
            List<WriteOffCodeStaVo> writeOffCodeStaVos = this.jdCkZhidengMapper.selectWriteOffCodestatistics(start, end);
            return R.ok(writeOffCodeStaVos);
        } catch (Exception e) {

        }
        return null;
    }

    @GetMapping("statistics")
    public R statistics(String start, String end) {
        List<Map<String, Object>> maps = jdLogMapper.selectStatistics(start, end);
        Integer sum = 0;
        Integer successNum = 0;
        for (Map<String, Object> map : maps) {
            Integer orderStatus = Integer.valueOf(map.get("orderStatus").toString());
            Integer num = Integer.valueOf(map.get("num").toString());
            if (orderStatus == 2) {
                successNum += num;
            }
            sum += num;
        }
        BigDecimal divide = new BigDecimal(successNum).divide(new BigDecimal(sum), 2, BigDecimal.ROUND_HALF_UP);
        return R.ok(divide.toString());
    }


    @GetMapping("kami")
    public R kami(@RequestParam("id") Integer id) {
        JdMchOrder jdMchOrder = jdMchOrderMapper.selectById(id);
        weiXinPayUrl.getCartNumAndMy(jdMchOrder);
        productProxyTask.notifySuccess(jdMchOrder);
        return R.ok();
    }

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("notify")
    public R notify(HttpServletRequest request, @RequestParam("id") Integer id) {
        PreSecurityUser securityUser = jwtUtil.getUserFromToken(request);
        String username = securityUser.getUsername();
        log.info("订单号：{},当前用户名：{}", id, username);
        JdMchOrder jdOrderPt = jdMchOrderMapper.selectById(id);
        weiXinPayUrl.getCartNumAndMy(jdOrderPt);
        if (username.equals("douyin")) {
            PreTenantContextHolder.setCurrentTenantId(2L);
        }
        productProxyTask.notifySuccess(jdOrderPt);
        return R.ok();
    }

    @GetMapping("/flowing_water")
    public R flowing_water(@RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime) {
        log.info("查询流水记录");
        FlowingWaterResVo flowingWater = jdService.flowingWater(startTime, endTime);
        Page<FlowingWaterResVo> objectPage = new Page<>();
        objectPage.setTotal(1);
        objectPage.setRecords(Arrays.asList(flowingWater));
        return R.ok(objectPage);
    }

    @GetMapping("/stock")
    public R stock() {
        log.info("查询库存");
        List<StockRes> resList = jdService.selectStock();
        Page<StockRes> objectPage = new Page<>(1, 10);
        objectPage.setRecords(resList);
        objectPage.setTotal(resList.size());
        return R.ok(objectPage);
    }

    @GetMapping("/refund")
    public R stock(@RequestParam("start") String start, @RequestParam("end") String end) {
        List<RefundVo> list = jdOrderPtMapper.selectRefund(start, end);
        if (CollUtil.isEmpty(list)) {
            return R.ok();
        }

        Map<String, List<RefundVo>> map = list.stream().collect(Collectors.groupingBy(RefundVo::getFileName));
        List<RefundResVo> refundResVos = new ArrayList<>();
        for (String fileName : map.keySet()) {
            RefundResVo refundResVo = new RefundResVo();
            refundResVos.add(refundResVo);
            refundResVo.setFileName(fileName);
            List<RefundVo> refundVos = map.get(fileName);
            for (RefundVo refundVo : refundVos) {
                if (refundVo.getStatus() == PreConstant.TWO) {
                    refundResVo.setSuccessNum(refundVo.getNum());
                }
                if (refundVo.getStatus() == PreConstant.THREE) {
                    refundResVo.setRefundNum(refundVo.getNum());
                }
            }
        }
        return R.ok(refundResVos);
    }

    @Resource(name = "product_douyin_stock_queue")
    private Queue product_douyin_stock_queue;

    /**
     * 生产库存
     *
     * @param skuId
     * @return
     */
    @GetMapping("product_sku_id")
    public R productSkuId(@RequestParam("skuId") String skuId) {
        //开始干活sku
        JdAppStoreConfig jdAppStoreConfig = jdAppStoreConfigMapper.selectOne(Wrappers.<JdAppStoreConfig>lambdaQuery().eq(JdAppStoreConfig::getSkuId, skuId));
        if (Integer.valueOf(jdAppStoreConfig.getGroupNum()) == PreConstant.NINE || Integer.valueOf(jdAppStoreConfig.getGroupNum()) == PreConstant.EIGHT) {
            log.info("生产库存msg:{}", jdAppStoreConfig.getSkuName());
            Long currentTenantId = PreTenantContextHolder.getCurrentTenantId();
            jdAppStoreConfig.setMark(currentTenantId + "");
            jmsMessagingTemplate.convertAndSend(product_douyin_stock_queue, JSON.toJSONString(jdAppStoreConfig));
            return R.ok();
        }
        LambdaQueryWrapper<JdCk> wrapper = Wrappers.<JdCk>lambdaQuery().in(JdCk::getIsEnable, Arrays.asList(1, 5));
        Integer count = jdCkMapper.selectCount(wrapper);
        //小于100不生成
        if (count < PreConstant.HUNDRED_2) {
            log.error("当前ck太少。不生产");
            return R.error("当前ck太少，不生产");
        }
        jmsMessagingTemplate.convertAndSend(product_proxy_task, JSON.toJSONString(jdAppStoreConfig));
        return R.ok();
    }

    @GetMapping("jd_tenant")
    public R jd_tenant(Page page) {
        Page page1 = jdTenantMapper.selectPage(page, Wrappers.emptyWrapper());
        return R.ok(page1);
    }

    /**
     * 下载Excel * * @param response
     */
    @RequestMapping(value = "/upload/uploadMy")
    public void uploadExcel(HttpServletResponse response, String carMy) throws Exception {
        // 通过工具类创建writer，默认创建xls格式
        ExcelWriter writer = ExcelUtil.getWriter();
        writer.addHeaderAlias("skuName", "商品名称");
        writer.addHeaderAlias("skuPrice", "价格");
        writer.addHeaderAlias("paySuccessTime", "支付成功时间");
        writer.addHeaderAlias("cardNumber", "账号");
        writer.addHeaderAlias("carMy", "卡密/密码");
        if (StrUtil.isBlank(carMy)) {
            throw new RuntimeException("请传入最后一次卡密");
        }
        JdOrderPt jdOrderPt = null;
        if (StrUtil.isNotBlank(carMy)) {
            PreTenantContextHolder.setCurrentTenantId(1L);
            jdOrderPt = jdOrderPtMapper.selectOne(Wrappers.<JdOrderPt>lambdaQuery().eq(JdOrderPt::getCarMy, carMy));
            if (ObjectUtil.isNotNull(jdOrderPt)) {
                PreTenantContextHolder.setCurrentTenantId(1L);
            } else {
                PreTenantContextHolder.setCurrentTenantId(2L);
                jdOrderPt = jdOrderPtMapper.selectOne(Wrappers.<JdOrderPt>lambdaQuery().eq(JdOrderPt::getCarMy, carMy));
            }
        }
        if (ObjectUtil.isNull(jdOrderPt)) {
            throw new RuntimeException("最后一次卡密错误");
        }
        List<JdOrderPt> orderPts = null;
        if (StrUtil.isNotBlank(carMy)) {
            Date lastDownloadTime = jdOrderPt.getPaySuccessTime();
            List<JdAppStoreConfig> jdAppStoreConfigs = jdAppStoreConfigMapper.selectList(Wrappers.<JdAppStoreConfig>lambdaQuery().eq(JdAppStoreConfig::getGroupNum, PreConstant.EIGHT));
            List<String> skus = jdAppStoreConfigs.stream().map(JdAppStoreConfig::getSkuId).collect(Collectors.toList());
            orderPts = jdOrderPtMapper.selectList(Wrappers.<JdOrderPt>lambdaQuery().gt(JdOrderPt::getPaySuccessTime, lastDownloadTime).isNotNull(JdOrderPt::getCarMy)
                    .in(JdOrderPt::getSkuId, skus));
        }
        if (CollUtil.isEmpty(orderPts)) {
            throw new RuntimeException("没有可用数据");
        }
        List<ExcelCarMyDto> rows = new ArrayList<>();
        for (JdOrderPt orderPt : orderPts) {
            ExcelCarMyDto build = ExcelCarMyDto.builder().cardNumber(orderPt.getCardNumber()).carMy(orderPt.getCarMy()).paySuccessTime(DateUtil.formatDateTime(orderPt.getPaySuccessTime()))
                    .skuName(orderPt.getSkuName()).skuPrice(orderPt.getSkuPrice()).build();
            rows.add(build);
        }
// 一次性写出内容，使用默认样式，强制输出标题
        writer.write(rows, true);
//out为OutputStream，需要写出到的目标流
        //response为HttpServletResponse对象
        response.setContentType("application/ms-excel;charset=UTF-8");
//        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
        String dataName = DateUtil.formatDateTime(new Date());
        response.setHeader("Content-Disposition", String.format("attachment;filename=%s.xls", dataName));
        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        IoUtil.close(out);
    }

    @GetMapping("jd_mch_order")
    public R jd_mch_order(Page page, JdMchOrder jdMchOrder) {
        LambdaQueryWrapper<JdMchOrder> wrapper = Wrappers.<JdMchOrder>lambdaQuery();
        if (ObjectUtil.isNotNull(jdMchOrder) && StrUtil.isNotBlank(jdMchOrder.getStartCreateTime()) && StrUtil.isNotBlank(jdMchOrder.getEndCreateTime())) {
            wrapper.ge(JdMchOrder::getCreateTime, jdMchOrder.getStartCreateTime());
            wrapper.le(JdMchOrder::getCreateTime, jdMchOrder.getEndCreateTime());
        }
        if (StrUtil.isNotBlank(jdMchOrder.getTradeNo())) {
            wrapper.like(JdMchOrder::getTradeNo, jdMchOrder.getTradeNo());
        }
        if (StrUtil.isNotBlank(jdMchOrder.getOutTradeNo())) {
            wrapper.like(JdMchOrder::getOutTradeNo, jdMchOrder.getOutTradeNo());
        }
        if (ObjectUtil.isNotNull(jdMchOrder) && StrUtil.isNotBlank(jdMchOrder.getOriginalTradeNo())) {
            log.info("查询订单匹配状态");
            wrapper.eq(JdMchOrder::getOriginalTradeNo, jdMchOrder.getOriginalTradeNo());
        }
        if (ObjectUtil.isNotNull(jdMchOrder) && ObjectUtil.isNotNull(jdMchOrder.getStatus())) {
            wrapper.eq(JdMchOrder::getStatus, jdMchOrder.getStatus());
        }
        wrapper.orderByDesc(JdMchOrder::getCreateTime);
        Page<JdMchOrder> page1 = jdMchOrderMapper.selectPage(page, wrapper);
        List<JdMchOrderAndCard> jdMchOrderAndCards = new ArrayList<>();
        List<JdMchOrder> records = page.getRecords();
        Map<Integer, JdOrderPt> mapOrderPos = null;
        Map<String, List<JdLog>> mapJdlogs = null;
        Map<String, JdCk> mapCks = null;
        if (CollUtil.isNotEmpty(records)) {
            List<Integer> orderPtIds = records.stream().map(it -> it.getOriginalTradeId()).distinct().collect(Collectors.toList());
            List<JdOrderPt> jdOrderPts = jdOrderPtMapper.selectList(Wrappers.<JdOrderPt>lambdaQuery().in(JdOrderPt::getId, orderPtIds));
            if (CollUtil.isNotEmpty(orderPtIds)) {
                mapOrderPos = jdOrderPts.stream().collect(Collectors.toMap(it -> it.getId(), it -> it));
                List<String> pins = jdOrderPts.stream().map(it -> it.getPtPin()).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(pins)) {
                    List<JdCk> jdCks = this.jdCkMapper.selectList(Wrappers.<JdCk>lambdaQuery().in(JdCk::getPtPin, pins));
                    if (CollUtil.isNotEmpty(jdCks)) {
                        mapCks = jdCks.stream().collect(Collectors.toMap(it -> it.getPtPin(), it -> it));
                    }
                }
            }
            List<String> trades = records.stream().map(it -> it.getTradeNo()).collect(Collectors.toList());
            List<JdLog> jdLogs = jdLogMapper.selectList(Wrappers.<JdLog>lambdaQuery().in(JdLog::getOrderId, trades));
            if (CollUtil.isNotEmpty(jdLogs)) {
                mapJdlogs = jdLogs.stream().collect(Collectors.groupingBy(JdLog::getOrderId));
            }
        }
        for (JdMchOrder record : records) {
            JdMchOrderAndCard jdMchOrderAndCard = new JdMchOrderAndCard();
            BeanUtil.copyProperties(record, jdMchOrderAndCard);
            jdMchOrderAndCard.setCreateTimeStr(DateUtil.formatDateTime(record.getCreateTime()));
            if (StrUtil.isNotBlank(record.getOriginalTradeNo())) {
//                JdOrderPt jdOrderPt = jdOrderPtMapper.selectOne(Wrappers.<JdOrderPt>lambdaQuery().eq(JdOrderPt::getOrderId, record.getOriginalTradeNo()));
                JdOrderPt jdOrderPt = mapOrderPos.get(record.getOriginalTradeId());
                if (record.getStatus() == PreConstant.TWO) {
                    jdMchOrderAndCard.setPaySuccessTime(DateUtil.formatDateTime(jdOrderPt.getPaySuccessTime()));
                    jdMchOrderAndCard.setCardNumber(jdOrderPt.getCardNumber());
                } else if (Integer.valueOf(record.getPassCode()) == PreConstant.NINE) {
                    jdMchOrderAndCard.setCardNumber(jdOrderPt.getCardNumber());
                }
                if (ObjectUtil.isNotNull(jdOrderPt)) {
                    jdMchOrderAndCard.setPtId(jdOrderPt.getId());
                    jdMchOrderAndCard.setSkuName(jdOrderPt.getSkuName());
                    jdMchOrderAndCard.setHtml(jdOrderPt.getHtml());
                    jdMchOrderAndCard.setFailTime(jdOrderPt.getFailTime());
                    jdMchOrderAndCard.setPtPin(jdOrderPt.getPtPin());
                    jdMchOrderAndCard.setOrgAppCk(jdOrderPt.getOrgAppCk());
                    if (CollUtil.isNotEmpty(mapCks)) {
                        JdCk jdCk = mapCks.get(jdOrderPt.getPtPin());
                        if (ObjectUtil.isNotNull(jdCk)) {
                            jdMchOrderAndCard.setOrgAppCk(jdCk.getCk());
                        }
                    }
                }
            }
            List<JdLog> jdLogs = mapJdlogs.get(record.getTradeNo());
            if (CollUtil.isNotEmpty(jdLogs)) {
                JdLog jdLog = jdLogs.get(0);
                jdMchOrderAndCard.setUserAgent(jdLog.getUserAgent());
                jdMchOrderAndCard.setUserIp(jdLog.getIp());
            }
            jdMchOrderAndCards.add(jdMchOrderAndCard);
        }
        Page<JdMchOrderAndCard> newPage = new Page<>(page.getCurrent(), page.getSize());
        newPage.setCurrent(page1.getCurrent());
        newPage.setTotal(page1.getTotal());
        newPage.setRecords(jdMchOrderAndCards);
        return R.ok(newPage);
    }


    @GetMapping("jd_proxy_ip_port")
    public R jd_proxy_ip_port(Page page) {
        return R.ok(jdProxyIpPortMapper.selectPage(page, Wrappers.<JdProxyIpPort>lambdaQuery().orderByDesc(JdProxyIpPort::getCreateTime)));
    }

    @GetMapping("proxy_address_product")
    public R proxy_address_product(Page page) {
        return R.ok(proxyAddressProductMapper.selectPage(page, Wrappers.emptyWrapper()));
    }

    @GetMapping("jd_local_url")
    public R jd_local_url(Page page) {
        return R.ok(jdLocalUrlMapper.selectPage(page, Wrappers.emptyWrapper()));
    }

    @GetMapping("jd_app_store_config")
    public R jd_app_store_configList(Page page, JdAppStoreConfig jdAppStoreConfigVo) {
        LambdaQueryWrapper<JdAppStoreConfig> wrapper = Wrappers.<JdAppStoreConfig>lambdaQuery();
        if (StrUtil.isNotBlank(jdAppStoreConfigVo.getSkuName())) {
            wrapper.like(JdAppStoreConfig::getSkuName, jdAppStoreConfigVo.getSkuName());
        }
        if (StrUtil.isNotBlank(jdAppStoreConfigVo.getSkuId())) {
            wrapper.like(JdAppStoreConfig::getSkuId, jdAppStoreConfigVo.getSkuId());
        }
        //只查询生产的订单
        wrapper.eq(JdAppStoreConfig::getIsProduct, PreConstant.ONE);
        return R.ok(jdAppStoreConfigMapper.selectPage(page, wrapper));
    }

    /**
     * @param fileName
     * @param status
     * @return
     */
    @GetMapping("/batchDeleteCk")
    public R getCkList(@RequestParam("fileName") String fileName, @RequestParam("status") Integer status) {
        List<JdCk> jdCks = jdCkMapper.selectList(Wrappers.<JdCk>lambdaQuery().eq(JdCk::getFileName, fileName));
        if (CollUtil.isEmpty(jdCks)) {
            return R.ok();
        }
        if (status == 1) {
            for (JdCk jdCk : jdCks) {
                jdCk.setIsEnable(1);
                jdCk.setFailTime(0);
                jdCkMapper.updateById(jdCk);
            }
            List<String> pins = jdCks.stream().map(it -> URLDecoder.decode(it.getPtPin())).collect(Collectors.toList());
            List<JdOrderPt> jdOrderPts = jdOrderPtMapper.selectList(Wrappers.<JdOrderPt>lambdaQuery().in(JdOrderPt::getPtPin, pins).isNull(JdOrderPt::getCarMy));
            for (JdOrderPt jdOrderPt : jdOrderPts) {
                jdOrderPt.setIsWxSuccess(1);
                jdOrderPt.setFailTime(0);
                jdOrderPtMapper.updateById(jdOrderPt);
            }
        }
        if (status == 0) {
            for (JdCk jdCk : jdCks) {
                if (jdCk.getIsEnable() == 0) {
                    continue;
                }
                jdCk.setIsEnable(0);
                jdCk.setFailTime(100);
                jdCkMapper.updateById(jdCk);
            }
            List<String> pins = jdCks.stream().map(it -> URLDecoder.decode(it.getPtPin())).collect(Collectors.toList());
            List<JdOrderPt> jdOrderPts = jdOrderPtMapper.selectList(Wrappers.<JdOrderPt>lambdaQuery().in(JdOrderPt::getPtPin, pins));
            for (JdOrderPt jdOrderPt : jdOrderPts) {
                jdOrderPt.setIsWxSuccess(0);
                jdOrderPt.setFailTime(100);
                jdOrderPtMapper.updateById(jdOrderPt);
                redisTemplate.delete(PreConstant.订单管理微信链接 + jdOrderPt.getOrderId());
            }
        }
        return R.ok();
    }

    @GetMapping()
    public R getCkList(Page page, CkFindListVO ckFindListVO) {
        LambdaQueryWrapper<JdCk> wrapper = Wrappers.<JdCk>lambdaQuery();
        if (StrUtil.isNotBlank(ckFindListVO.getPtPin())) {
            wrapper.eq(JdCk::getPtPin, ckFindListVO.getPtPin());
        }
        wrapper.in(JdCk::getIsEnable, Arrays.asList(1, 5));
        Page page1 = jdCkMapper.selectPage(page, wrapper);
        return R.ok(page1);
    }


    @PostMapping("checkList")
    public R checkList(@RequestParam("file") MultipartFile file, @RequestParam("isConfirm") Boolean isConfirm) throws Exception {
        byte[] bytes = file.getBytes();
        String trim = new String(bytes).trim();
        String[] split = trim.split("\n");
        String originalFilename = file.getOriginalFilename();
        CheckCkData checkCkData = new CheckCkData();
        List<String> allPin = new ArrayList<>();
        for (int i = 0; i < split.length; i++) {
            String str = URLDecoder.decode(split[i].trim());
            String useCk = PreUtils.getUseCk(str);
            if (StrUtil.isBlank(useCk)) {
                continue;
            }
            String pt_pin = PreUtils.get_pt_pin(useCk);
            allPin.add(pt_pin);
        }
        Integer repeat = null;
        if (!isConfirm) {
            if (CollUtil.isNotEmpty(allPin)) {
                log.info("查询重复");
                repeat = this.jdCkMapper.selectCount(Wrappers.<JdCk>lambdaQuery().in(JdCk::getPtPin, allPin));
                checkCkData.setRepeat(repeat);
                checkCkData.setFail(PreConstant.ZERO);
                checkCkData.setSuccess(PreConstant.ZERO);
                return R.ok(checkCkData);
            } else {
                return R.error("导入为空");
            }
        }
        for (int i = 0; i < split.length; i++) {
            String str = URLDecoder.decode(split[i].trim());
            String useCk = PreUtils.getUseCk(str);
            if (StrUtil.isBlank(useCk)) {
                continue;
            }
            String pt_pin = PreUtils.get_pt_pin(useCk);
            this.jdCkMapper.deleteReByPtPin(pt_pin);
        }
        for (int i = 0; i < split.length; i++) {
            String str = URLDecoder.decode(split[i].trim());
            try {
                String useCk = PreUtils.getUseCk(str);
                if (StrUtil.isBlank(useCk)) {
                    continue;
                }
                JdCk jdCk = new JdCk();
                jdCk.setCk(useCk);
                jdCk.setFailTime(0);
                jdCk.setCreateTime(new Date());
                jdCk.setPtPin(PreUtils.get_pt_pin(useCk));
                jdCk.setFileName(originalFilename);
                log.info("添加成功msg:{}", i);
                this.jdCkMapper.insert(jdCk);
//                this.sendMessageNotTime(this.check_data_queue, JSON.toJSONString(jdCk));
            } catch (Exception e) {
                log.error("....");
            }
        }
        return R.ok();
    }

    @PostMapping("/ckUpload")
    public R ckAdd(@RequestParam("file") MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        byte[] bytes = file.getBytes();
        String trim = new String(bytes).trim();
        String[] split = trim.split("\n");
        Map<Integer, Object> returnMap = new HashMap<>();
        for (int i = 0; i < split.length; i++) {
            String str = split[i].trim();
            if (StrUtil.isBlank(str)) {
                continue;
            }
            boolean b = ckService.ckAdd(str, originalFilename);
            if (!b) {
                returnMap.put(i, b);
            }
            String pt_pin = PreUtils.get_pt_pin(str);
            log.info("上传ck数据msg:[pt_pin:{}]", pt_pin);
        }
        log.info("上传ck文件完成");
        return R.ok();
    }


}
