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
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("jd_mch_order")
public class JdMchOrder {
    /**
     * CREATE TABLE `jd_mch_order` (
     * `mch_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '商户号',
     * `sign` varchar(255) COLLATE utf8_bin NOT NULL COMMENT '固定参数',
     * `pass_code` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '802' COMMENT '通道号，默认802',
     * `subject` varchar(255) COLLATE utf8_bin NOT NULL COMMENT '订单标题',
     * `body` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '订单描述',
     * `trade_no` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '平台订单号',
     * `original_trade_no` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '原始订单号',
     * `out_trade_no` varchar(100) COLLATE utf8_bin NOT NULL COMMENT '商户订单号',
     * `amount` varchar(255) COLLATE utf8_bin NOT NULL COMMENT '金额',
     * `client_ip` varchar(255) COLLATE utf8_bin NOT NULL COMMENT '金额',
     * `notify_url` varchar(255) COLLATE utf8_bin NOT NULL COMMENT '通知地址',
     * `return_url` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '回调地址',
     * `timestamp` varchar(100) COLLATE utf8_bin NOT NULL COMMENT '时间。格式标准',
     * `create_time` datetime NOT NULL COMMENT '创建时间',
     * `expired_time` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '标准时间,过期时间',
     * `pay_url` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '支付连接',
     * `qrcode_url` varchar(255) COLLATE utf8_bin DEFAULT NULL,
     * `money` varchar(255) COLLATE utf8_bin NOT NULL COMMENT '0,未成功，1成功',
     * `notify_succ` int(255) DEFAULT NULL,
     * `status` int(255) NOT NULL COMMENT '0，失败，1待支付，2支付成功',
     * `original_trade_id` int(255) NOT NULL COMMENT '0，失败，1待支付，2支付成功',
     * PRIMARY KEY (`mch_id`) USING BTREE
     * ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String mchId;
    private String sign;
    private String passCode;
    private String subject;
    private String body;
    private String tradeNo;
    private String originalTradeNo;
    private String outTradeNo;
    private String amount;
    private String money;
    private String clientIp;
    private String notifyUrl;
    private String returnUrl;
    private String timestamp;
    private Date createTime;
    private String expiredTime;
    private String payUrl;
    private String qrcodeUrl;
    /**
     * 0,未成功，1成功
     */
    private Integer notifySucc;
    /**
     * 0，失败，1待支付，2支付成功
     */
    private Integer status;
    /**
     * 匹配时间成功
     */
    private Long matchTime;

    /**
     * 匹配Id
     */
    private Integer originalTradeId;
    /**
     *
     */
    private String skuId;

    @TableField(exist = false)
    private String startCreateTime;

    @TableField(exist = false)
    private String endCreateTime;

    private Long tenantId;

}
