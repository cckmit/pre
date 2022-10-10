package com.xd.pre.modules.sys.mapper;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xd.pre.modules.px.vo.resvo.RefundVo;
import com.xd.pre.modules.sys.domain.JdOrderPt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface JdOrderPtMapper extends BaseMapper<JdOrderPt> {


    @Select("SELECT count(1) as stockNum " +
            "FROM jd_order_pt op " +
            "WHERE op.create_time>#{createTime} and  op.sku_id=#{skuId} and is_wx_success =1 and car_my is null ")
    Integer selectStockDb(@Param("createTime") Date createTime, @Param("skuId") String skuId);

    @Update("update jd_order_pt set is_match =0 where order_id =#{originalTradeNo} ")
    void updateByTradeNo(@Param("originalTradeNo") String originalTradeNo);

    @Select("select  pt_pin from jd_order_pt where expire_time >NOW()  GROUP BY pt_pin ")
    List<String> selectMax2Data(@Param("productNum") Integer productNum);

    @Select("select  pt_pin from jd_order_pt where expire_time >NOW() and car_my is not null  GROUP BY pt_pin")
    List<String> selectMax2DataPay(@Param("productNum") Integer productNum);

    @Select("select    jc.file_name as fileName ,mo.status,count(1) as num  from jd_order_pt op   left join jd_mch_order mo on mo.original_trade_no = op.order_id left join jd_ck jc on jc.pt_pin = op.pt_pin " +
            " where  mo.create_time >  #{start}  and   mo.create_time <  #{end}  and mo.status in (2,3)" +
            "GROUP BY jc.file_name,mo.status ")
    List<RefundVo> selectRefund(@Param("start") String start, @Param("end") String end);


    @Select("select pt_pin from jd_order_pt where sku_id in ('3486426077254021120','2889136176674373632','2474805010956906496','741200629395914752','516865072957472768','1315128106909040640') GROUP BY pt_pin " +
            "HAVING count(1)>=#{max}")
    List<String> selectwphOrderMax(@Param("max") Integer max);


    @Select("SELECT pt_pin FROM jd_order_pt WHERE create_time >=#{createDate} and " +
            "sku_id IN ( '10022039398507_1','11183343342_1','11183368356_1')  " +
            "GROUP BY pt_pin HAVING sum(sku_price)> 2800")
    List<String> selectPcAppStoreOrderMax(@Param("createDate") Date createDate);

    @Select("select sum(sku_price ) as sku_price_total from jd_order_pt where sku_id in ('1736502463777799', " +
            "'1739136614382624', " +
            "'1739136822194211', " +
            "'1745277214000191') " +
            "and pt_pin = #{ptPin}' " +
            "and (pay_success_time is not null or  wx_pay_expire_time >NOW()  ) " +
            "and create_time BETWEEN #{beginOfDay} AND #{endOfDay}")
    Integer selectDouYinByStartTimeAndEndAndUid(@Param("ptPin") String ptPin, @Param("beginOfDay") DateTime beginOfDay, @Param("endOfDay") DateTime endOfDay);

    @Select("select sum(sku_price ) as sku_price_total,pt_pin from jd_order_pt where sku_id in ('1736502463777799', " +
            "'1739136614382624', " +
            "'1739136822194211', " +
            "'1745277214000191') " +
            "and (pay_success_time is not null or  wx_pay_expire_time >NOW()  ) " +
            "and create_time BETWEEN #{beginOfDay} AND #{endOfDay} " +
            "GROUP BY pt_pin ")
    List<Map<String, Object>> selectDouYinByStartTimeAndEndAndUidGroup(@Param("beginOfDay") DateTime beginOfDay, @Param("endOfDay") DateTime endOfDay);

    @Select("select pt_pin  from jd_order_pt where pay_success_time < #{beginOfDay} " +
            "and sku_id  in   ('1736502463777799', '1739136614382624', '1739136822194211','1745277214000191')" +
            "GROUP BY pt_pin ")
    List<String> selectOrderSuccessYesterday(@Param("beginOfDay") DateTime beginOfDay);
}
