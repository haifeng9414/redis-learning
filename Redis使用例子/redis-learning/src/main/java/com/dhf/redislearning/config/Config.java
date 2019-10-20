package com.dhf.redislearning.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class Config {
    @Bean
    public JedisPool jedisPool() {
        return new JedisPool(new JedisPoolConfig(), "localhost", 6379, 2000);
    }

    @Bean
    public Jedis jedis(JedisPool jedisPool) {
        return jedisPool.getResource();
    }
}
