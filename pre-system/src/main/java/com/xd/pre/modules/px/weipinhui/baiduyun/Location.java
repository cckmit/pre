package com.xd.pre.modules.px.weipinhui.baiduyun;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Location {
    private int top;
    private int left;
    private int width;
    private int height;
}
