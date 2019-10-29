package com.dhf.redislearning.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class Config {
    @Bean(destroyMethod = "shutdown")
    public ClientResources clientResources() {
        return DefaultClientResources.create();
    }

    @Bean(destroyMethod = "shutdown")
    public RedisClient redisClient(ClientResources clientResources, Environment environment) {
        return RedisClient.create(clientResources, RedisURI.create(environment.getProperty("redis.server", "localhost"), environment.getProperty("redis.port", Integer.class, 6379)));
    }

    // 注意lettuce中，如果多个线程使用同一个Connection对象，无论调用的是sync还是async，当某个线程开启了事务或者使用了阻塞命令，如blpop，会导致
    // 所有从该线程所属的Connection对象创建的Command被阻塞，这会导致一个线程的阻塞命令会影响到其他线程，可以通过调用RedisClient.connect()创建
    // 新的Connection对象防止被阻塞
    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String, String> singleRedisConnection(RedisClient redisClient) {
        return redisClient.connect();
    }
}
