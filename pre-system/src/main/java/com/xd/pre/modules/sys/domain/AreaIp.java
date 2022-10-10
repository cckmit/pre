package com.xd.pre.modules.sys.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AreaIp {
    /**
     * CREATE TABLE `area_ip` (
     *   `id` int(11) NOT NULL AUTO_INCREMENT,
     *   `province_id` int(11) DEFAULT NULL,
     *   `province_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
     *   `city_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
     *   `city_id` int(11) DEFAULT NULL,
     *   PRIMARY KEY (`id`)
     * ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
     */
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String provinceId;
    private String provinceName;
    private String cityId;
    private String cityName;
}
