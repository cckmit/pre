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
@TableName("douyin_ck")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DouyinCk {
    /**
     * CREATE TABLE `douyin_ck` (
     *   `id` int(11) NOT NULL AUTO_INCREMENT,
     *   `ck` varchar(255) COLLATE utf8_bin DEFAULT NULL,
     *   `create_time` datetime DEFAULT NULL,
     *   PRIMARY KEY (`id`)
     * ) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
     */
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String ck;
    private Date createTime;
    private String failReason;

}
