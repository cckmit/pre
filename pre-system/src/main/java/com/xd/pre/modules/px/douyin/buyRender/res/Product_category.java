/**
 * Copyright 2022 bejson.com
 */
package com.xd.pre.modules.px.douyin.buyRender.res;

import lombok.Data;

@Data
public class Product_category {

    private int fourth_cid;
    private int third_cid;
    private int second_cid;
    private int first_cid;

    public void setFourth_cid(int fourth_cid) {
        this.fourth_cid = fourth_cid;
    }

    public int getFourth_cid() {
        return fourth_cid;
    }

    public void setThird_cid(int third_cid) {
        this.third_cid = third_cid;
    }

    public int getThird_cid() {
        return third_cid;
    }

    public void setSecond_cid(int second_cid) {
        this.second_cid = second_cid;
    }

    public int getSecond_cid() {
        return second_cid;
    }

    public void setFirst_cid(int first_cid) {
        this.first_cid = first_cid;
    }

    public int getFirst_cid() {
        return first_cid;
    }

}