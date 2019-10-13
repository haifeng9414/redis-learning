package com.dhf.redislearning.item.article;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ArticleTest {
    @Resource
    private Jedis jedis;
    @Resource
    private PostArticle postArticle;
    @Resource
    private ArticleVote articleVote;
    @Resource
    private GetArticles getArticles;
    @Resource
    private AddRemoteGroups addRemoteGroups;
    @Resource
    private GetGroupArticles getGroupArticles;

    @After
    public void removeAllKeys() {
        Set<String> keys = jedis.keys("*");
        jedis.del(keys.toArray(new String[]{}));
    }

    @Test
    public void test() {
        String articleId = postArticle.postArticle("username", "A title", "http://www.google.com");
        System.out.println("We posted a new article with id: " + articleId);
        System.out.println("Its HASH looks like:");
        Map<String,String> articleData = jedis.hgetAll("article:" + articleId);
        for (Map.Entry<String,String> entry : articleData.entrySet()){
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }

        System.out.println();

        articleVote.articleVote("other_user", "article:" + articleId);
        String votes = jedis.hget("article:" + articleId, "votes");
        System.out.println("We voted for the article, it now has votes: " + votes);
        assert Integer.parseInt(votes) > 1;

        System.out.println("The currently highest-scoring articles are:");
        List<Map<String,String>> articles = getArticles.getArticles(1, "score:");
        printArticles(articles);
        assert articles.size() >= 1;

        addRemoteGroups.addRemoveGroups(articleId, Collections.singletonList("new-group"), Collections.emptyList());
        System.out.println("We added the article to a new group, other articles include:");
        articles = getGroupArticles.getGroupArticles("new-group", 1, "score:");
        printArticles(articles);
        assert articles.size() >= 1;
    }

    private void printArticles(List<Map<String,String>> articles){
        for (Map<String,String> article : articles){
            System.out.println("  id: " + article.get("id"));
            for (Map.Entry<String,String> entry : article.entrySet()){
                if (entry.getKey().equals("id")){
                    continue;
                }
                System.out.println("    " + entry.getKey() + ": " + entry.getValue());
            }
        }
    }
}
