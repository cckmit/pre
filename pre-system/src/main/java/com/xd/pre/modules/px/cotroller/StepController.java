package com.xd.pre.modules.px.cotroller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xd.pre.common.utils.R;
import com.xd.pre.common.utils.px.PreUtils;
import com.xd.pre.modules.data.tenant.PreTenantContextHolder;
import com.xd.pre.modules.px.listener.TopicConsumerListener;
import com.xd.pre.modules.px.service.NewWeiXinPayUrl;
import com.xd.pre.modules.px.service.ProxyProductService;
import com.xd.pre.modules.px.service.TokenKeyService;
import com.xd.pre.modules.px.task.ProductProxyTask;
import com.xd.pre.modules.px.utils.LingJuanDemo;
import com.xd.pre.modules.px.vo.reqvo.StepVo;
import com.xd.pre.modules.px.vo.reqvo.TokenKeyVo;
import com.xd.pre.modules.px.vo.resvo.TokenKeyResVo;
import com.xd.pre.modules.px.weipinhui.service.WphService;
import com.xd.pre.modules.sys.domain.*;
import com.xd.pre.modules.sys.mapper.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.activemq.ScheduledMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.Queue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

@RequestMapping("/jd")
@RestController
@Slf4j
@CrossOrigin
public class StepController {
    @Autowired
    private ProxyProductService proxyProductService;

    @Autowired
    private NewWeiXinPayUrl newWeiXinPayUrl;
    @Resource
    private JdAppStoreConfigMapper jdAppStoreConfigMapper;

    @Resource(name = "product_proxy_task")
    private Queue product_proxy_task;


    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;
    @Resource
    private JdMchOrderMapper jdMchOrderMapper;
    @Autowired
    private TopicConsumerListener topicConsumerListener;

    @Resource
    private JdOrderPtMapper jdOrderPtMapper;

    @Autowired
    private TokenKeyService tokenKeyService;

    @Autowired
    private WphService wphService;


    @Resource(name = "findQueue")
    private Queue findQueue;

    @GetMapping("/dispatcher5")
    public R dispatcher5(Integer id, Long ti) throws Exception {
        PreTenantContextHolder.setCurrentTenantId(ti);
        JdMchOrder jdMchOrder = jdMchOrderMapper.selectById(id);
        this.sendMessageSenc(this.findQueue, JSON.toJSONString(jdMchOrder), 10);
        return R.ok();
    }

    // 发送消息，destination是发送到的队列，message是待发送的消息
    private void sendMessageSenc(Destination destination, final String message, Integer minit) {
        Map<String, Object> headers = new HashMap<>();
        //发送延迟队列，延迟10秒,单位毫秒
        headers.put(ScheduledMessage.AMQ_SCHEDULED_DELAY, minit * 1000);
        jmsMessagingTemplate.convertAndSend(destination, message, headers);
    }

    @GetMapping("/dispatcher")
    public R dispatcher(HttpServletRequest request, HttpServletResponse response) throws Exception {
     /*   Assert.isTrue(ObjectUtil.isNotNull(stepVo.getPrice()), "价格不能为空不能为空");
        log.info("定时任务分发接口数据msg:[data:{}]", stepVo);
        JdAppStoreConfig jdAppStoreConfig = jdAppStoreConfigMapper.selectOne(Wrappers.<JdAppStoreConfig>lambdaQuery().eq(JdAppStoreConfig::getSkuPrice, stepVo.getPrice()));
        R r = newWeiXinPayUrl.liuChengMethond(stepVo.getCk(), jdAppStoreConfig.getSkuId());
        log.info("返回结果msg:{}", r);*/
        return null;
    }

    @GetMapping("test")
    public R test(HttpServletRequest request, HttpServletResponse response) {
        Enumeration<String> headerNames = request.getHeaderNames();
        // 设置请求头
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> v = request.getHeaders(headerName);
            List<String> arr = new ArrayList<>();
            while (v.hasMoreElements()) {
                arr.add(v.nextElement());
            }
            log.info("msg:{}:{}", headerName, arr);
        }
        String ipAddress = PreUtils.getIPAddress(request);
        log.info("访问的ip为msg:{}", ipAddress);
        log.info("");
        return R.ok();
    }

    @PostMapping("/dispatcher1")
    public R dispatcher1(@RequestBody StepVo stepVo) throws Exception {
        R r = newWeiXinPayUrl.getCartNumAndMy(jdMchOrderMapper.selectById(502));
        log.info("返回结果msg:{}", r);
        return r;
    }

    @PostMapping("/dispatcher2")
    public R dispatcher2(@RequestBody StepVo stepVo) throws Exception {
        //开始干活sku
        JdAppStoreConfig jdAppStoreConfig = jdAppStoreConfigMapper.selectOne(Wrappers.<JdAppStoreConfig>lambdaQuery().eq(JdAppStoreConfig::getSkuId, stepVo.getSkuId()));
        jmsMessagingTemplate.convertAndSend(product_proxy_task, JSON.toJSONString(jdAppStoreConfig));
        return R.ok();
    }


    @Autowired
    private ProductProxyTask productProxyTask;

    @PostMapping("/dispatcher3")
    public R dispatcher3() {
        JdMchOrder jdMchOrder = new JdMchOrder();
        jdMchOrder.setId(502);
        productProxyTask.notifySuccess(jdMchOrder);
        return R.ok();
    }

    @PostMapping("/dispatcher4")
    public R dispatcher4() {
        JdMchOrder jdMchOrder = new JdMchOrder();
        jdMchOrder.setId(42);
        jdMchOrderMapper.updateTradeNoById(1);
        return R.ok();
    }

    @Resource
    private JdCkMapper jdCkMapper;

    @GetMapping("/pin")
    public R buildpin() {
        List<JdCk> jdCks = jdCkMapper.selectList(Wrappers.emptyWrapper());
        for (JdCk jdCk : jdCks) {
            try {
                String pt_pin = PreUtils.get_pt_pin(jdCk.getPtPin());
                jdCk.setPtPin(pt_pin);
                jdCkMapper.updateById(jdCk);
            } catch (Exception e) {
                jdCkMapper.deleteById(jdCk.getId());
            }
        }
        return R.ok();
    }

    @PostMapping("/pinTothis")
    public R pinTothis(@RequestParam("file") MultipartFile file) throws Exception {
        byte[] bytes = file.getBytes();
        String trim = new String(bytes).trim();
        String originalFilename = file.getOriginalFilename();
        String[] split = trim.split("\n");
        for (int i = 0; i < split.length; i++) {
            String str = split[i].trim();
            try {
                JdCk jdCk = new JdCk();
                jdCk.setCk(str.trim());
                jdCk.setCreateTime(new Date());
                jdCk.setUseTimes(0);
                jdCk.setPtPin(PreUtils.get_pt_pin(str));
                jdCk.setFileName(originalFilename);
                log.info("添加成功msg:{}", i);
                jdCkMapper.insert(jdCk);
            } catch (Exception e) {
                log.error("....重复");
            }
        }
        return R.ok();
    }

    public static void main(String[] args) throws Exception {

        FileReader fileReader1 = new FileReader("C:\\Users\\Administrator\\Desktop\\系统上线文档\\账号\\xin8\\5000.txt");
        List<String> strings1 = fileReader1.readLines();
        for (String s : strings1) {
            System.out.println(s.split(PreUtils.get_pt_pin(s)));
        }
        FileReader fileReader = new FileReader("C:\\Users\\Administrator\\Desktop\\系统上线文档\\账号\\xin8\\5000.txt");
        List<String> strings = fileReader.readLines();
        OkHttpClient client = new OkHttpClient();
        for (String string : strings) {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                Request request = new Request.Builder()
                        .url("https://api.m.jd.com/?t=1652679128286&functionId=pg_channel_page_data&appid=vip_h5&body=%7B%22paramData%22:%7B%22token%22:%2260143dce-1cde-44de-8130-a6e5579e1567%22%7D%7D")
                        .get()
                        .addHeader("Cookie", string)
                        .addHeader("cache-control", "no-cache")
                        .build();
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                List<JSONObject> jsonObjects = JSON.parseArray(JSON.parseObject(JSON.parseObject(body).get("data").toString()).get("floorInfoList").toString(), JSONObject.class);
                for (JSONObject oneJson : jsonObjects) {
                    if (!JSON.toJSONString(oneJson).contains("账户价值")) {
                        continue;
                    }
                    Object o = JSON.parseObject(JSON.parseObject(oneJson.get("floorData").toString()).get("jxScoreInfo").toString()).get("elements");
                    String creditLevel = JSON.parseObject(JSON.parseObject(oneJson.get("floorData").toString()).get("jxScoreInfo").toString()).get("creditLevel").toString();
                    stringBuilder.append("creditLevel:" + creditLevel + "=====");
                    List<JSONObject> elements = JSON.parseArray(o.toString(), JSONObject.class);
                    for (JSONObject element : elements) {
                        if (!element.containsKey("level")) {
                            continue;
                        }
                        String level = element.get("level").toString();
                        String name = element.get("name").toString();
                        stringBuilder.append(level + ":" + name + "-----");
                    }
                }
                System.out.println(PreUtils.get_pt_pin(string) + "分析数据============" + stringBuilder.toString());
                response.close();
            } catch (Exception e) {

            }
        }
    }

    @PostMapping("/lingjuan")
    public R pinTothis1() throws Exception {
        List<String> list = this.jdCkMapper.selectlistCk();
        if (CollUtil.isEmpty(list)) {
            return R.error();
        }
        List<Object> objects = new ArrayList<>();

        for (String ck : list) {
            JdProxyIpPort oneIp = proxyProductService.getOneIp(0, 0, false);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(oneIp.getIp(), Integer.valueOf(oneIp.getPort())));
            if (StrUtil.isBlank(ck)) {
                continue;
            }
            JdCk jdCk = jdCkMapper.selectOne(Wrappers.<JdCk>lambdaQuery().eq(JdCk::getPtPin, PreUtils.get_pt_pin(ck)));
            TokenKeyVo build = TokenKeyVo.builder().cookie(jdCk.getCk().trim()).build();
            TokenKeyResVo tokenKey = tokenKeyService.getTokenKey(build, null, jdCk.getFileName());
            if (ObjectUtil.isNull(tokenKey)) {
                continue;
            }
            String tokenUrl = String.format("https://un.m.jd.com/cgi-bin/app/appjmp?tokenKey=%s&to=https://gamerecg.m.jd.com?skuId=%s", tokenKey.getTokenKey(), "11183343342");
            OkHttpClient client = newWeiXinPayUrl.clientManager(null, null);
            this.newWeiXinPayUrl.init(client, tokenUrl, null);
            Boolean aBoolean = LingJuanDemo.CheckIsLingJuanMain(newWeiXinPayUrl.ckManager(client), proxy);
            System.out.println("==========================" + aBoolean + PreUtils.get_pt_pin(ck));
            objects.add(aBoolean);
        }
        System.out.println(JSON.toJSONString(objects));
        return R.ok();
    }

    @Resource
    private JdAddressMapper jdAddressMapper;

    @GetMapping("/address")
    public R address() throws Exception {
        List<JdAddress> jdAddresses = jdAddressMapper.selectList(Wrappers.<JdAddress>lambdaQuery().eq(JdAddress::getAddressLevel, 4));
        for (JdAddress jdAddress : jdAddresses) {
            String url = String.format("https://api.m.jd.com/api?appid=mdb&functionId=order_address_code&loginType=2&t=1655737574951&body={\"addressLevel\":%d,\"addressId\":%d}", 4, jdAddress.getAddressId());
            String result1 = HttpRequest.get(url)
                    .header(Header.REFERER, "https://thunder.jd.com/")//头信息，多个头信息多次调用此方法即可
                    .timeout(20000)//超时，毫秒
                    .execute().body();
            List<JSONObject> jsonObjects1 = JSON.parseArray(JSON.parseObject(result1).getString("value"), JSONObject.class);
            for (JSONObject jsonObject : jsonObjects1) {
                Integer addressLevel = 5;
                String address_name = jsonObject.getString("name");
                Integer address_id = jsonObject.getInteger("id");
                JdAddress build = JdAddress.builder().parentId(jdAddress.getAddressId()).addressLevel(addressLevel).addressId(address_id).addressName(address_name).build();
                log.info("开始入库msg:{}", build);
                jdAddressMapper.insert(build);
            }
        }
        return R.ok();
    }


}
