package com.xd.pre.register;

import cn.hutool.db.nosql.redis.RedisDS;
import redis.clients.jedis.Jedis;

public class Test {
    public static void main(String[] args) {
        Jedis jedis = RedisDS.create().getJedis();
        Integer a = 223232;
        Integer b = 223232;
        System.out.println(a.equals(b));
    }
}
