package com.dhf.redislearning.item;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AllKindOfBaseCommandTests {
    private final static String KEY = "a";
    @Autowired
    private AllKindOfBaseCommand allKindOfBaseCommand;

    @Before
    public void before() {
        allKindOfBaseCommand.del(KEY);
    }

    @After
    public void after() {
        allKindOfBaseCommand.del(KEY);
    }

    /*
    ----------------------------------------------------------
    string
    ----------------------------------------------------------
     */
    @Test
    public void string() {
        allKindOfBaseCommand.set(KEY, "1");
        String value = allKindOfBaseCommand.get(KEY);
        assertEquals("1", value);

        allKindOfBaseCommand.del(KEY);
        value = allKindOfBaseCommand.get(KEY);
        assertNull(value);
    }

    /*
    ----------------------------------------------------------
    list
    ----------------------------------------------------------
     */
    @Test
    public void list() {
        allKindOfBaseCommand.rpush(KEY, "1", "2", "3");
        List<String> values = allKindOfBaseCommand.lrange(KEY, 0, -1);
        assertEquals(3, values.size());

        String indexValue = allKindOfBaseCommand.lindex(KEY, 1);
        assertEquals("2", indexValue);

        allKindOfBaseCommand.lpush(KEY, "0");
        values = allKindOfBaseCommand.lrange(KEY, 0, -1);
        assertEquals(4, values.size());
        assertEquals(Arrays.toString(new String[]{"0", "1", "2", "3"}), Arrays.toString(values.toArray(new String[0])));

        String rpop = allKindOfBaseCommand.rpop(KEY);
        assertEquals("3", rpop);

        String lpop = allKindOfBaseCommand.lpop(KEY);
        assertEquals("0", lpop);

        values = allKindOfBaseCommand.lrange(KEY, 0, -1);
        assertEquals(2, values.size());
    }

    /*
    ----------------------------------------------------------
    set
    ----------------------------------------------------------
     */
    @Test
    public void set() {
        allKindOfBaseCommand.sadd(KEY, "1", "2", "3");
        Set<String> values = allKindOfBaseCommand.smembers(KEY);
        assertEquals(3, values.size());

        boolean sismember = allKindOfBaseCommand.sismember(KEY, "1");
        Assert.assertTrue(sismember);
        sismember = allKindOfBaseCommand.sismember(KEY, "0");
        assertFalse(sismember);

        allKindOfBaseCommand.sadd(KEY, "0");
        values = allKindOfBaseCommand.smembers(KEY);
        assertEquals(4, values.size());

        allKindOfBaseCommand.srem(KEY, "1");
        values = allKindOfBaseCommand.smembers(KEY);
        assertEquals(3, values.size());
    }

    /*
    ----------------------------------------------------------
    hash
    ----------------------------------------------------------
     */
    @Test
    public void hash() {
        allKindOfBaseCommand.hset(KEY, "a", "1");
        allKindOfBaseCommand.hset(KEY, "b", "2");
        allKindOfBaseCommand.hset(KEY, "c", "3");
        Map<String, String> values = allKindOfBaseCommand.hgetAll(KEY);
        assertEquals(3, values.size());

        assertEquals("1", allKindOfBaseCommand.hget(KEY, "a"));

        allKindOfBaseCommand.hdel(KEY, "a");
        assertNull(allKindOfBaseCommand.hget(KEY, "a"));
    }

    /*
    ----------------------------------------------------------
    zset
    ----------------------------------------------------------
     */
    @Test
    public void zset() {
        allKindOfBaseCommand.zadd(KEY, 10, "a");
        allKindOfBaseCommand.zadd(KEY, 20, "b");
        allKindOfBaseCommand.zadd(KEY, 30, "c");
        Set<String> values = allKindOfBaseCommand.zrange(KEY, 0, -1);
        assertEquals(3, values.size());

        assertEquals(Arrays.toString(new String[]{"a", "b", "c"}), Arrays.toString(values.toArray(new String[0])));

        Set<String> rangeValues = allKindOfBaseCommand.zrange(KEY, 0, 1);
        assertEquals(Arrays.toString(new String[]{"a", "b"}), Arrays.toString(rangeValues.toArray(new String[0])));

        allKindOfBaseCommand.zrem(KEY, "a");
        values = allKindOfBaseCommand.zrange(KEY, 0, -1);
        assertEquals(2, values.size());

        allKindOfBaseCommand.zadd(KEY, 40, "d");
        allKindOfBaseCommand.zadd(KEY, 50, "e");
        Set<String> rangeValuesByScore = allKindOfBaseCommand.zrangeByScore(KEY, 10, 40);
        assertEquals(Arrays.toString(new String[]{"b", "c", "d"}), Arrays.toString(rangeValuesByScore.toArray(new String[0])));

        Set<ZSetOperations.TypedTuple<String>> tuples = allKindOfBaseCommand.zrangeWithScores(KEY, 0, -1);
        double collect = tuples.stream().mapToDouble(item -> Optional.ofNullable(item.getScore()).orElse(0.0)).sum();
        // b + c + d + e = 20 + 30 + 40 + 50
        assertEquals(140, (int) collect);
    }
}
