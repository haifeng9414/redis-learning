package com.dhf.redislearning.item.article;

import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.HashMap;

import static com.dhf.redislearning.item.article.ArticleVote.ONE_WEEK_IN_SECOND;
import static com.dhf.redislearning.item.article.ArticleVote.VOTE_SCORE;

/**
 * 发布文章功能
 */
@Service
public class PostArticle {
    @Resource
    private Jedis jedis;

    /**
     * @param userId 发布文章的用户
     * @param title 文章名称
     * @param link 文章链接
     * @return 文章ID
     */
    public String postArticle(String userId, String title, String link) {
        // 利用redis的自增功能生成articleId
        long articleId = jedis.incr("article:");

        String voted = "voted:" + articleId;
        // 默认发布者已经为文章投票
        jedis.sadd(voted, userId);
        // voted集合有效时间为一个星期，一个星期后不能再投票
        jedis.expire(voted, ONE_WEEK_IN_SECOND);

        long now = System.currentTimeMillis();
        String article = "article:" + articleId;

        // 用hash保存文章信息
        jedis.hmset(article, new HashMap<String, String>() {{
            put("title", title);
            put("link", link);
            put("poster", userId);
            // 文章的票数
            put("votes", "1");
        }});
        jedis.zadd("time:", now, article);
        jedis.zadd("score:", now + VOTE_SCORE, article);

        return String.valueOf(articleId);
    }
}
