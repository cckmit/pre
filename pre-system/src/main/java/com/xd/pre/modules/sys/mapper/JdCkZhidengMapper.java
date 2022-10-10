package com.xd.pre.modules.sys.mapper;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xd.pre.modules.px.jddj.Max5Dto;
import com.xd.pre.modules.sys.domain.JdCkZhideng;
import com.xd.pre.modules.sys.vo.WriteOffCodeStaVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * 直登账号管理mapper
 */
public interface JdCkZhidengMapper extends BaseMapper<JdCkZhideng> {

    @Select("update jd_ck_zhideng set is_enable=-1 where pt_pin=#{pt_pin}")
    void deleteByPtPin(String pt_pin);

    @Select("SELECT " +
            "op.is_wx_success as isWxSuccess, " +
            "op.pt_pin as ptPin, " +
            "count( 1 ) as count  " +
            "FROM " +
            "jd_ck_zhideng jcz " +
            "LEFT JOIN jd_order_pt op ON op.pt_pin = jcz.pt_pin  " +
            "WHERE " +
            "jcz.is_enable = 1  " +
            "and op.create_time > #{beginOfDay}  " +
            "GROUP BY " +
            "op.is_wx_success, " +
            "op.pt_pin  " +
            "HAVING " +
            "count( 1 ) >= #{max}  " +
            "AND op.is_wx_success =1")
    List<Max5Dto> selectMax5Order(@Param("max") Integer max, @Param("beginOfDay") Date beginOfDay, @Param("endOfDay") Date endOfDay);


    @Select("select IFNULL( MAX(write_off_code),0)+1 as maxCode from jd_ck_zhideng")
    Integer selectOffCode();


    @Select("select DISTINCT jcz.pt_pin   from jd_ck_zhideng jcz left join jd_order_pt op on op.pt_pin = jcz.pt_pin " +
            "where jcz.create_time > #{createTime} and  jcz.is_enable=-1 and jcz.write_off_code is null and op.pay_success_time is not null " +
            "and jcz.fail_reason like '%前方系统拥堵%' " +
            "order by jcz.pt_pin")
    List<String> selectNotProductOffCode(@Param("createTime") DateTime createTime);


    @Select("select DISTINCT jcz.pt_pin   from jd_ck_zhideng jcz left join jd_order_pt op on op.pt_pin = jcz.pt_pin " +
            "where jcz.file_name = #{fileName} and jcz.write_off_code is null and op.pay_success_time is not null " +
//            "and jcz.fail_reason like '%前方系统拥堵%' " +
            "order by jcz.pt_pin")
    List<String> batchNotProductOffCode(@Param("fileName") String fileName);


    @Select("select  jcz.is_write_off_code as isWriteOffCode ,sum(op.sku_price) as money ,op.tenant_id  from jd_ck_zhideng jcz left join jd_order_pt op on op.pt_pin = jcz.pt_pin " +
            "where " +
            "op.pay_success_time >= #{start} " +
            "and op.pay_success_time  &lt;= #{end} " +
            "and  jcz.write_off_code is not null   and  op.pay_success_time is not null  " +
            "GROUP BY jcz.is_write_off_code  ")
    List<WriteOffCodeStaVo> selectWriteOffCodestatistics(@Param("start") String start, @Param("end") String end);
}
