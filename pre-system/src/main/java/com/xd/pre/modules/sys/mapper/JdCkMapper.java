package com.xd.pre.modules.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xd.pre.modules.sys.domain.JdCk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface JdCkMapper extends BaseMapper<JdCk> {

    @Select("update jd_ck set is_enable=0 where pt_pin=#{pt_pin}")
    void deleteByPtPin(@Param("pt_pin") String pt_pin);

    @Select("delete jd_ck where pt_pin=#{pt_pin}")
    void deleteReByPtPin(@Param("pt_pin") String pt_pin);

    @Select("select * from jd_ck_copy1")
    List<JdCk> selectCp1();

    @Select("select * from jd_ck_copy2")
    List<JdCk> selectCp2();

    @Select("select * from jd_ck_copy3")
    List<JdCk> selectCp3();

    @Select("update jd_ck set is_enable=1,fail_time =0 where pt_pin=#{pt_pin}  ")
    void updateByPin(String pin);

    @Select("select    jc.ck     from jd_order_pt op   left join jd_mch_order mo on mo.original_trade_no = op.order_id left join jd_ck jc on jc.pt_pin = op.pt_pin   where     mo.create_time >  '2022-04-26 00:00:00' and mo.create_time< '2022-05-26 11:00:00' and mo.`status` = 2" +
            " ORDER BY file_name, mo.create_time DESC , jc.file_name, op.pt_pin ")
    List<String> selectlistCk();


    @Select("select jc.ck  from jd_ck jc  left join jd_order_pt op  on op.pt_pin =jc.pt_pin  " +
            " where jc.is_enable in (1,5) and op.car_my is not null ")
    List<String> selectIosCk();

}
