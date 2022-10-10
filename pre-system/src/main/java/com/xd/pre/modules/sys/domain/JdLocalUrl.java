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
@TableName("jd_local_url")
@Builder
public class JdLocalUrl {
    /**
     * CREATE TABLE `jd_local_url` (
     * `id` int(11) NOT NULL AUTO_INCREMENT,
     * `url` varchar(500) DEFAULT NULL COMMENT '签证地址',
     * `tag` varchar(255) DEFAULT NULL COMMENT '标识符,必须程序员添加的',
     * `mark` varchar(255) DEFAULT NULL COMMENT '备注',
     * `step` int(255) DEFAULT NULL COMMENT '步骤',
     * `ext` text COMMENT '额外数据',
     * `group` varchar(255) DEFAULT NULL,
     * `is_enable` int(2) DEFAULT '1' COMMENT '是否启动',
     * PRIMARY KEY (`id`)
     * ) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
     */

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String url;
    private String tag;
    private String mark;
    private Integer step;
    private String ext;
    private String groupNum;
    private Integer isEnable;

}
