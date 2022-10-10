package com.xd.pre.modules.sys.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProxyAddressProduct {
    /**
     * CREATE TABLE `proxy_address_product` (
     * `id` int(11) NOT NULL,
     * `agent_address` varchar(255) DEFAULT NULL COMMENT '代理地址',
     * `num` varchar(20) DEFAULT NULL COMMENT '生产数量',
     * `expiration_time` int(11) DEFAULT NULL COMMENT '过期时间分钟数',
     * PRIMARY KEY (`id`)
     * ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String agentAddress;
    private String num;
    private Integer expirationTime;
    private Integer isProduct;
    private Integer type;

}
