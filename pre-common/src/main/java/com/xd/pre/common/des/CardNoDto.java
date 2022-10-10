package com.xd.pre.common.des;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardNoDto {
    private String cardNo;
    private String cardPass;
}
