package com.xd.pre.modules.sys.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DouyinRechargePhone {

    //    CREATE TABLE `douyin_recharge_phone` (
//            `id` int(11) NOT NULL,
//  `recharge_phone` varchar(255) COLLATE utf8_bin NOT NULL COMMENT '手机号',
//            `price` int(10) DEFAULT NULL,
//  `order_status` int(10) DEFAULT NULL COMMENT '0，还未充值，1充值中，2充值完成，3充值失败',
//            `create_time` datetime DEFAULT NULL,
//            `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
//  `file_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
//    PRIMARY KEY (`id`)
//) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String rechargePhone;
    private Integer price;
    //0，还未充值，1充值中，2充值完成，3充值失败
    private Integer orderStatus;
    private String fileName;
    private Date createTime;
    private Date updateTime;
    private Integer tenantId;
    private Date successTime;
    private Integer isEnable;
    private String batchNum;
}
