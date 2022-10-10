package com.xd.pre.modules.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xd.pre.modules.sys.domain.JdProxyIpPort;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface JdProxyIpPortMapper extends BaseMapper<JdProxyIpPort> {

    @Select("select * from jd_proxy_ip_port where is_use=0 and expiration_time>NOW() LIMIT 1 ")
    JdProxyIpPort selectOneAndNotUse();

}
