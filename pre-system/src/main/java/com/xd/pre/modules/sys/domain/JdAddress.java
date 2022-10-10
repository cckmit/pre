package com.xd.pre.modules.sys.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("jd_address")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JdAddress {
    /**
     * CREATE TABLE `jd_address` (
     * `parent_id` int(11) DEFAULT NULL COMMENT '0是顶点',
     * `address_level` int(11) DEFAULT NULL COMMENT '父节点.2是顶点,最低点等级5',
     * `address_id` int(11) DEFAULT NULL,
     * `address_name` varchar(255) COLLATE utf8_bin DEFAULT NULL
     * ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
     */
    private Integer parentId;
    private Integer addressLevel;
    private Integer addressId;
    private String addressName;
}
