package com.xd.pre.modules.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xd.pre.modules.sys.domain.JdDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface JdDocumentMapper extends BaseMapper<JdDocument> {

    @Select("select * from jd_document where order_id = #{orderId}")
    JdDocument selectByOrderId(@Param("orderId") String orderId);

}
