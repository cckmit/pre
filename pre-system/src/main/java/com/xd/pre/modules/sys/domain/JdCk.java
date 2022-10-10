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

@Data
@TableName("jd_ck")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JdCk {
    /**
     * CREATE TABLE `jd_ck` (
     * `id` int(11) NOT NULL AUTO_INCREMENT,
     * `ck` text COMMENT '内容',
     * `create_time` datetime DEFAULT NULL,
     * `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
     * `is_enable` int(1) DEFAULT '1' COMMENT '是否白号',
     * `use_times` int(11) DEFAULT NULL COMMENT '使用次数',
     * PRIMARY KEY (`id`)
     * ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String ck;
    private Date createTime;
    private Date updateTime;
    private Integer isEnable;
    private Integer useTimes;
    private String ptPin;
    private String fileName;
    private Integer failTime;
    private String mck;
    private Date mckCreateTime;
    private String failReason;
    private String thor;
    private Integer isPc;
    private Integer isAppstoreOrder;
    private Date pcExpireDate;
    private Integer pcRisk;

//    private Integer isZhideng;
//    private String account;
//    private String password;
//    private String jddj_ck_json;
//    private Date jddj_ck_time;
//    private String mck;


    @TableField(exist = false)
    private String skuId;


}
