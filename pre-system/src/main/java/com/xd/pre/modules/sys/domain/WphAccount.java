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
@TableName("wph_account")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WphAccount {
    /**
     * CREATE TABLE `wph_account` (
     * `id` int(11) NOT NULL AUTO_INCREMENT,
     * `phone` varchar(20) COLLATE utf8_bin NOT NULL COMMENT '手机号',
     * `login_info` text COLLATE utf8_bin COMMENT '登录信息',
     * `create_time` datetime DEFAULT NULL COMMENT '创建时间',
     * `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
     * `mark` longtext COLLATE utf8_bin COMMENT '备注信息',
     * PRIMARY KEY (`id`)
     * ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String phone;
    private String loginInfo;
    private Date createTime;
    private Date expireTime;
    private String mark;
    private Integer isEnable;

}
