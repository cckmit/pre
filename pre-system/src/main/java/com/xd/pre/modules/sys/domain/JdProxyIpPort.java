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
public class JdProxyIpPort {
    /**
     * CREATE TABLE `jd_proxy_ip_port` (
     * `id` int(11) NOT NULL AUTO_INCREMENT,
     * `agent_address` varchar(255) DEFAULT NULL COMMENT '代理生成地址',
     * `ip` varchar(255) DEFAULT NULL COMMENT 'ip',
     * `port` varchar(255) DEFAULT NULL COMMENT '端口',
     * `create_time` datetime DEFAULT NULL COMMENT '创建时间',
     * `use_time` datetime DEFAULT NULL COMMENT '使用时间',
     * `is_use` int(1) DEFAULT NULL COMMENT '1已经使用，0未使用',
     * expiration_time
     * PRIMARY KEY (`id`)
     * ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
     */

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String agentAddress;
    private String ip;
    private String port;
    private Date createTime;
    private Integer isUse;
    private Date useTime;
    private Date expirationTime;
    private String city;
    private String provinceId;
    private String cityId;
}
