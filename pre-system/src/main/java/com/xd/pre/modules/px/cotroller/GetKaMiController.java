package com.xd.pre.modules.px.cotroller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xd.pre.common.utils.R;
import com.xd.pre.modules.px.service.GetKaMiService;
import com.xd.pre.modules.px.vo.reqvo.KaMiVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/jd")
@RestController
@Slf4j
public class GetKaMiController {
    @Autowired
    private GetKaMiService getKaMiService;

    @PostMapping("/kami")
    public R getKaMi(@RequestBody KaMiVo kaMiVo) {
        Assert.isTrue(ObjectUtil.isNotNull(kaMiVo.getOrderId()), "订单编号不能为空");
        Assert.isTrue(ObjectUtil.isNotNull(kaMiVo.getPtPin()), "ptPin不能为空");
        Assert.isTrue(ObjectUtil.isNotNull(kaMiVo.getGroupNum()), "groupNum不能为空");
        getKaMiService.getKaMiByGoogle(kaMiVo);
        return R.ok();
    }

    public static void main(String[] args) {
      for(int i =0;i<100;i++){
          try {
              cn.hutool.json.JSON jsonObject1 = JSONUtil.parse("{\n" +
                      "\t\n" +
                      "\t\"ptPin\":\"pt_pin=jd_542a0da49a690\",\n" +
                      "\t\"orderId\":\"240807839310\",\n" +
                      "\t\"groupNum\":1\n" +
                      "}");
              HttpRequest.post("192.168.2.149:8081/jd/kami").body(jsonObject1).timeout(100).execute().body();
          }catch (Exception e ){
            log.error("++++++++++++++++报错");

          }
      }

    }

}
