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

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String, String> singleRedisConnection(RedisClient redisClient) {
        return redisClient.connect();
    }
}
