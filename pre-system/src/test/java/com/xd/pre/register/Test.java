package com.xd.pre.register;

import cn.hutool.db.nosql.redis.RedisDS;
import redis.clients.jedis.Jedis;

public class Test {
    public static void main(String[] args) {
        Jedis jedis = RedisDS.create().getJedis();
        String s = jedis.get("抖音和设备号关联:1051563615585283");
        System.out.println(s);
    }
}
