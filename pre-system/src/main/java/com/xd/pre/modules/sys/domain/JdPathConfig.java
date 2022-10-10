package com.xd.pre.modules.sys.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class JdPathConfig {

    /**
     * CREATE TABLE `jd_path_config` (
     * `id` int(11) NOT NULL AUTO_INCREMENT,
     * `ip` varchar(255) DEFAULT NULL,
     * `jd_apk` varchar(255) DEFAULT NULL,
     * `chrome_driver` varchar(255) DEFAULT NULL,
     * PRIMARY KEY (`id`)
     * ) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String ip;
    private String jdApk;
    private String chromeDriver;
    private Integer headless;

}
