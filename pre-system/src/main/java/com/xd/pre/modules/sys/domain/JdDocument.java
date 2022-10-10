package com.xd.pre.modules.sys.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("jd_document")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JdDocument {
    /**
     * CREATE TABLE `jd_document` (
     * `id` int(11) NOT NULL AUTO_INCREMENT,
     * `document_url` varchar(1000) DEFAULT NULL COMMENT '支付链接',
     * `order_id` varchar(50) DEFAULT NULL COMMENT '订单编号',
     * `is_success` int(2) DEFAULT NULL COMMENT '是否支付成功',
     * `sku_id` varchar(50) DEFAULT NULL COMMENT 'sku_id',
     * `create_time` datetime NOT NULL COMMENT '创建时间',
     * `pay_success_time` datetime DEFAULT NULL COMMENT '支付成功时间',
     * `pt_pin` varchar(255) NOT NULL COMMENT 'ck关联',
     * `card_number` varchar(100) DEFAULT NULL COMMENT '卡号',
     * `car_my` varchar(100) DEFAULT NULL COMMENT '卡密',
     * PRIMARY KEY (`id`)
     * ) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8;
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String documentUrl;
    private String orderId;
    private Integer isSuccess;
    private String skuId;
    private Date createTime;
    private Date paySuccessTime;
    private String ptPin;
    private String cardNumber;
    private String carMy;

    public JdDocument(String documentUrl, String orderId, Integer isSuccess, String skuId, Date createTime, Date paySuccessTime,
                      String ptPin, String cardNumber, String carMy) {
        this.documentUrl = documentUrl;
        this.orderId = orderId;
        this.isSuccess = isSuccess;
        this.skuId = skuId;
        this.createTime = createTime;
        this.paySuccessTime = paySuccessTime;
        this.ptPin = ptPin;
        this.cardNumber = cardNumber;
        this.carMy = carMy;
    }

    @TableField(exist = false)
    private String ck;
}
