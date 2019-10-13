package com.dhf.redislearning.item.article;

import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.List;

/**
 * 将文章添加到群组或从群组中移除
 */
@Service
public class AddRemoteGroups {
    @Resource
    private Jedis jedis;

    /**
     * @param articleId 文章ID
     * @param addGroups 需要添加文章的群组ID列表
     * @param removeGroups 需要移除文章的群组ID列表
     */
    public void addRemoveGroups(String articleId, List<String> addGroups, List<String> removeGroups) {
        String article = "article:" + articleId;

        for (String groupId : addGroups) {
            jedis.sadd("group:" + groupId, article);
        }
        for (String groupId : removeGroups) {
            jedis.srem("group:" + groupId, article);
        }
    }
}
