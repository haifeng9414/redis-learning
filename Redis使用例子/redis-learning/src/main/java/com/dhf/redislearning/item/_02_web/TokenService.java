package com.dhf.redislearning.item._02_web;

import com.dhf.redislearning.item.AllKindOfBaseCommand;
import io.lettuce.core.ZStoreArgs;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 实现token的相关操作，token中保存了用户的登录信息
 */
@Service
public class TokenService {
    @Resource
    private AllKindOfBaseCommand allKindOfBaseCommand;

    /**
     * @param token 用户token
     * @return 用户ID
     */
    public String checkToken(String token) {
        return allKindOfBaseCommand.hget("login:", token);
    }

    public void updateToken(String token, String user, List<String> items) {
        long now = System.currentTimeMillis();

        /* 保存用户和其token的映射关系 */
        allKindOfBaseCommand.hset("login:", token, user);
        /* 记录最近登录用户 */
        allKindOfBaseCommand.zadd("recent:", now, token);

        if (!CollectionUtils.isEmpty(items)) {
            for (String item : items) {
                allKindOfBaseCommand.zadd("viewed:" + token, now, item);
                /*
                 * 只保存25个最近浏览的商品，zremrangeByRank按照数值移除元素，默认是从小到大的顺序，而'viewed:' + token有序集合的
                 * 数值为浏览时间，所以最新的25个浏览商品为'viewed:' + token有序集合的倒数25个元素
                 * 这里直接移除'viewed:' + token有序集合的0到倒数26的元素，如果元素个数不足26个，则zremrangeByRank方法什么元素也不会删除
                 * 也就是删除时的start和stop需要在元素的下标范围内
                 */
                allKindOfBaseCommand.zremrangeByRank("viewed:" + token, 0, -26);
                // viewed:有序集合保存了所有商品的浏览次数，每次-1而不是+1使得浏览的多的商品数值更小，在有序集合中排在前头，方便下面的清理工作
                allKindOfBaseCommand.zincrby("viewed:", -1, token);
            }
        }
    }

    /**
     * 为了让商品浏览次数排行榜能够保持最新，需要定期修剪有序集合的长度并调整已有元素的分值，从而使得新流行的商品也可以在排行榜里面占据一席之地
     */
    public void rescaleViewed() throws InterruptedException {
        while (true) {
            // 删除20000名之后的商品浏览记录
            allKindOfBaseCommand.zremrangeByRank("viewed:", 20000, -1);
            // zinterstore方法能够对多个有序集合的交集进行计算（求和、取最大值/最小值），第一个参数为保存结果的key，这里用ZParams
            // 设置viewed:的权重为0.5，即该集合中的值参与计算时数值减半，最后再将结果保存到viewed:，从而实现了将viewed:集合中的所有数值减半的效果
            final ZStoreArgs weights = ZStoreArgs.Builder
                    .sum()
                    .weights(0.5);
            allKindOfBaseCommand.zinterstore("viewed:", weights, "viewed:");
            // 每5分钟执行一次
            TimeUnit.MINUTES.sleep(5);
        }
    }
}
