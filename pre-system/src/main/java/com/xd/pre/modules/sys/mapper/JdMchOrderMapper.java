package com.xd.pre.modules.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xd.pre.modules.sys.domain.JdMchOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

public interface JdMchOrderMapper extends BaseMapper<JdMchOrder> {

    @Select("select ifnull(sum(money),0) from  jd_mch_order  " +
            "where  create_time BETWEEN  #{startTime} and #{endTime}  ")
    BigDecimal selectTotalFlowingWater(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("select ifnull(sum(money),0) from  jd_mch_order  " +
            "where  create_time BETWEEN  #{startTime} and #{endTime}  " +
            "and status=2")
    BigDecimal selectSuccessFlowingWater(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("select ifnull(sum(money),0) from  jd_mch_order  " +
            "where  create_time BETWEEN  #{startTime} and #{endTime}  " +
            "and (status=1  or status=0 ) and (original_trade_no is  null or original_trade_no ='')")
    BigDecimal selectFailFlowingWater(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("select ifnull(sum(money),0) from  jd_mch_order  " +
            " where  create_time BETWEEN  #{startTime} and #{endTime}  " +
            "  and (original_trade_no is  null  or original_trade_no ='')")
    BigDecimal selectNoMatchFlowingWater(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("update jd_mch_order set `original_trade_no` = null , `match_time`= null  ,   `status`  = 0  where id =#{xxx} ")
    void updateTradeNoById(@Param("xxx") Integer id);

    @Select("update  jd_mch_order  set notify_succ =#{one}    where id  =#{id}")
    void updateByIdNotSuccess(  @Param("id") Integer id,  @Param("one")Integer one);
}
