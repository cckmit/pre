/**
  * Copyright 2022 bejson.com 
  */
package com.xd.pre.modules.px.douyin.buyRender.res;

import lombok.Data;


@Data
public class Limit_info {

    private int min;
    private int max;
    private String toast_up;
    private String toast_down;
    private String limit_type;


}