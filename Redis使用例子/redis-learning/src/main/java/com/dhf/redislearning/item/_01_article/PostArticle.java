package com.dhf.redislearning.item._01_article;

import com.dhf.redislearning.item.AllKindOfBaseCommand;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.dhf.redislearning.item._01_article.ArticleVote.ONE_WEEK_IN_SECOND;
import static com.dhf.redislearning.item._01_article.ArticleVote.VOTE_SCORE;

/**
 * 发布文章功能
 */
@Service
public class PostArticle {
    @Resource
    private AllKindOfBaseCommand allKindOfBaseCommand;

    /**
     * @param userId 发布文章的用户
     * @param title  文章名称
     * @param link   文章链接
     * @return 文章ID
     */
    public String postArticle(String userId, String title, String link) {
        // 利用redis的自增功能生成articleId
        long articleId = allKindOfBaseCommand.incr("article:");

        String voted = "voted:" + articleId;
        // 默认发布者已经为文章投票
        allKindOfBaseCommand.sadd(voted, userId);
        // voted集合有效时间为一个星期，一个星期后不能再投票
        allKindOfBaseCommand.expire(voted, ONE_WEEK_IN_SECOND);

        long now = System.currentTimeMillis();
        String article = "article:" + articleId;

        // 用hash保存文章信息
        allKindOfBaseCommand.hmset(article, new HashMap<String, String>() {{
            put("title", title);
            put("link", link);
            put("poster", userId);
            // 文章的票数
            put("votes", "1");
        }});
        allKindOfBaseCommand.zadd("time:", now, article);
        allKindOfBaseCommand.zadd("score:", now + VOTE_SCORE, article);

        return String.valueOf(articleId);
    }
}
