package com.xd.pre.modules.sys.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("jd_pay_order_post_address")
@Builder
public class JdPayOrderPostAddress {
    /**
     * CREATE TABLE `jd_pay_order_post_address` (
     * `id` int(11) NOT NULL AUTO_INCREMENT,
     * `url` varchar(255) DEFAULT NULL,
     * `step` int(1) DEFAULT NULL COMMENT '步骤',
     * `param` varchar(255) DEFAULT NULL,
     * `referer` varchar(255) DEFAULT NULL,
     * `origin` varchar(255) DEFAULT NULL,
     * `user_agent` varchar(255) DEFAULT NULL,
     * `app_id` varchar(11) DEFAULT NULL,
     * PRIMARY KEY (`id`)
     * ) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String url;
    private Integer step;
    private String param;
    private String referer;
    private String origin;
    private String userAgent;
    private String appId;
}
