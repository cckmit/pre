package com.xd.pre.modules.sys.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("jd_ex_kami")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JdExKami {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String exKami;

    public JdExKami(String exKami) {
        this.exKami = exKami;
    }
}
