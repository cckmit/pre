package com.xd.pre.modules.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xd.pre.modules.sys.domain.JdLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface JdLogMapper extends BaseMapper<JdLog> {

    @Select("SELECT " +
            "count(1) as num ,mo.`status` as orderStatus " +
            "FROM " +
            "jd_mch_order mo " +
            " LEFT JOIN jd_log jl ON mo.trade_no = jl.order_id  " +
            "WHERE " +
            "jl.type = 15 " +
            "AND mo.match_time <=60 " +
            "AND mo.create_time >= #{start} and mo.create_time <= #{end}" +
            "AND jl.id IS NOT NULL " +
            "GROUP BY  mo.`status` ")
    List<Map<String, Object>> selectStatistics(@Param("start") String start, @Param("end") String end);
}
