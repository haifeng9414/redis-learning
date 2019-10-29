package com.dhf.redislearning.item._03_purchase;

import io.lettuce.core.TransactionResult;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 实现用户贩卖商品功能
 */
@Service
public class ListItem {
    private final static String MARKET_KEY = "market:";
    private RedisCommands<String, String> redisCommands;

    @Autowired
    private void setRedisCommands(StatefulRedisConnection<String, String> singleRedisConnection) {
        this.redisCommands = singleRedisConnection.sync();
    }

    public boolean listItem(String itemId, String sellerId, double price) {
        String inventory = String.format("inventory:%s", sellerId);
        String item = String.format("%s:%s", itemId, sellerId);

        final long end = System.currentTimeMillis() + 5000;

        while (System.currentTimeMillis() < end) {
            try {
                // 监听用户背包，确保贩卖的商品存在
                redisCommands.watch(inventory);
                if (Boolean.FALSE.equals(redisCommands.sismember(inventory, itemId))) {
                    // 不存在该商品则直接返回
                    redisCommands.unwatch();
                    return false;
                }
                // 开启事务
                redisCommands.multi();
                // 将商品添加到市场
                redisCommands.zadd(MARKET_KEY, price, item);
                // 将商品从用户背包中移除
                redisCommands.srem(inventory, itemId);
                final TransactionResult result = redisCommands.exec();
                // 返回空说明watch的key数据发生了变化，重试
                if (result.isEmpty()) {
                    continue;
                }
                return true;
            } catch (Exception e) {
                // 执行失败了则重试
                e.printStackTrace();
            }
        }
        return false;
    }
}
