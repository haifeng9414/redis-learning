package com.dhf.redislearning.item;

import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@SuppressWarnings("WeakerAccess")
public class AllKindOfBaseCommand {
    private RedisCommands<String, String> redisCommands;

    @Autowired
    private void setRedisCommands(StatefulRedisConnection<String, String> singleRedisConnection) {
        this.redisCommands = singleRedisConnection.sync();
    }

    /*
    ----------------------------------------------------------
    common
    ----------------------------------------------------------
     */
    // 删除指定的key，返回被删除的key的个数
    public Long del(String... keys) {
        return redisCommands.del(keys);
    }

    // 判断key是否存在，返回存在的key的个数
    public Long exists(String... keys) {
        return redisCommands.exists(keys);
    }

    // 判断key是否存在
    public boolean exists(String key) {
        return redisCommands.exists(key) > 0;
    }

    // 返回匹配指定pattern的key
    public List<String> keys(String pattern) {
        return redisCommands.keys(pattern);
    }

    /*
    针对列表、集合、有序集合、散列，对值进行排序，可以按照数字或字母顺序排序，默认按照数字排序，如果元素不能转换为数字则报错
    sort还支持外部Key作为权重对数据进行排序，如存在列表：sort-input [7 15 23 110]，如果直接调用sort sort-input命令则返回
    [7 15 23 110]，现在新建若干散列：
    hset d-7 field 5
    hset d-15 field 1
    hset d-23 field 9
    hset d-110 field 3

    此时调用sort sort-input by d-*->field将返回[15 110 7 23]，该结果是按照这些分值对应的散列的field域指定的权重排序的，by选项后面
    的d-*->field由->分成两个部分，d-*用于设置分值对应的散列，field用于设置散列对应的键值对
    还可以使用get选项设置按权重排序的结果的值，调用sort sort-input by d-*->field get d-*->field将返回[1 3 5 9]，get选项指定了如何获取结果，这里的
    d-*->field表示结果不再使用分值，而是其对应的权重

    get选项有一个特殊的值'#'，表示想要返回被排序的对象，也就是上面的7 15 23 110，get选项可以指定多个，结果按顺序返回，如调用sort sort-input by d-*->field get d-*->field get #
    将返回[1 15 3 110 5 7 9 23]，先返回1因为15对应的权重值最小，然后是15，因为还指定了get #，表示还要返回被排序对象本身，即15自己

    上面是针对权重保存在散列中的情况，Redis还支持使用普通字符串作为外部key，如上，可以创建四个字符串，每个字符串分别保存一个权重，如：
    d-7     5
    d-15    1
    d-23    9
    d-110   3
    则上面的sort sort-input by d-*->field写成sort sort-input by d-*会得到同样的结果[15 110 7 23]，写成sort sort-input by d-* get d-*
    返回[1 3 5 9]

    上面的例子用下面的代码，调用代码是
    SortArgs sortArgs = SortArgs.Builder
                .by("d-*")
                .get("d-*")
                .get("#")
                .limit(0, 10);

    return redisCommands.sortStore(key, sortArgs, destKey);
     */
    public Long sortStore(String key, SortArgs sortQuery, String destKey) {
        return redisCommands.sortStore(key, sortQuery, destKey);
    }

    // 不存储结果则直接返回结果
    public List<String> sort(String key, SortArgs sortArgs) {
        return redisCommands.sort(key, sortArgs);
    }

    // 移除key过期时间，如果存在key并且已过期，返回true
    public Boolean persist(String key) {
        return redisCommands.persist(key);
    }

    // 查看key还有久过期（秒）
    public Long ttl(String key) {
        return redisCommands.ttl(key);
    }

    // 设置key的过期时间（秒），如果存在key并且设置成功则返回true
    public Boolean expire(String key, long timeout) {
        return redisCommands.expire(key, timeout);
    }

    // 设置key的过期时间，如果存在key并且设置成功则返回true
    public Boolean expireAt(String key, Date date) {
        return redisCommands.expireat(key, date);
    }

    /*
    ----------------------------------------------------------
    string
    ----------------------------------------------------------
     */
    public String get(String key) {
        return redisCommands.get(key);
    }

    public void set(String key, String value) {
        redisCommands.set(key, value);
    }

    // 设置值的同时设置过期时间（秒）
    public void set(String key, String value, long timeout) {
        final SetArgs ex = SetArgs.Builder
                .ex(timeout);

        redisCommands.set(key, value, ex);
    }

    // 值加，不存在的key则值视为0
    public Long incr(String key) {
        return redisCommands.incr(key);
    }

    // 值减1
    public Long decr(String key) {
        return redisCommands.decr(key);
    }

    // 值加incr
    public Long incrBy(String key, long incr) {
        return redisCommands.incrby(key, incr);
    }

    // 值减incr
    public Long decrBy(String key, long decr) {
        return redisCommands.decrby(key, decr);
    }

    // 值加incr，针对浮点值，Redis2.6以上有效
    public Double incrByFloat(String key, double incr) {
        return redisCommands.incrbyfloat(key, incr);
    }

    // value追加到指定key的值后，返回追加后的值长度，不存在key则报错
    public Long append(String key, String value) {
        return redisCommands.append(key, value);
    }

    // 获取指定范围内的值，包括start和end
    public String getrange(String key, long start, long end) {
        return redisCommands.getrange(key, start, end);
    }

    // 设置指定范围内的值
    public void setrange(String key, long offset, String value) {
        redisCommands.setrange(key, offset, value);
    }

    // 将字节串看作是二进制位串，并返回位串中偏移量为offset的二进制位的值
    // 如果原值为字符串1，其在Redis中保存为Ascii形式，即真实的值为1对应的Ascii码：00110001，getbit将会针对该值返回结果
    public Long getbit(String key, long offset) {
        return redisCommands.getbit(key, offset);
    }

    // 设置指定的key的offset位置的二进制值，同getbit一样，也是针对Ascii形式进行操作
    public Long setbit(String key, long offset, int value) {
        return redisCommands.setbit(key, offset, value);
    }

    // 统计二进制位串里面值为1的二进制位的数量，如果给定了可选的start偏移量和end偏移量，那么只对偏移量指定范围内的二进制位进行统计
    public Long bitcount(String key) {
        return redisCommands.bitcount(key);
    }

    // 对一个或多个二进制位串执行包括并（AND）、或（OR）、异或（XOR）、非（NOT）在内的任意一种按位运算操作，并将结果保存在destKey键里
    public Long bitop(String destKey, String... keys) {
        return redisCommands.bitopAnd(destKey, keys);
//        return redisCommands.bitopOr(destKey, keys);
//        return redisCommands.bitopXor(destKey, keys);
//        return redisCommands.bitopNot(destKey, keys);
    }

    /*
    ----------------------------------------------------------
    list
    相当于链表
    ----------------------------------------------------------
     */
    // 返回链表的长度
    public Long llen(String key) {
        return redisCommands.llen(key);
    }

    public Long lpush(String key, String... values) {
        return redisCommands.lpush(key, values);
    }

    public Long rpush(String key, String... values) {
        return redisCommands.rpush(key, values);
    }

    public String lpop(String key) {
        return redisCommands.lpop(key);
    }

    public String rpop(String key) {
        return redisCommands.rpop(key);
    }

    // 返回列表中偏移量为offset的元素
    public String lindex(String key, long offset) {
        return redisCommands.lindex(key, offset);
    }

    // 返回列表从start偏移量到end偏移量范围内的所有元素，包括start和end
    public List<String> lrange(String key, long start, long end) {
        return redisCommands.lrange(key, start, end);
    }

    // 对列表进行修剪，只保留从start偏移量到end偏移量范围内的元素，包括start和end
    public void ltrim(String key, long start, long end) {
        redisCommands.ltrim(key, start, end);
    }

    // 从第一个非空列表中弹出位于最左端的元素，或者在timeout秒之内阻塞并等待可弹出的元素出现
    public KeyValue<String, String> blpop(long timeout, String... keys) {
        return redisCommands.blpop(timeout, keys);
    }

    // 从第一个非空列表中弹出位于最右端的元素，或者在timeout秒之内阻塞并等待可弹出的元素出现
    public KeyValue<String, String> brpop(int timeout, String... keys) {
        return redisCommands.brpop(timeout, keys);
    }

    // 从srcKey列表中弹出位于最右端的元素，然后将这个元素推入destKey列表的最左端，并向用户返回这个元素
    public String rpoplpush(String srcKey, String destKey) {
        return redisCommands.rpoplpush(srcKey, destKey);
    }

    // 从srcKey列表中弹出位于最右端的元素，然后将这个元素推入destKey列表的最左端，并向用户返回这个元素，如果srcKey为空，那么在timeout秒之内阻塞并等待可弹出的元素出现
    public String brpoplpush(String srcKey, String destKey, long timeout) {
        return redisCommands.brpoplpush(timeout, srcKey, destKey);
    }

    /*
    ----------------------------------------------------------
    set
    无序的存储各不相同的元素，多个集合之间还可以进行组合和关联
    ----------------------------------------------------------
     */
    // 将一个或多个元素添加到集合里面，并返回被添加元素当中原本并不存在于集合里面的元素数量
    public Long sadd(String key, String... members) {
        return redisCommands.sadd(key, members);
    }

    // 从集合里面移除一个或多个元素，并返回被移除元素的数量
    public Long srem(String key, String... members) {
        return redisCommands.srem(key, members);
    }

    public Boolean sismember(String key, String value) {
        return redisCommands.sismember(key, value);
    }

    // 返回集合元素的数量
    public Long scard(String key) {
        return redisCommands.scard(key);
    }

    // 返回所有集合元素
    public Set<String> smembers(String key) {
        return redisCommands.smembers(key);
    }

    // 从集合里面随机地返回一个或多个元素。当count为正数时，命令返回的随机元素不会重复；当count为负数时，命令返回的随机元素可能会出现重复
    public List<String> srandmember(String key, int count) {
        return redisCommands.srandmember(key, count);
    }

    // 随机地移除集合中指定数量的元素，并返回被移除的元素
    public Set<String> spop(String key, int count) {
        return redisCommands.spop(key, count);
    }

    // 如果集合srcKey包含元素item，那么从集合srcKey里面移除元素item，并将元素item添加到集合destKey中；如果item被成功移除，返回true，否则false
    public Boolean smove(String srcKey, String destKey, String item) {
        return redisCommands.smove(srcKey, destKey, item);
    }

    // 下面是针对多个集合的操作

    // 返回那些存在于第一个集合、但不存在于其他集合中的元素（差集）
    public Set<String> sdiff(String... keys) {
        return redisCommands.sdiff(keys);
    }

    // 将那些存在于第一个集合但并不存在于其他集合中的元素（差集）存储到destKey键里面
    public Long sdiffstore(String destKey, String... keys) {
        return redisCommands.sdiffstore(destKey, keys);
    }

    // 返回那些同时存在于所有集合中的元素（交集）
    public Set<String> sinter(String... keys) {
        return redisCommands.sinter(keys);
    }

    // 同时存在于所有集合中的元素（交集）存储到destKey键里面
    public Long sinterstore(String destKey, String... keys) {
        return redisCommands.sinterstore(destKey, keys);
    }

    // 返回那些至少存在于一个集合中的元素（并集）
    public Set<String> sunion(String... keys) {
        return redisCommands.sunion(keys);
    }

    // 至少存在于一个集合中的元素（并集）存储到destKey键里面
    public Long sunionstore(String destKey, String... keys) {
        return redisCommands.sunionstore(destKey, keys);
    }

    /*
    ----------------------------------------------------------
    hash
    ----------------------------------------------------------
     */
    public String hget(String key, String field) {
        return redisCommands.hget(key, field);
    }

    // 为散列里面的一个或多个键设置值
    public void hset(String key, String field, String value) {
        redisCommands.hset(key, field, value);
    }

    // 为散列里面的一个或多个键设置值
    public void hmset(String key, Map<String, String> hash) {
        redisCommands.hmset(key, hash);
    }

    // 删除指定的键值对，返回成功删除的键值对数量
    public Long hdel(String key, String... fields) {
        return redisCommands.hdel(key, fields);
    }

    // 返回键值对数量
    public Long hlen(String key) {
        return redisCommands.hlen(key);
    }

    // 判断是否存在指定键值对
    public Boolean hexists(String key, String field) {
        return redisCommands.hexists(key, field);
    }

    // 返回所有键值对的键
    public List<String> hkeys(String key) {
        return redisCommands.hkeys(key);
    }

    // 返回所有键值对的值
    public List<String> hvals(String key) {
        return redisCommands.hvals(key);
    }

    // 返回所有键值对
    public Map<String, String> hgetAll(String key) {
        return redisCommands.hgetall(key);
    }

    // 将键key存储的值加上整数incr
    public Long hincrBy(String key, String field, long incr) {
        return redisCommands.hincrby(key, field, incr);
    }

    // 将键key存储的值加上浮点数value
    public Double hincrByFloat(String key, String field, double value) {
        return redisCommands.hincrbyfloat(key, field, value);
    }

    /*
    ----------------------------------------------------------
    zset
    有序集合
    ----------------------------------------------------------
     */
    public void zadd(String key, double score, String member) {
        redisCommands.zadd(key, score, member);
    }

    public void zrem(String key, String... members) {
        redisCommands.zrem(key, members);
    }

    // 返回有序集合元素数量
    public Long zcard(String key) {
        return redisCommands.zcard(key);
    }

    // 将指定元素的值加上incr
    public Double zincrby(String key, double incr, String member) {
        return redisCommands.zincrby(key, incr, member);
    }

    // 返回分值在min到max之间的元素个数
    public Long zcount(String key, double min, double max) {
        return redisCommands.zcount(key, Range.create(min, max));
    }

    // 返回分值在min到max之间的元素
    public List<String> zrangeByScore(String key, double min, double max) {
        return redisCommands.zrangebyscore(key, Range.create(min, max));
    }

    // 返回指定元素的排名
    public Long zrank(String key, String member) {
        return redisCommands.zrank(key, member);
    }

    // 返回指定元素的分值
    public Double zscore(String key, String member) {
        return redisCommands.zscore(key, member);
    }

    // 从小到大返回指定排名之间的元素
    public List<String> zrange(String key, long start, long stop) {
        return redisCommands.zrange(key, start, stop);
    }

    // 返回指定排名之间的元素和分值
    public List<ScoredValue<String>> zrangeWithScores(String key, long start, long stop) {
        return redisCommands.zrangeWithScores(key, start, stop);
    }

    // 按从大到小的顺序，返回指定元素的排名
    public Long zrevrank(String key, String member) {
        return redisCommands.zrevrank(key, member);
    }

    // 从大到小返回指定排名之间的元素
    public List<String> zrevrange(String key, long start, long stop) {
        return redisCommands.zrevrange(key, start, stop);
    }

    // 从大到小返回指定排名之间的元素和分值
    public List<ScoredValue<String>> zrevrangeWithScores(String key, long start, long stop) {
        return redisCommands.zrevrangeWithScores(key, start, stop);
    }

    // 从大到小返回指定分值之间的元素
    public List<String> zrevrangeByScore(String key, double min, double max) {
        return redisCommands.zrevrangebyscore(key, Range.create(min, max));
    }

    // 删除指定排名之间的元素
    public Long zremrangeByRank(String key, long start, long stop) {
        return redisCommands.zremrangebyrank(key, start, stop);
    }

    // 删除指定分值之间的元素
    public Long zremrangeByScore(String key, double min, double max) {
        return redisCommands.zremrangebyscore(key, min, max);
    }

    // 对给定的有序集合的交集执行指定的操作（MAX、MIN、SUM），结果保存到destKey
    public Long zinterstore(String destKey, ZStoreArgs storeArgs, String... keys) {
        return redisCommands.zinterstore(destKey, storeArgs, keys);
    }

    // 对给定的有序集合的并集执行指定的操作（MAX、MIN、SUM），结果保存到destKey
    public Long zunionstore(String destKey, String... keys) {
        return redisCommands.zunionstore(destKey, keys);
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
