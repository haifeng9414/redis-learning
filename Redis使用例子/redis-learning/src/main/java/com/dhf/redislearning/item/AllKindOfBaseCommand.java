package com.dhf.redislearning.item;

import org.springframework.stereotype.Component;
import redis.clients.jedis.*;

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
    // 删除指定的key，返回被删除的key的个数
    public Long del(String... keys) {
        return jedis.del(keys);
    }

    /*
    针对字符串、列表、集合、有序集合、散列，对值进行排序，可以按照数字或字母顺序排序，默认按照数字排序，如果元素不能转换为数字则报错
    如果使用字母顺序排序：new SortingParams().alpha().asc()
    sort还支持将散列的数据作为权重对数据进行排序，如存在列表：sort-input [7 15 23 110]，如果直接调用sort sort-input命令则返回
    [7 15 23 110]，现在新建若干散列：
    hset d-7 field 5
    hset d-15 field 1
    hset d-23 field 9
    hset d-110 field 3

    此时调用sort sort-input by 'd-*->field'将返回[15 110 7 23]，该结果是按照这些分值对应的散列的field域指定的权重排序的，by选项后面
    的d-*->field由->分成两个部分，d-*用于设置分值对应的散列，field用于设置散列对应的键值对
    还可以使用get选项设置按权重排序的结果的值，调用sort sort-input by 'd-*->field'将返回[1 3 5 9]，get选项指定了如何获取结果，这里的
    d-*->field表示结果不再使用分值，而是其对应的权重
     */
    public void sort(String key, SortingParams sortingParams, String destKey) {
        jedis.sort(key, sortingParams, destKey);
    }

    // 移除key过期时间
    public Long persist(String key) {
        return jedis.persist(key);
    }

    // 查看key还有久过期（秒）
    public Long ttl(String key) {
        return jedis.ttl(key);
    }

    // 查看key还有久过期（毫秒）（Redis2.6以上可用）
    public Long pttl(String key) {
        return jedis.pttl(key);
    }

    // 设置key的过期时间（秒）
    public Long expire(String key, int seconds) {
        return jedis.expire(key, seconds);
    }

    // 设置key的过期时间（毫秒）
    public Long pexpire(String key, long milliseconds) {
        return jedis.pexpire(key, milliseconds);
    }

    // 设置key的过期时间（秒时间戳）
    public Long expireAt(String key, long unixTimestamp) {
        return jedis.expireAt(key, unixTimestamp);
    }

    // 设置key的过期时间（毫秒时间戳）
    public Long pexpireAt(String key, long milliUnixTimestamp) {
        return jedis.pexpireAt(key, milliUnixTimestamp);
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

    // 值加，不存在的key则值视为0
    public Long incr(String key) {
        return jedis.incr(key);
    }

    // 值减1
    public Long decr(String key) {
        return jedis.decr(key);
    }

    // 值加increment
    public Long incrBy(String key, long increment) {
        return jedis.incrBy(key, increment);
    }

    // 值减increment
    public Long decrBy(String key, long decrement) {
        return jedis.decrBy(key, decrement);
    }

    // 值加increment，针对浮点值，Redis2.6以上有效
    public Double incrByFloat(String key, double increment) {
        return jedis.incrByFloat(key, increment);
    }

    // value追加到指定key的值后，返回追加后的值长度，不存在key则报错
    public Long append(String key, String value) {
        return jedis.append(key, value);
    }

    // 获取指定范围内的值，包括start和end
    public String getrange(String key, long start, long end) {
        return jedis.getrange(key, start, end);
    }

    // 设置指定范围内的值，返回新值的长度
    public Long setrange(String key, long offset, String value) {
        return jedis.setrange(key, offset, value);
    }

    // 将字节串看作是二进制位串，并返回位串中偏移量为offset的二进制位的值
    // 如果原值为字符串1，其在Redis中保存为Ascii形式，即真实的值为1对应的Ascii码：00110001，getbit将会针对该值返回结果
    public Boolean getbit(String key, long offset) {
        return jedis.getbit(key, offset);
    }

    // 设置指定的key的offset位置的二进制值，同getbit一样，也是针对Ascii形式进行操作
    public Boolean setbit(String key, long offset, boolean value) {
        return jedis.setbit(key, offset, value);
    }

    // 统计二进制位串里面值为1的二进制位的数量，如果给定了可选的start偏移量和end偏移量，那么只对偏移量指定范围内的二进制位进行统计
    public Long bitcount(String key) {
        return jedis.bitcount(key);
    }

    // 对一个或多个二进制位串执行包括并（AND）、或（OR）、异或（XOR）、非（NOT）在内的任意一种按位运算操作，并将结果保存在destKey键里
    public Long bitop(BitOP op, String destKey, String... keys) {
        return jedis.bitop(op, destKey, keys);
    }

    /*
    ----------------------------------------------------------
    list
    相当于链表
    ----------------------------------------------------------
     */
    // 返回链表的长度
    public Long lpush(String key, String... values) {
        return jedis.lpush(key, values);
    }

    // 返回链表的长度
    public Long rpush(String key, String... values) {
        return jedis.rpush(key, values);
    }

    public String lpop(String key) {
        return jedis.lpop(key);
    }

    public String rpop(String key) {
        return jedis.rpop(key);
    }

    // 返回列表中偏移量为offset的元素
    public String lindex(String key, long offset) {
        return jedis.lindex(key, offset);
    }

    // 返回列表从start偏移量到end偏移量范围内的所有元素，包括start和end
    public List<String> lrange(String key, long start, long end) {
        return jedis.lrange(key, start, end);
    }

    // 对列表进行修剪，只保留从start偏移量到end偏移量范围内的元素，，包括start和end
    public String ltrim(String key, long start, long end) {
        return jedis.ltrim(key, start, end);
    }

    // 从第一个非空列表中弹出位于最左端的元素，或者在timeout秒之内阻塞并等待可弹出的元素出现
    public List<String> blpop(int timeout, String... keys) {
        return jedis.blpop(timeout, keys);
    }

    // 从第一个非空列表中弹出位于最右端的元素，或者在timeout秒之内阻塞并等待可弹出的元素出现
    public List<String> brpop(int timeout, String... keys) {
        return jedis.brpop(timeout, keys);
    }

    // 从srcKey列表中弹出位于最右端的元素，然后将这个元素推入destKey列表的最左端，并向用户返回这个元素
    public String rpoplpush(String srcKey, String destKey) {
        return jedis.rpoplpush(srcKey, destKey);
    }

    // 从srcKey列表中弹出位于最右端的元素，然后将这个元素推入destKey列表的最左端，并向用户返回这个元素，如果srcKey为空，那么在timeout秒之内阻塞并等待可弹出的元素出现
    public String brpoplpush(String srcKey, String destKey, int timeout) {
        return jedis.brpoplpush(srcKey, destKey, timeout);
    }

    /*
    ----------------------------------------------------------
    set
    无序的存储各不相同的元素，多个集合之间还可以进行组合和关联
    ----------------------------------------------------------
     */
    // 将一个或多个元素添加到集合里面，并返回被添加元素当中原本并不存在于集合里面的元素数量
    public Long sadd(String key, String... members) {
        return jedis.sadd(key, members);
    }

    // 从集合里面移除一个或多个元素，并返回被移除元素的数量
    public Long srem(String key, String... members) {
        return jedis.srem(key, members);
    }

    public Boolean sismember(String key, String value) {
        return jedis.sismember(key, value);
    }

    // 返回集合元素的数量
    public Long scard(String key) {
        return jedis.scard(key);
    }

    // 返回所有集合元素
    public Set<String> smembers(String key) {
        return jedis.smembers(key);
    }

    // 从集合里面随机地返回一个或多个元素。当count为正数时，命令返回的随机元素不会重复；当count为负数时，命令返回的随机元素可能会出现重复
    public List<String> srandmember(String key, int count) {
        return jedis.srandmember(key, count);
    }

    // 随机地移除集合中指定数量的元素，并返回被移除的元素
    public Set<String> spop(String key, int count) {
        return jedis.spop(key, count);
    }

    // 如果集合srcKey包含元素item，那么从集合srcKey里面移除元素item，并将元素item添加到集合destKey中；如果item被成功移除，返回1，否则0
    public Long smove(String srcKey, String destKey, String item) {
        return jedis.smove(srcKey, destKey, item);
    }

    // 下面是针对多个集合的操作

    // 返回那些存在于第一个集合、但不存在于其他集合中的元素（差集）
    public Set<String> sdiff(String... keys) {
        return jedis.sdiff(keys);
    }

    // 将那些存在于第一个集合但并不存在于其他集合中的元素（差集）存储到destKey键里面
    public Long sdiffstore(String destKey, String... keys) {
        return jedis.sdiffstore(destKey, keys);
    }

    // 返回那些同时存在于所有集合中的元素（交集）
    public Set<String> sinter(String... keys) {
        return jedis.sinter(keys);
    }

    // 同时存在于所有集合中的元素（交集）存储到destKey键里面
    public Long sinterstore(String destKey, String... keys) {
        return jedis.sinterstore(destKey, keys);
    }

    // 返回那些至少存在于一个集合中的元素（并集）
    public Set<String> sunion(String... keys) {
        return jedis.sunion(keys);
    }

    // 至少存在于一个集合中的元素（并集）存储到destKey键里面
    public Long sunion(String destKey, String... keys) {
        return jedis.sunionstore(destKey, keys);
    }

    /*
    ----------------------------------------------------------
    hash
    ----------------------------------------------------------
     */
    public String hget(String key, String subKey) {
        return jedis.hget(key, subKey);
    }

    // 为散列里面的一个或多个键设置值
    public void hset(String key, Map<String, String> hash) {
        jedis.hset(key, hash);
    }

    // 删除指定的键值对，返回成功删除的键值对数量
    public Long hdel(String key, String... subKey) {
         return jedis.hdel(key, subKey);
    }

    // 返回键值对数量
    public Long hlen(String key) {
        return jedis.hlen(key);
    }

    // 判断是否存在指定键值对
    public Boolean hexists(String key, String field) {
        return jedis.hexists(key, field);
    }

    // 返回所有键值对的键
    public Set<String> hkeys(String key) {
        return jedis.hkeys(key);
    }

    // 返回所有键值对的值
    public List<String> hvals(String key) {
        return jedis.hvals(key);
    }

    // 返回所有键值对
    public Map<String, String> hgetAll(String key) {
        return jedis.hgetAll(key);
    }

    // 将键key存储的值加上整数increment
    public Long hincrBy(String key, String field, Long increment) {
        return jedis.hincrBy(key, field, increment);
    }

    // 将键key存储的值加上浮点数value
    public Double hincrByFloat(String key, String field, double value) {
        return jedis.hincrByFloat(key, field, value);
    }

    /*
    ----------------------------------------------------------
    zset
    有序集合
    ----------------------------------------------------------
     */
    public void zadd(String key, double score, String members) {
        jedis.zadd(key, score, members);
    }

    public void zrem(String key, String... members) {
        jedis.zrem(key, members);
    }

    // 返回有序集合元素数量
    public Long zcard(String key) {
        return jedis.zcard(key);
    }

    // 将指定元素的值加上increment
    public Double zincrby(String key, double increment, String member) {
        return jedis.zincrby(key, increment, member);
    }

    // 返回分值在min到max之间的元素个数
    public Long zcount(String key, double min, double max) {
        return jedis.zcount(key, min, max);
    }

    // 返回分值在min到max之间的元素
    public Set<String> zrangeByScore(String key, double min, double max) {
        return jedis.zrangeByScore(key, min, max);
    }

    // 返回指定元素的排名
    public Long zrank(String key, String member) {
        return jedis.zrank(key, member);
    }

    // 返回指定元素的分值
    public Double zscore(String key, String member) {
        return jedis.zscore(key, member);
    }

    // 从小到大返回指定排名之间的元素
    public Set<String> zrange(String key, long start, long stop) {
        return jedis.zrange(key, start, stop);
    }

    // 返回指定排名之间的元素和分值
    public Set<Tuple> zrangeWithScores(String key, long start, long stop) {
        return jedis.zrangeWithScores(key, start, stop);
    }

    // 按从大到小的顺序，返回指定元素的排名
    public Long zrevrank(String key, String member) {
        return jedis.zrevrank(key, member);
    }

    // 从大到小返回指定排名之间的元素
    public Set<String> zrevrange(String key, long start, long stop) {
        return jedis.zrevrange(key, start, stop);
    }

    // 从大到小返回指定排名之间的元素和分值
    public Set<Tuple> zrevrangeWithScores(String key, long start, long stop) {
        return jedis.zrevrangeWithScores(key, start, stop);
    }

    // 从大到小返回指定分值之间的元素
    public Set<String> zrevrangeByScore(String key, double min, double max) {
        return jedis.zrevrangeByScore(key, min, max);
    }

    // 删除指定排名之间的元素
    public Long zremrangeByRank(String key, long start, long stop) {
        return jedis.zremrangeByRank(key, start, stop);
    }

    // 删除指定分值之间的元素
    public Long zremrangeByScore(String key, double min, double max) {
        return jedis.zremrangeByScore(key, min, max);
    }

    // 对给定的有序集合的交集执行指定的操作（MAX、MIN、SUM），结果保存到destKey
    public Long zremrangeByScore(String destKey, ZParams zParams, String... keys) {
        return jedis.zinterstore(destKey, zParams, keys);
    }

    // 对给定的有序集合的并集执行指定的操作（MAX、MIN、SUM），结果保存到destKey
    public Long zunionstore(String destKey, ZParams zParams, String... keys) {
        return jedis.zunionstore(destKey, zParams, keys);
    }

    /*
    ----------------------------------------------------------
    发布和订阅，jedis的发布和订阅功能和JedisPubSub对象有关联，这里直接放方法不太直观，直接看单测：SubscribeTest

    Redis的发布和订阅有两个比较重要的缺点：
    第一个和Redis系统的稳定性有关。对于旧版Redis来说，如果一个客户端订阅了某个
    或某些频道，但它读取消息的速度却不够快的话，那么不断积压的消息就会使得Redis输出缓冲
    区的体积变得越来越大，这可能会导致Redis的速度变慢，甚至直接崩溃。也可能会导致Redis
    被操作系统强制杀死，甚至导致操作系统本身不可用。新版的Redis不会出现这种问题，因为它
    会自动断开不符合 client-output-buffer-limit pubsub 配置选项要求的订阅客户端

    第二个和数据传输的可靠性有关。任何网络系统在执行操作时都可能会遇上断线情况，
    而断线产生的连接错误通常会使得网络连接两端中的其中一端进行重新连接，如果客户端
    在执行订阅操作的过程中断线，那么客户端将丢失在断线期间发送的所有消息
    ----------------------------------------------------------
     */
}
