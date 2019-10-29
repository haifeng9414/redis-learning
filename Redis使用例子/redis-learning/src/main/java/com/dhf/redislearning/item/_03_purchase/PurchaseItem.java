package com.dhf.redislearning.item._03_purchase;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * 购买商品
 */
@Service
public class PurchaseItem {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public boolean purchaseItem(String buyerId, String itemId, String sellerId, double lprice) {
        String buyer = "users:" + buyerId;
        String seller = "users:" + sellerId;
        String item = itemId + '.' + sellerId;
        String inventory = "inventory:" + buyerId;
        long end = System.currentTimeMillis() + 10000;

        while (System.currentTimeMillis() < end) {
            final Boolean result = stringRedisTemplate.execute((RedisCallback<Boolean>) connection -> {
                connection.watch("market:".getBytes(), buyer.getBytes());

                // 获取市场中商品价格
                double price = Optional.ofNullable(connection.zScore("market:".getBytes(), item.getBytes())).orElse(Double.MAX_VALUE);
                // 获取用户剩余金钱
                double funds = Optional.ofNullable(connection.hGet(buyer.getBytes(), "funds".getBytes())).map(val -> {
                    return Double.parseDouble(new String(val));
                }).orElse(Double.MIN_VALUE);
                if (price != lprice || price > funds) {
                    connection.unwatch();
                    return false;
                }

                connection.multi();
                // 卖家剩余金钱加上当前商品价格
                connection.hIncrBy(seller.getBytes(), "funds".getBytes(), price);
                // 买家剩余金钱减去当前商品价格
                connection.hIncrBy(buyer.getBytes(), "funds".getBytes(), -price);
                // 商品保存到买家背包
                connection.sAdd(inventory.getBytes(), itemId.getBytes());
                // 从市场移除商品
                connection.zRem("market:".getBytes(), item.getBytes());
                List<Object> results = connection.exec();
                // null response indicates that the transaction was aborted due to
                // the watched key changing.
                if (results == null) {
                    return false;
                }
                return true;
            });

            // 成功则返回true，否则重试
            if (Boolean.TRUE.equals(result)) {
                return true;
            }
        }

        return false;
    }
}
