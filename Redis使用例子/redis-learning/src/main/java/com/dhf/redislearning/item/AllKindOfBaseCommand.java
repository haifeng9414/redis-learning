package com.dhf.redislearning.item;

import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.query.SortQuery;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@SuppressWarnings("WeakerAccess")
public class AllKindOfBaseCommand {
    // RedisTemplate默认采用的是JDK的序列化策略，保存的key和value都是采用此策略序列化保存的，如果想要保存对象而不是字符串到Redis，则
    // 使用该类
    @Resource
    private RedisTemplate<String, Serializable> redisTemplate;

    // StringRedisTemplate默认采用的是String的序列化策略，保存的key和value都是采用此策略序列化保存的，下面为了简单期间，只是用stringRedisTemplate
    // 执行命令
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /*
    ----------------------------------------------------------
    common
    ----------------------------------------------------------
     */
    // 删除指定的key
    public Boolean del(String key) {
        return stringRedisTemplate.delete(key);
    }

    // 删除指定的key，返回被删除的key的个数
    public Long del(Collection<String> keys) {
        return stringRedisTemplate.delete(keys);
    }

    public Boolean exists(String key) {
        return stringRedisTemplate.execute((RedisCallback<Boolean>) connection -> connection.exists(key.getBytes()));
    }

    public Set<String> keys(String key) {
        return stringRedisTemplate.keys(key);
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
    SortQuery<String> sortQuery = SortQueryBuilder
                .sort("sort-input")
                .by("d-*")
                .order(SortParameters.Order.ASC) // 默认值
                .alphabetical(false) // 默认值
                .get("d-*")
                .get("#")
                .limit(0, 10) // 如果需要分页的话
                .build();
     stringRedisTemplate.sort(sortQuery, destKey);
     */
    public Long sort(SortQuery<String> sortQuery, String destKey) {
        return stringRedisTemplate.sort(sortQuery, destKey);
    }

    // 不存储结果则直接返回结果
    public List<String> sort(SortQuery<String> sortQuery) {
        return stringRedisTemplate.sort(sortQuery);
    }

    // 移除key过期时间，如果存在key并且已过期，返回true
    public Boolean persist(String key) {
        return stringRedisTemplate.persist(key);
    }

    // 查看key还有久过期
    public Long ttl(String key, TimeUnit timeUnit) {
        return stringRedisTemplate.getExpire(key, timeUnit);
    }

    // 设置key的过期时间，如果存在key并且设置成功则返回true
    public Boolean expire(String key, long timeout, TimeUnit timeUnit) {
        return stringRedisTemplate.expire(key, timeout, timeUnit);
    }

    // 设置key的过期时间，如果存在key并且设置成功则返回true
    public Boolean expireAt(String key, Date date) {
        return stringRedisTemplate.expireAt(key, date);
    }

    /*
    ----------------------------------------------------------
    string
    ----------------------------------------------------------
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, String value, long timeout, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    // 值加，不存在的key则值视为0
    public Long incr(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

    // 值减1
    public Long decr(String key) {
        return stringRedisTemplate.opsForValue().decrement(key);
    }

    // 值加increment
    public Long incrBy(String key, long increment) {
        return stringRedisTemplate.opsForValue().increment(key, increment);
    }

    // 值减increment
    public Long decrBy(String key, long decrement) {
        return stringRedisTemplate.opsForValue().decrement(key, decrement);
    }

    // 值加increment，针对浮点值，Redis2.6以上有效
    public Double incrByFloat(String key, double increment) {
        return stringRedisTemplate.opsForValue().increment(key, increment);
    }

    // value追加到指定key的值后，返回追加后的值长度，不存在key则报错
    public Integer append(String key, String value) {
        return stringRedisTemplate.opsForValue().append(key, value);
    }

    // 获取指定范围内的值，包括start和end
    public String getrange(String key, long start, long end) {
        return stringRedisTemplate.opsForValue().get(key, start, end);
    }

    // 设置指定范围内的值
    public void setrange(String key, long offset, String value) {
        stringRedisTemplate.opsForValue().set(key, value, offset);
    }

    // 将字节串看作是二进制位串，并返回位串中偏移量为offset的二进制位的值
    // 如果原值为字符串1，其在Redis中保存为Ascii形式，即真实的值为1对应的Ascii码：00110001，getbit将会针对该值返回结果
    public Boolean getbit(String key, long offset) {
        return stringRedisTemplate.opsForValue().getBit(key, offset);
    }

    // 设置指定的key的offset位置的二进制值，同getbit一样，也是针对Ascii形式进行操作
    public Boolean setbit(String key, long offset, boolean value) {
        return stringRedisTemplate.opsForValue().setBit(key, offset, value);
    }

    // 统计二进制位串里面值为1的二进制位的数量，如果给定了可选的start偏移量和end偏移量，那么只对偏移量指定范围内的二进制位进行统计
    public Long bitcount(String key) {
        return stringRedisTemplate.execute((RedisCallback<Long>) connection -> connection.bitCount(key.getBytes()));
    }

    // 对一个或多个二进制位串执行包括并（AND）、或（OR）、异或（XOR）、非（NOT）在内的任意一种按位运算操作，并将结果保存在destKey键里
    public Long bitop(RedisStringCommands.BitOperation op, String destKey, String... keys) {
        return stringRedisTemplate.execute((RedisCallback<Long>) connection -> connection.bitOp(op, destKey.getBytes(), Arrays.stream(keys).map(String::getBytes).toArray(byte[][]::new)));
    }

    /*
    ----------------------------------------------------------
    list
    相当于链表
    ----------------------------------------------------------
     */
    // 返回链表的长度
    public Long lpush(String key, String... values) {
        return stringRedisTemplate.opsForList().leftPushAll(key, values);
    }

    // 返回链表的长度
    public Long rpush(String key, String... values) {
        return stringRedisTemplate.opsForList().rightPushAll(key, values);
    }

    public String lpop(String key) {
        return stringRedisTemplate.opsForList().leftPop(key);
    }

    public String rpop(String key) {
        return stringRedisTemplate.opsForList().rightPop(key);
    }

    // 返回列表中偏移量为offset的元素
    public String lindex(String key, long offset) {
        return stringRedisTemplate.opsForList().index(key, offset);
    }

    // 返回列表从start偏移量到end偏移量范围内的所有元素，包括start和end
    public List<String> lrange(String key, long start, long end) {
        return stringRedisTemplate.opsForList().range(key, start, end);
    }

    // 对列表进行修剪，只保留从start偏移量到end偏移量范围内的元素，，包括start和end
    public void ltrim(String key, long start, long end) {
        stringRedisTemplate.opsForList().trim(key, start, end);
    }

    // 从第一个非空列表中弹出位于最左端的元素，或者在timeout秒之内阻塞并等待可弹出的元素出现
    public List<String> blpop(int timeout, String... keys) {
        List<byte[]> result = stringRedisTemplate.execute((RedisCallback<List<byte[]>>) connection -> connection.bLPop(timeout, Arrays.stream(keys).map(String::getBytes).toArray(byte[][]::new)));
        if (!CollectionUtils.isEmpty(result)) {
            return result.stream().map(String::new).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    // 从第一个非空列表中弹出位于最右端的元素，或者在timeout秒之内阻塞并等待可弹出的元素出现
    public List<String> brpop(int timeout, String... keys) {
        List<byte[]> result = stringRedisTemplate.execute((RedisCallback<List<byte[]>>) connection -> connection.bRPop(timeout, Arrays.stream(keys).map(String::getBytes).toArray(byte[][]::new)));
        if (!CollectionUtils.isEmpty(result)) {
            return result.stream().map(String::new).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    // 从srcKey列表中弹出位于最右端的元素，然后将这个元素推入destKey列表的最左端，并向用户返回这个元素
    public String rpoplpush(String srcKey, String destKey) {
        byte[] result = stringRedisTemplate.execute((RedisCallback<byte[]>) connection -> connection.rPopLPush(srcKey.getBytes(), destKey.getBytes()));
        if (result != null) {
            return new String(result);
        } else {
            return null;
        }
    }

    // 从srcKey列表中弹出位于最右端的元素，然后将这个元素推入destKey列表的最左端，并向用户返回这个元素，如果srcKey为空，那么在timeout秒之内阻塞并等待可弹出的元素出现
    public String brpoplpush(String srcKey, String destKey, int timeout) {
        byte[] result = stringRedisTemplate.execute((RedisCallback<byte[]>) connection -> connection.bRPopLPush(timeout, srcKey.getBytes(), destKey.getBytes()));
        if (result != null) {
            return new String(result);
        } else {
            return null;
        }
    }

    /*
    ----------------------------------------------------------
    set
    无序的存储各不相同的元素，多个集合之间还可以进行组合和关联
    ----------------------------------------------------------
     */
    // 将一个或多个元素添加到集合里面，并返回被添加元素当中原本并不存在于集合里面的元素数量
    public Long sadd(String key, String... members) {
        return stringRedisTemplate.opsForSet().add(key, members);
    }

    // 从集合里面移除一个或多个元素，并返回被移除元素的数量
    public Long srem(String key, String... members) {
        return stringRedisTemplate.opsForSet().remove(key, members);
    }

    public Boolean sismember(String key, String value) {
        return stringRedisTemplate.opsForSet().isMember(key, value);
    }

    // 返回集合元素的数量
    public Long scard(String key) {
        return stringRedisTemplate.opsForSet().size(key);
    }

    // 返回所有集合元素
    public Set<String> smembers(String key) {
        return stringRedisTemplate.opsForSet().members(key);
    }

    // 从集合里面随机地返回一个或多个元素。当count为正数时，命令返回的随机元素不会重复；当count为负数时，命令返回的随机元素可能会出现重复
    public List<String> srandmember(String key, int count) {
        return stringRedisTemplate.opsForSet().randomMembers(key, count);
    }

    // 随机地移除集合中指定数量的元素，并返回被移除的元素
    public List<String> spop(String key, int count) {
        return stringRedisTemplate.opsForSet().pop(key, count);
    }

    // 如果集合srcKey包含元素item，那么从集合srcKey里面移除元素item，并将元素item添加到集合destKey中；如果item被成功移除，返回true，否则false
    public Boolean smove(String srcKey, String destKey, String item) {
        return stringRedisTemplate.opsForSet().move(srcKey, item, destKey);
    }

    // 下面是针对多个集合的操作

    // 返回那些存在于第一个集合、但不存在于其他集合中的元素（差集）
    public Set<String> sdiff(String key, String... others) {
        return stringRedisTemplate.opsForSet().difference(key, Arrays.stream(others).collect(Collectors.toList()));
    }

    // 将那些存在于第一个集合但并不存在于其他集合中的元素（差集）存储到destKey键里面
    public Long sdiffstore(String key, String destKey, String... others) {
        return stringRedisTemplate.opsForSet().differenceAndStore(key, Arrays.stream(others).collect(Collectors.toList()), destKey);
    }

    // 返回那些同时存在于所有集合中的元素（交集）
    public Set<String> sinter(String key, String... others) {
        return stringRedisTemplate.opsForSet().intersect(key, Arrays.stream(others).collect(Collectors.toList()));
    }

    // 同时存在于所有集合中的元素（交集）存储到destKey键里面
    public Long sinterstore(String key, String destKey, String... others) {
        return stringRedisTemplate.opsForSet().intersectAndStore(key, Arrays.stream(others).collect(Collectors.toList()), destKey);
    }

    // 返回那些至少存在于一个集合中的元素（并集）
    public Set<String> sunion(String key, String... others) {
        return stringRedisTemplate.opsForSet().union(key, Arrays.stream(others).collect(Collectors.toList()));
    }

    // 至少存在于一个集合中的元素（并集）存储到destKey键里面
    public Long sunion(String key, String destKey, String... others) {
        return stringRedisTemplate.opsForSet().unionAndStore(key, Arrays.stream(others).collect(Collectors.toList()), destKey);
    }

    /*
    ----------------------------------------------------------
    hash
    ----------------------------------------------------------
     */
    public String hget(String key, String subKey) {
        return Optional.ofNullable(stringRedisTemplate.opsForHash().get(key, subKey)).map(Object::toString).orElse(null);
    }

    // 为散列里面的一个或多个键设置值
    public void hset(String key, String hashKey, String value) {
        stringRedisTemplate.opsForHash().put(key, hashKey, value);
    }

    // 为散列里面的一个或多个键设置值
    public void hmset(String key, Map<String, String> hash) {
        stringRedisTemplate.execute((RedisCallback<Void>) connection -> {
            HashMap<byte[], byte[]> hashes = new HashMap<>();
            hash.forEach((entryKey, entryValue) -> hashes.put(entryKey.getBytes(), Optional.ofNullable(entryValue).map(String::getBytes).orElse(new byte[0])));
            connection.hMSet(key.getBytes(), hashes);
            return null;
        });
    }

    // 删除指定的键值对，返回成功删除的键值对数量
    public Long hdel(String key, String... hashKeys) {
        return stringRedisTemplate.opsForHash().delete(key, Arrays.stream(hashKeys).map((item) -> (Object) item).toArray());
    }

    // 返回键值对数量
    public Long hlen(String key) {
        return stringRedisTemplate.opsForHash().size(key);
    }

    // 判断是否存在指定键值对
    public Boolean hexists(String key, String hashKey) {
        return stringRedisTemplate.opsForHash().hasKey(key, hashKey);
    }

    // 返回所有键值对的键
    public Set<String> hkeys(String key) {
        Set<Object> keys = stringRedisTemplate.opsForHash().keys(key);
        if (!CollectionUtils.isEmpty(keys)) {
            return keys.stream().map(Object::toString).collect(Collectors.toSet());
        } else {
            return null;
        }
    }

    // 返回所有键值对的值
    public List<String> hvals(String key) {
        List<Object> values = stringRedisTemplate.opsForHash().values(key);
        if (!CollectionUtils.isEmpty(values)) {
            return values.stream().map(Object::toString).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    // 返回所有键值对
    public Map<String, String> hgetAll(String key) {
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
        if (!CollectionUtils.isEmpty(entries)) {
            HashMap<String, String> result = new HashMap<>();
            entries.forEach((entryKey, entryValue) -> result.put(entryKey.toString(), Optional.ofNullable(entryValue).map(Object::toString).orElse(null)));
            return result;
        } else {
            return null;
        }
    }

    // 将键key存储的值加上整数increment
    public Long hincrBy(String key, String hashKey, Long increment) {
        return stringRedisTemplate.opsForHash().increment(key, hashKey, increment);
    }

    // 将键key存储的值加上浮点数value
    public Double hincrByFloat(String key, String hashKey, double value) {
        return stringRedisTemplate.opsForHash().increment(key, hashKey, value);
    }

    /*
    ----------------------------------------------------------
    zset
    有序集合
    ----------------------------------------------------------
     */
    public void zadd(String key, double score, String members) {
        stringRedisTemplate.opsForZSet().add(key, members, score);
    }

    public void zrem(String key, String... members) {
        stringRedisTemplate.opsForZSet().remove(key, Arrays.stream(members).map((item) -> (Object) item).toArray());
    }

    // 返回有序集合元素数量
    public Long zcard(String key) {
        return stringRedisTemplate.opsForZSet().size(key);
    }

    // 将指定元素的值加上increment
    public Double zincrby(String key, double increment, String member) {
        return stringRedisTemplate.opsForZSet().incrementScore(key, member, increment);
    }

    // 返回分值在min到max之间的元素个数
    public Long zcount(String key, double min, double max) {
        return stringRedisTemplate.opsForZSet().count(key, min, max);
    }

    // 返回分值在min到max之间的元素
    public Set<String> zrangeByScore(String key, double min, double max) {
        return stringRedisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    // 返回指定元素的排名
    public Long zrank(String key, String member) {
        return stringRedisTemplate.opsForZSet().rank(key, member);
    }

    // 返回指定元素的分值
    public Double zscore(String key, String member) {
        return stringRedisTemplate.opsForZSet().score(key, member);
    }

    // 从小到大返回指定排名之间的元素
    public Set<String> zrange(String key, long start, long stop) {
        return stringRedisTemplate.opsForZSet().range(key, start, stop);
    }

    // 返回指定排名之间的元素和分值
    public Set<ZSetOperations.TypedTuple<String>> zrangeWithScores(String key, long start, long stop) {
        return stringRedisTemplate.opsForZSet().rangeWithScores(key, start, stop);
    }

    // 按从大到小的顺序，返回指定元素的排名
    public Long zrevrank(String key, String member) {
        return stringRedisTemplate.opsForZSet().reverseRank(key, member);
    }

    // 从大到小返回指定排名之间的元素
    public Set<String> zrevrange(String key, long start, long stop) {
        return stringRedisTemplate.opsForZSet().reverseRange(key, start, stop);
    }

    // 从大到小返回指定排名之间的元素和分值
    public Set<ZSetOperations.TypedTuple<String>> zrevrangeWithScores(String key, long start, long stop) {
        return stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, start, stop);
    }

    // 从大到小返回指定分值之间的元素
    public Set<String> zrevrangeByScore(String key, double min, double max) {
        return stringRedisTemplate.opsForZSet().reverseRangeByScore(key, min, max);
    }

    // 删除指定排名之间的元素
    public Long zremrangeByRank(String key, long start, long stop) {
        return stringRedisTemplate.opsForZSet().removeRange(key, start, stop);
    }

    // 删除指定分值之间的元素
    public Long zremrangeByScore(String key, double min, double max) {
        return stringRedisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }

    // 对给定的有序集合的交集执行指定的操作（MAX、MIN、SUM），结果保存到destKey
    public Long zinterstore(String key, String destKey, RedisZSetCommands.Aggregate aggregate, RedisZSetCommands.Weights weights, String... others) {
        return stringRedisTemplate.opsForZSet().intersectAndStore(key, Arrays.stream(others).collect(Collectors.toList()), destKey, aggregate, weights);
    }

    // zinterstore的重载版本
    public Long zinterstore(String key, String destKey, RedisZSetCommands.Aggregate aggregate, String... others) {
        return stringRedisTemplate.opsForZSet().intersectAndStore(key, Arrays.stream(others).collect(Collectors.toList()), destKey, aggregate);
    }

    // 对给定的有序集合的并集执行指定的操作（MAX、MIN、SUM），结果保存到destKey
    public Long zunionstore(String key, String destKey, RedisZSetCommands.Aggregate aggregate, String... others) {
        return stringRedisTemplate.opsForZSet().unionAndStore(key, Arrays.stream(others).collect(Collectors.toList()), destKey, aggregate);
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
