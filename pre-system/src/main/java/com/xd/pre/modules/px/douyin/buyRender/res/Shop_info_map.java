/**
  * Copyright 2022 bejson.com 
  */
package com.xd.pre.modules.px.douyin.buyRender.res;

import lombok.Data;

@Data
public class Shop_info_map {

    private GceCTPIk GceCTPIk;
    public void setGceCTPIk(GceCTPIk GceCTPIk) {
         this.GceCTPIk = GceCTPIk;
     }
     public GceCTPIk getGceCTPIk() {
         return GceCTPIk;
     }

}