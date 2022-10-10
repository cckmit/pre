package com.xd.pre.modules.sys.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DouyinHexiaoPhone {
    /**
     * CREATE TABLE `douyin_hexiao_phone` (
     * `id` int(11) NOT NULL AUTO_INCREMENT,
     * `hexiao_phone` varchar(11) COLLATE utf8_bin DEFAULT NULL,
     * `is_enable` int(2) DEFAULT '1' COMMENT '1有效，2无效',
     * `hexiao_phone_start` datetime DEFAULT NULL COMMENT '核销有效时间',
     * `hexiao_phone_end` datetime DEFAULT NULL,
     * `batch_num` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '批号',
     * `create_time` datetime DEFAULT NULL,
     * `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
     * `file_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
     * `tenant_id` int(11) DEFAULT NULL,
     * PRIMARY KEY (`id`)
     * ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer isEnable;
    private String batchNum;
    private Integer tenantId;
    private String fileName;
    private Date createTime;
    private Date updateTime;

    private String hexiaoPhoneStart;
    private String hexiaoPhoneEnd;
    private String hexiaoPhone;
    private String hexiaoName;


}
