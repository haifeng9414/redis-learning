package com.dhf.redislearning.item._03_purchase;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 实现用户贩卖商品功能
 */
@Service
public class ListItem {
    private final static String MARKET_KEY = "market:";
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public boolean listItem(String itemId, String sellerId, double price) {
        String inventory = String.format("inventory:%s", sellerId);
        String item = String.format("%s:%s", itemId, sellerId);

        final long end = System.currentTimeMillis() + 5000;

        while (System.currentTimeMillis() < end) {
            try {
                List<Object> result = stringRedisTemplate.executePipelined((RedisCallback<Boolean>) connection -> {
                    // 监听用户背包，确保贩卖的商品存在
                    connection.watch(inventory.getBytes());
                    if (Boolean.FALSE.equals(connection.sIsMember(inventory.getBytes(), itemId.getBytes()))) {
                        // 不存在该商品则直接返回
                        connection.unwatch();
                        return false;
                    }
                    // 开启事务
                    connection.multi();
                    // 将商品添加到市场
                    connection.zAdd(MARKET_KEY.getBytes(), price, item.getBytes());
                    // 将商品从用户背包中移除
                    connection.sRem(inventory.getBytes(), itemId.getBytes());
                    connection.exec();
                    return true;
                });

                // 返回空说明watch的key数据发生了变化，重试
                if (result == null) {
                    continue;
                }

                return (boolean) result.get(0);
            } catch (Exception e) {
                // 执行失败了则重试
                e.printStackTrace();
            }
        }
        return false;
    }
}
