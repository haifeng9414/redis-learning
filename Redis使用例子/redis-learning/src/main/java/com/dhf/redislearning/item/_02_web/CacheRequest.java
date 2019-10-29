package com.dhf.redislearning.item._02_web;

import com.dhf.redislearning.item.AllKindOfBaseCommand;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 缓存请求结果
 */
@Service
public class CacheRequest {
    @Resource
    private AllKindOfBaseCommand allKindOfBaseCommand;

    /**
     * 判断请求是否可以被缓存
     */
    public boolean canCache(String request) {
        return true;
    }

    /**
     * 根据请求返回其hash作为ID，正常情况下一个请求不会是一个字符串，这里简单起见直接将请求视为字符串
     */
    public String hashRequest(String request) {
        return String.valueOf(request.hashCode());
    }

    public String cacheRequest(String request, Function<String, String> callback) {
        if (!canCache(request)) {
            return callback.apply(request);
        }

        String cacheKey = "cache:" + hashRequest(request);
        String content = allKindOfBaseCommand.get(cacheKey);

        if (StringUtils.isEmpty(content)) {
            content = callback.apply(request);
            // 缓存结果，300秒后过期
            allKindOfBaseCommand.set(cacheKey, content, 300);
        }

        return content;
    }
}
