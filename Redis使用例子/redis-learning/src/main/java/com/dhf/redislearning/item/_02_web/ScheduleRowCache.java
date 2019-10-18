package com.dhf.redislearning.item._02_web;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存数据行
 */
@Service
public class ScheduleRowCache {
    @Resource
    private Jedis jedis;

    /**
     * @param rowId 被缓存的数据行ID
     * @param delay 延迟值
     */
    public void scheduleRowCache(String rowId, double delay) {
        jedis.zadd("delay:", delay, rowId);
        // 被调度缓存的数据行立即开始更新缓存
        jedis.zadd("schedule:", System.currentTimeMillis(), rowId);
    }

    public void cacheRow() throws InterruptedException {
        while (true) {
            Set<Tuple> tuples = jedis.zrangeWithScores("schedule:", 0, 0);
            long now = System.currentTimeMillis();

            // 如果调用列表中最近的一个还没有到达调度时间，则sleep 50ms
            if (!CollectionUtils.isEmpty(tuples) && tuples.iterator().next().getScore() > now) {
                TimeUnit.MILLISECONDS.sleep(50);
                continue;
            }

            String rowId = tuples.iterator().next().getElement();
            Double delay = jedis.zscore("delay:", rowId);

            // 如果延迟为空或者小于等于0，则不再缓存数据行
            if (delay == null || delay <= 0) {
                jedis.zrem("schedule:", rowId);
                jedis.zrem("delay:", rowId);
                // 删除缓存的数据行
                jedis.del("data:" + rowId);
                continue;
            }

            String data = getData(rowId);
            // 更新调度时间
            jedis.zadd("schedule:", now + delay, rowId);
            // 缓存数据
            jedis.set("data:" + rowId, data);
        }
    }

    /**
     * 模拟获取数据行
     */
    public String getData(String rowId) {
        return rowId;
    }
}
