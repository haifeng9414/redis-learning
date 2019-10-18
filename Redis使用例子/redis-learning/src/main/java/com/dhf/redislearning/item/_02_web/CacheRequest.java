package com.dhf.redislearning.item._02_web;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import javax.annotation.Resource;
import java.util.function.Function;

/**
 * 缓存请求结果
 */
@Service
public class CacheRequest {
    @Resource
    private Jedis jedis;

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
        String content = jedis.get(cacheKey);

        if (StringUtils.isEmpty(content)) {
            content = callback.apply(request);
            // 缓存结果，300秒后过期
            jedis.set(cacheKey, content, SetParams.setParams().ex(300));
        }

        return content;
    }
}
