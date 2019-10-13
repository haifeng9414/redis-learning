package com.dhf.redislearning.item._02_web;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.List;

/**
 * 实现token的相关操作，token中保存了用户的登录信息
 */
@Service
public class TokenService {
    @Resource
    private Jedis jedis;

    /**
     * @param token 用户token
     * @return 用户ID
     */
    public String checkToken(String token) {
        return jedis.hget("login:", token);
    }

    public void updateToken(String token, String user, List<String> items) {
        long now = System.currentTimeMillis();

        /* 保存用户和其token的映射关系 */
        jedis.hset("login:", token, user);
        /* 记录最近登录用户 */
        jedis.zadd("recent:", now, token);

        if (!CollectionUtils.isEmpty(items)) {
            for (String item : items) {
                jedis.zadd("viewed:" + token, now, item);
                /*
                 * 只保存25个最近浏览的商品，zremrangeByRank按照数值移除元素，默认是从小到大的顺序，而'viewed:' + token有序集合的
                 * 数值为浏览时间，所以最新的25个浏览商品为'viewed:' + token有序集合的倒数25个元素
                 * 这里直接移除'viewed:' + token有序集合的0到倒数26的元素，如果元素个数不足26个，则zremrangeByRank方法什么元素也不会删除
                 * 也就是删除时的start和stop需要在元素的下标范围内
                 */
                jedis.zremrangeByRank("viewed:" + token, 0, -26);
            }
        }
    }
}
