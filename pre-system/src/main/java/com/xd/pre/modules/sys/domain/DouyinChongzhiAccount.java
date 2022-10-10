package com.xd.pre.modules.sys.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("douyin_chongzhi_account")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DouyinChongzhiAccount {


    /**
     * CREATE TABLE `douyin_chongzhi_account` (
     *   `id` int(11) NOT NULL AUTO_INCREMENT,
     *   `account` varchar(255) COLLATE utf8_bin NOT NULL COMMENT '充值号',
     *   `money` decimal(10,2) NOT NULL COMMENT '充值金额',
     *   `is_enable` int(2) NOT NULL DEFAULT '1' COMMENT '0不充值,1充值,2下架',
     *   `is_success` int(2) NOT NULL COMMENT '0充值失败,1充值成功',
     *   `fail_time` int(11) NOT NULL DEFAULT '0' COMMENT '失败次数,默认0次',
     *     `create_time` datetime DEFAULT NULL COMMENT '创建时间',
     *   PRIMARY KEY (`id`)
     * ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
     */
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String account;

    private BigDecimal money;

    private Integer isEnable;

    private Integer isSuccess;

    private Integer failTime;

    private Date createTime;


}
