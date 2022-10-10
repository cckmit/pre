/**
 * Copyright 2022 bejson.com
 */
package com.xd.pre.modules.px.douyin.buyRender.res;

import lombok.Data;

import java.util.List;

@Data
public class Tag_tip_content {

    private List<Tip_tags> tip_tags;
    private String content;
    private int show_type;
    private String extra_info;


}