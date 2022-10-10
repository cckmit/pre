package com.xd.pre.modules.sys.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@TableName("jd_ck_zhideng")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JdCkZhideng {

    /**
     * CREATE TABLE `jd_ck_zhideng` (
     * `id` int(11) NOT NULL AUTO_INCREMENT,
     * `create_time` datetime DEFAULT NULL,
     * `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
     * `is_enable` int(1) NOT NULL DEFAULT '1' COMMENT '-1不可以用，0-可用，但是不可下单，1可用',
     * `pt_pin` varchar(255) NOT NULL COMMENT 'pt_pin_ck的唯一标识符',
     * `file_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
     * `account` varchar(255) DEFAULT NULL COMMENT '账号密码',
     * `password` varchar(255) DEFAULT NULL COMMENT '密码',
     * `jddj_ck_json` varchar(2000) DEFAULT NULL COMMENT 'json',
     * `jddj_ck_time` datetime DEFAULT NULL COMMENT '生产ck时间',
     * `mck` varchar(255) DEFAULT NULL COMMENT 'mck',
     * PRIMARY KEY (`id`) USING BTREE,
     * UNIQUE KEY `pt_pin` (`pt_pin`) USING BTREE,
     * KEY `update_time` (`update_time`,`is_enable`) USING BTREE
     * ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Date createTime;
    private Date updateTime;
    private Integer isEnable;
    private String ptPin;
    private String fileName;
    private String appck;
    /**
     * 账号名称
     */
    private String accountName;
    private String account;
    private String password;
    private String jddjCkJson;
    private Date jddjCkTime;
    private String mck;
    /**
     * mck时间
     */
    private Date mckTime;
    private String failReason;
    private Integer writeOffCode;
    private Integer isWriteOffCode;
    private Date writeOffCodeTime;

    /**
     * 数据库不存在的账号
     */
    @TableField(exist = false)
    private List<JdOrderPt> jdOrderPtList;
    @TableField(exist = false)
    private Integer failPay = 0;
    @TableField(exist = false)
    private Integer toBePay = 0;
    @TableField(exist = false)
    private Integer successPay = 0;
    @TableField(exist = false)
    private Integer successMoney;


}
