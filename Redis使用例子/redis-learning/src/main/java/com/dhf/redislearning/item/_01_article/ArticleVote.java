package com.dhf.redislearning.item._01_article;

import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;

/**
 * 文章投票功能
 */
@Service
public class ArticleVote {
    /* 新发布的文章一个星期后不能再投票 */
    final static int ONE_WEEK_IN_SECOND = 7 * 86400;
    /* 投一票文章获得的分数 */
    final static int VOTE_SCORE = 10;

    @Resource
    private Jedis jedis;

    /**
     * @param userId 投票的用户
     * @param article 'article:' + articleId
     */
    public void articleVote(String userId, String article) {
        long diff = System.currentTimeMillis() - ONE_WEEK_IN_SECOND;

        if (jedis.zscore("time:", article) < diff) {
            return;
        }

        String articleId = article.substring(article.indexOf(":") + 1);
        // 'voted:' + articleId的集合保存了某个文章已投票的用户id，一个用户只能为文章投一次票
        // 'voted:' + articleId集合的有效时间为一个星期，一个星期后该集合会被删除，过期时间在发布文章的实现中设置
        // jedis.sadd方法在不存在集合时自动创建集合，添加userId并返回1，已存在集合时添加userId到集合并返回1，如果集合中已存在userId则
        // 返回0
        ///todo: 下面的3个命令应该在一个事务中执行
        if (jedis.sadd("voted:" + articleId, userId) > 0) {
            // 为文章增加分数
            jedis.zincrby("score:", VOTE_SCORE, article);
            // key为article的hash保存了文章的信息，这里文章的投票数 + 1
            jedis.hincrBy(article, "votes", 1);
        }
    }
}
