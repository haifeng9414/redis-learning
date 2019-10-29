package com.dhf.redislearning.item._03_purchase;

import io.lettuce.core.TransactionResult;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 购买商品
 */
@Service
public class PurchaseItem {
    private RedisCommands<String, String> redisCommands;

    @Autowired
    private void setRedisCommands(StatefulRedisConnection<String, String> singleRedisConnection) {
        this.redisCommands = singleRedisConnection.sync();
    }

    public boolean purchaseItem(String buyerId, String itemId, String sellerId, double lprice) {
        String buyer = "users:" + buyerId;
        String seller = "users:" + sellerId;
        String item = itemId + '.' + sellerId;
        String inventory = "inventory:" + buyerId;
        long end = System.currentTimeMillis() + 10000;

        while (System.currentTimeMillis() < end) {
            redisCommands.watch("market:", buyer);

            // 获取市场中商品价格
            double price = Optional.ofNullable(redisCommands.zscore("market:", item)).orElse(Double.MAX_VALUE);
            // 获取用户剩余金钱
            double funds = Optional.ofNullable(redisCommands.hget(buyer, "funds")).map(Double::parseDouble).orElse(Double.MIN_VALUE);
            if (price != lprice || price > funds) {
                redisCommands.unwatch();
                return false;
            }

            redisCommands.multi();
            // 卖家剩余金钱加上当前商品价格
            redisCommands.hincrbyfloat(seller, "funds", price);
            // 买家剩余金钱减去当前商品价格
            redisCommands.hincrbyfloat(buyer, "funds", -price);
            // 商品保存到买家背包
            redisCommands.sadd(inventory, itemId);
            // 从市场移除商品
            redisCommands.zrem("market:", item);
            final TransactionResult result = redisCommands.exec();
            // null response indicates that the transaction was aborted due to
            // the watched key changing.
            if (result.isEmpty()) {
                continue;
            }
            return true;
        }

        return false;
    }
}
