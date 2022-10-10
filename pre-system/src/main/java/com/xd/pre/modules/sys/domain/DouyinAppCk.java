package com.xd.pre.modules.sys.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("douyin_app_ck")
public class DouyinAppCk {
    /**
     * id	int	11	0	0	0	-1	0									0	0	0	0	-1	0	0
     * uid	varchar	100	0	-1	0	0	0							utf8	utf8_bin	0	0	0	0	0	0	0
     * ck	text	0	0	-1	0	0	0							utf8	utf8_bin	0	0	0	0	0	0	0
     * is_enable	int	1	0	-1	0	0	0				1.正常，0不正常，2跑满					0	0	0	0	0	0	0
     * file_name	varchar	255	0	-1	0	0	0							utf8	utf8_bin	0	0	0	0	0	0	0
     * create_time	datetime	0	0	-1	0	0	0									0	0	0	0	0	0	0
     * update_time	datetime	0	0	-1	0	0	0									0	0	0	0	0	0	0
     * fail_reason	longtext	0	0	-1	0	0	0							utf8	utf8_bin	0	0	0	0	0	0	0
     * `max_amount` int(11) NOT NULL DEFAULT '2000' COMMENT '最大金额',
     */

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String uid;
    private String ck;
    private Integer isEnable;
    private String fileName;
    private Date createTime;
    private Date updateTime;
    private String failReason;
    private Integer maxAmount;
    private Integer tenantId;
}
