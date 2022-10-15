package com.xd.pre.register;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.nosql.redis.RedisDS;
import com.alibaba.fastjson.JSON;
import com.xd.pre.modules.px.douyin.pay.PayDto;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SetRelaship {
    public static Jedis jedis = RedisDS.create().getJedis();

    public static void main(String[] args) throws Exception {
        //1736502463777799
        //1739136614382624
        //1739136822194211
        //1745277214000191
        List<Entity> result = Db.use().query("select pt_pin , mark from jd_order_pt where create_time> '2022-10-10 00:00:00' and mark is not null order by create_time ");
        List<Entity> querys = Db.use().query("select id,device_id as deviceId ,iid,fail_reason as failReason  from douyin_device_iid");
        Map<String, Entity> mapto = querys.stream().collect(Collectors.toMap(it -> it.getStr("deviceId") + it.getStr("iid"), it -> it));
        for (Entity entity : result) {
            System.out.println(result.indexOf(entity));
            String pt_pin = entity.getStr("pt_pin");
            String mark = entity.getStr("mark");
            PayDto payDto = JSON.parseObject(mark, PayDto.class);
                      Entity query = mapto.get(payDto.getDevice_id() + payDto.getIid());
            if (ObjectUtil.isNull(query)) {
                continue;
            }
            Integer id = query.getInt("id");
            jedis.set("抖音锁定设备:" + id, JSON.toJSONString(query));
            jedis.expire("抖音锁定设备:" + id, 720000);
            jedis.set("抖音和设备号关联:" + pt_pin, JSON.toJSONString(query));
            jedis.expire("抖音和设备号关联:" + pt_pin, 720000);
/*            redisTemplate.opsForValue().set("抖音锁定设备:" + douyinDeviceIid.getId(), JSON.toJSONString(douyinDeviceIid), 2000, TimeUnit.HOURS);
            redisTemplate.opsForValue().set("抖音和设备号关联:" + douyinAppCk.getUid(), JSON.toJSONString(douyinDeviceIid), 2000, TimeUnit.HOURS);*/
        }
        System.out.println(result);

    }
}
