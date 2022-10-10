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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("douyin_device_iid")
public class DouyinDeviceIid {

    /**
     * CREATE TABLE `douyin_device_iid` (
     * `id` int(11) NOT NULL AUTO_INCREMENT,
     * `device_id` varchar(100) COLLATE utf8_bin DEFAULT NULL,
     * `iid` varchar(100) COLLATE utf8_bin DEFAULT NULL,
     * `fail_reason` longtext COLLATE utf8_bin,
     * `is_enable` int(1) DEFAULT '1',
     * PRIMARY KEY (`id`)
     * ) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
     */


    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String deviceId;
    private String iid;
    private String failReason;
    private Integer isEnable;
    private Integer success;
    private Integer fail;
    private Date lastSuccessTime;
    private Date lastFailTime;

    public DouyinDeviceIid(String deviceId, String iid) {
        this.deviceId = deviceId;
        this.iid = iid;
    }
}
