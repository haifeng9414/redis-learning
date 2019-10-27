package com.dhf.redislearning.item._01_article;

import com.dhf.redislearning.item.AllKindOfBaseCommand;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 按页获取文章
 */
@Service
public class GetArticles {
    private final static int ARTICLE_PER_PAGE = 10;

    @Resource
    private AllKindOfBaseCommand allKindOfBaseCommand;

    /**
     * @param page  需要获取的文章列表的页号
     * @param order 排序条件key，即保存了文章分数的集合的key，如'score:'或'time:'等
     * @return 文章信息集合
     */
    public List<Map<String, String>> getArticles(int page, String order) {
        int start = (page - 1) * ARTICLE_PER_PAGE;
        int end = start + ARTICLE_PER_PAGE - 1;

        Set<String> ids = allKindOfBaseCommand.zrevrange(order, start, end);

        List<Map<String, String>> result = new ArrayList<>();
        for (String id : ids) {
            Map<String, String> articleData = allKindOfBaseCommand.hgetAll(id);
            articleData.put("id", id);
            result.add(articleData);
        }

        return result;
    }
}
