package com.xd.pre.modules.sys.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("jd_log")
public class JdLog {
    /**
     * CREATE TABLE `jd_log` (
     *   `id` int(11) NOT NULL,
     *   `user_agent` varchar(255) COLLATE utf8_bin DEFAULT NULL,
     *   `ip` varchar(255) COLLATE utf8_bin DEFAULT NULL,
     *   `type` int(2) DEFAULT NULL,
     *   PRIMARY KEY (`id`)
     * ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
     */
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String userAgent;
    private String ip;
    private Integer type;
    private String orderId;
}
