package com.xd.pre.modules.sys.domain;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xd.pre.modules.px.vo.tmpvo.appstorevo.AppStoreVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@TableName("jd_app_store_config")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JdAppStoreConfig {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String url;
    private String config;
    private String mark;

    private BigDecimal skuPrice;
    private String skuName;
    private String skuId;
    private Integer groupNum;
    private Integer expireTime;
    private Integer payIdExpireTime;
    private Integer productStockNum;
    private Integer isProduct;
    private Integer productNum;
    private Integer payType;
    @TableField(exist = false)
    private Integer IOS;

    @TableField(exist = false)
    private List<AppStoreVo> appStoreVo;



}
