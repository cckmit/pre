package com.xd.pre.modules.sys.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("jd_order_pt")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JdOrderPt {

    /**
     * id	int	11	0	0	0	-1	0									0	0	0	0	-1	0	0
     * order_id	int	11	0	0	0	0	0									0	0	0	0	0	0	0
     * pt_pin	varchar	255	0	0	0	0	0							utf8	utf8_general_ci	0	0	0	0	0	0	0
     * success	int	1	0	-1	0	0	0			0	1,成功，0未成功					0	0	0	0	0	0	0
     * expire_time	datetime	0	0	-1	0	0	0				过期时间					0	0	0	0	0	0	0
     * create_time	datetime	0	0	-1	0	0	0				创建时间					0	0	0	0	0	0	0
     * sku_price	decimal	10	2	-1	0	0	0				价格			utf8	utf8_general_ci	0	0	0	0	0	0	0
     * sku_name	varchar	255	0	-1	0	0	0							utf8	utf8_general_ci	0	0	0	0	0	0	0
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String orderId;
    private String ptPin;
    private Integer success;
    private Date expireTime;
    private Date createTime;
    private BigDecimal skuPrice;
    private String skuName;
    private String skuId;
    private String wxPayUrl;
    private Date wxPayExpireTime;
    private String cardNumber;
    private String carMy;
    private Date paySuccessTime;
    private String prerId;
    private Integer isWxSuccess;
    private Integer isMatch;
    private String currentCk;
    private String hrefUrl;
    private String weixinUrl;
    private String ip;
    private String port;
    private String html;
    private Integer failTime;
    private Integer retryTime;
    private String orgAppCk;

    private String wphCardPhone;
    private String mark;
    private Integer tenantId;

    @TableField(exist = false)
    private Integer isEnable;
    @TableField(exist = false)
    private String temp;
    @TableField(exist = false)
    private String payData;

}
