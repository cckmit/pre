package com.xd.pre.modules.px.weipinhui.baiduyun;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WordsResult {
    private String words;
    private Location location;
//    private Integer words_result_num;

}
