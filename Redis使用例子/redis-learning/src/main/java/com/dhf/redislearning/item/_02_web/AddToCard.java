package com.dhf.redislearning.item._02_web;

import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;

/**
 * 实现添加商品到购物车功能
 */
@Service
public class AddToCard {
    @Resource
    private Jedis jedis;

    /**
     * @param session 当前用户的sessionID
     * @param item 商品ID
     * @param count 商品数量，小于等于0时表示从购物车移除商品
     */
    public void addToCard(String session, String item, int count) {
        if (count <= 0) {
            jedis.hdel("cart:" + session, item);
        } else {
            jedis.hset("cart:" + session, item, String.valueOf(count));
        }
    }
}
