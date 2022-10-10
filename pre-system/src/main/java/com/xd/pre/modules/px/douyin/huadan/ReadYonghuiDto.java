package com.xd.pre.modules.px.douyin.huadan;

import lombok.Data;

@Data
public class ReadYonghuiDto {
    private String 手机号码;
    private String 批号;
    private Integer 是否有效;
    private String 有效时间开始时间;
    private String 有效结束时间;
    private String 核销人员;
}
