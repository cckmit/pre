package com.xd.pre.modules.sys.util;

import com.alibaba.fastjson.JSON;
import com.xd.pre.modules.sys.vo.RedisVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Classname RedisUtil
 * @Description redis工具类
 * @Author Created by Lihaodong (alias:小东啊) lihaodongmail@163.com
 * @Date 2019-07-22 16:15
 * @Version 1.0
 */
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 获取所有key value
     *
     * @return
     */
    public List<RedisVo> getAll() {
        List<RedisVo> redisList = new ArrayList<>();
        // 获取所有key
        Set<Object> keys = redisTemplate.keys("*");
        if (keys != null) {
            redisList = keys.stream().map(i -> new RedisVo((String) i, JSON.toJSONString(redisTemplate.opsForValue().get(i)), redisTemplate.getExpire(i))).collect(Collectors.toList());
        }
        return redisList;
    }

    /**
     * 通过key删除value
     *
     * @param keys
     * @return
     */
    public boolean removeKey(List<String> keys) {
        return redisTemplate.delete(keys);
    }


}
