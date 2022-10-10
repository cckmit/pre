package com.xd.pre.common.config;

public enum LoginPcEnum {
    请电脑端确认(1, "请电脑端确认"),
    过期(-1, "二维码失效"),
    未知情况(-2, "未知情况");
    private Integer key;
    private String value;

    LoginPcEnum(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    public Integer getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }


}
