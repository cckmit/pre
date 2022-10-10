package com.xd.pre.modules.sys.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("jd_tenant")
public class JdTenant {
    /**
     * CREATE TABLE `jd_tenant` (
     *   `id` int(11) NOT NULL AUTO_INCREMENT,
     *   `username` varchar(50) NOT NULL COMMENT '账号密码',
     *   `password` varchar(255) NOT NULL,
     *   `expiration_time` datetime DEFAULT NULL COMMENT '到期时间',
     *   `is_enable` int(1) DEFAULT NULL COMMENT '1，正常使用，0不能使用',
     *   PRIMARY KEY (`id`)
     * ) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
     */
    /**
     * 部门主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String username;
    private String password;
    private String expirationTime;
    private Integer isEnable;
    private String tenantName;
    private String urlPre;
    private String callBackUrl;
    private String webProxy;
}
