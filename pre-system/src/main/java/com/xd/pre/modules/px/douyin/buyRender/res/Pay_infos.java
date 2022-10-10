/**
 * Copyright 2022 bejson.com
 */
package com.xd.pre.modules.px.douyin.buyRender.res;

import lombok.Data;

import java.util.List;

@Data
public class Pay_infos {

    private String show_name;
    private int pay_type;
    private int sub_way;
    private boolean select;
    private int show_num;
    private String pay_id;
    private boolean support;
    private String tag;
    private String sub_methods;
    private List<String> pay_type_desc;
    private String icon_url;
    private String extra_info;
    private List<String> voucher_msg_list;
    private String more_voucher_msg_list;
    private String unsupported_reason;
    private Nopwd_pay_params nopwd_pay_params;
    private String home_page_banner;
    private int stable_status;
    private Pay_change_info pay_change_info;
    private int sdk_service;
    private String show_style;


}