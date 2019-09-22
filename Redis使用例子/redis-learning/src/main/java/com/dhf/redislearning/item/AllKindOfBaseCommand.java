package com.dhf.redislearning.item;

import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@SuppressWarnings("WeakerAccess")
public class AllKindOfBaseCommand {
    @Resource
    private Jedis jedis;

    /*
    ----------------------------------------------------------
    common
    ----------------------------------------------------------
     */
    public void del(String key) {
        jedis.del(key);
    }

    /*
    ----------------------------------------------------------
    string
    ----------------------------------------------------------
     */
    public String get(String key) {
        return jedis.get(key);
    }

    public void set(String key, String value) {
        jedis.set(key, value);
    }

    /*
    ----------------------------------------------------------
    list
    ----------------------------------------------------------
     */
    public List<String> lrange(String key, long start, long stop) {
        return jedis.lrange(key, start, stop);
    }

    public List<String> getAllListValue(String key) {
        return lrange(key, 0, -1);
    }

    public String lindex(String key, long index) {
        return jedis.lindex(key, index);
    }

    public void lpush(String key, String... values) {
        jedis.lpush(key, values);
    }

    public void rpush(String key, String... values) {
        jedis.rpush(key, values);
    }

    public String lpop(String key) {
        return jedis.lpop(key);
    }

    public String rpop(String key) {
        return jedis.rpop(key);
    }

    /*
    ----------------------------------------------------------
    set
    ----------------------------------------------------------
     */
    public Set<String> smembers(String key) {
        return jedis.smembers(key);
    }

    public Boolean sismember(String key, String value) {
        return jedis.sismember(key, value);
    }

    public void srem(String key, String... members) {
        jedis.srem(key, members);
    }

    public void sadd(String key, String... members) {
        jedis.sadd(key, members);
    }

    /*
    ----------------------------------------------------------
    hash
    ----------------------------------------------------------
     */
    public Map<String, String> hgetAll(String key) {
        return jedis.hgetAll(key);
    }

    public String hget(String key, String subKey) {
        return jedis.hget(key, subKey);
    }

    public void hdel(String key, String... subKey) {
        jedis.hdel(key, subKey);
    }

    public void hset(String key, String subKey, String value) {
        jedis.hset(key, subKey, value);
    }

    /*
    ----------------------------------------------------------
    zset
    ----------------------------------------------------------
     */
    public Set<String> zrange(String key, long start, long stop) {
        return jedis.zrange(key, start, stop);
    }

    public Set<Tuple> zrangeWithScores(String key, long start, long stop) {
        return jedis.zrangeWithScores(key, start, stop);
    }

    public Set<String> getAllZSetValue(String key) {
        return jedis.zrange(key, 0, -1);
    }

    public void zadd(String key, double score, String members) {
        jedis.zadd(key, score, members);
    }

    public void zrem(String key, String... members) {
        jedis.zrem(key, members);
    }

    public Set<String> zrangeByScore(String key, double min, double max) {
        return jedis.zrangeByScore(key, min, max);
    }
}
