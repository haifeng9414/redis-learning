package com.dhf.redislearning.item._01_article;

import com.dhf.redislearning.item.AllKindOfBaseCommand;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 获取群组中的文章，并按照指定条件排序
 */
@Service
public class GetGroupArticles {
    @Resource
    private AllKindOfBaseCommand allKindOfBaseCommand;
    @Resource
    private GetArticles getArticles;

    /**
     * @param group 群组ID
     * @param page  页数
     * @param order 排序条件key，即保存了文章分数的集合的key，如'score:'或'time:'等
     * @return 文章信息集合
     */
    public List<Map<String, String>> getGroupArticles(String group, int page, String order) {
        String key = order + group;

        if (!allKindOfBaseCommand.exists(key)) {
            // zinterstore命令接受多个有序集合或者集合（普通集合的数值为1），取这些集合的交集并按照指定的条件排序，并将结果保存到第一个参数
            // 指定的key中，这里传入集合group和有序集合order，相当于取存在于群组group中的文章，并按照order集合中的数值从大到小排序
            allKindOfBaseCommand.zinterstore("group:" + group, key, RedisZSetCommands.Aggregate.MAX, order);
            // 如果集合过大，则zinterstore命令会比较长的时间计算结果，这里将zinterstore命令的结果缓存60秒
            allKindOfBaseCommand.expire(key, 60, TimeUnit.SECONDS);
        }

        // 取结果的指定页的文章，这里以key作为获取文章的排序条件，key保存了群组中的文章按照order排序后的结果
        return getArticles.getArticles(page, key);
    }
}
