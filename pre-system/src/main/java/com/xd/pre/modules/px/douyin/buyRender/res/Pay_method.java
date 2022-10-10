/**
 * Copyright 2022 bejson.com
 */
package com.xd.pre.modules.px.douyin.buyRender.res;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.util.List;


@Data
public class Pay_method {

    private int pay_amount;
    private List<Pay_infos> pay_infos;
    private int show_num;
    private String zg_ext_info;
    private String risk_info;
    private String dev_info;
    private String jh_ext_info;
    private String trace_id;
    private String toast_msg;

    public  String getDecision_id() {
        String decision_id = JSON.parseObject(zg_ext_info).getString("decision_id");
        return decision_id;
    }

    public  String getPayapi_cache_id() {
        String payapi_cache_id = JSON.parseObject(jh_ext_info).getString("payapi_cache_id");
        return payapi_cache_id;
    }


}