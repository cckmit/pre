package com.xd.pre.douyinnew;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.nosql.redis.RedisDS;
import redis.clients.jedis.Jedis;

import java.util.List;

public class SynData {
    public static Db db = Db.use();
    public static Jedis jedis = RedisDS.create().getJedis();

    public static void main(String[] args) throws Exception {
        List<Entity> appCks = db.use().query("select * from douyin_app_ck");
        for (Entity appCk : appCks) {
            String uid = jedis.get("抖音和设备号关联:" + appCk.getStr("uid"));
            if (StrUtil.isNotBlank(uid)) {
                continue;
            }
            Db.use().execute("update douyin_app_ck set is_enable = 0 where id = ?", appCk.getInt("id"));
        }
    }
}
