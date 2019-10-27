package com.dhf.redislearning.item._02_web;

import com.dhf.redislearning.item.AllKindOfBaseCommand;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 实现添加商品到购物车功能
 */
@Service
public class AddToCard {
    @Resource
    private AllKindOfBaseCommand allKindOfBaseCommand;

    /**
     * @param session 当前用户的sessionID
     * @param item    商品ID
     * @param count   商品数量，小于等于0时表示从购物车移除商品
     */
    public void addToCard(String session, String item, int count) {
        if (count <= 0) {
            allKindOfBaseCommand.hdel("cart:" + session, item);
        } else {
            allKindOfBaseCommand.hset("cart:" + session, item, String.valueOf(count));
        }
    }
}
