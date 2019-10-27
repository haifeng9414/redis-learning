package com.dhf.redislearning.item._01_article;

import com.dhf.redislearning.item.AllKindOfBaseCommand;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 将文章添加到群组或从群组中移除
 */
@Service
public class AddRemoteGroups {
    @Resource
    private AllKindOfBaseCommand allKindOfBaseCommand;

    /**
     * @param articleId    文章ID
     * @param addGroups    需要添加文章的群组ID列表
     * @param removeGroups 需要移除文章的群组ID列表
     */
    public void addRemoveGroups(String articleId, List<String> addGroups, List<String> removeGroups) {
        String article = "article:" + articleId;

        for (String groupId : addGroups) {
            allKindOfBaseCommand.sadd("group:" + groupId, article);
        }
        for (String groupId : removeGroups) {
            allKindOfBaseCommand.srem("group:" + groupId, article);
        }
    }
}
