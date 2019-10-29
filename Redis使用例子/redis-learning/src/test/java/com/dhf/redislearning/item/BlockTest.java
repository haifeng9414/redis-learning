package com.dhf.redislearning.item;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BlockTest {
    private final static String KEY = "a";
    @Resource
    private RedisClient redisClient;

    @After
    public void deleteAllKey() {
        final RedisCommands<String, String> sync = redisClient.connect().sync();
        final List<String> keys = sync.keys("*");
        if (!CollectionUtils.isEmpty(keys)) {
            sync.del(keys.toArray(new String[0]));
        }
    }

    @Test
    public void syncBlockTest() throws InterruptedException {
        final StatefulRedisConnection<String, String> connect = redisClient.connect();
        final RedisCommands<String, String> sync = connect.sync();


        new Thread(() -> {
            System.out.println("start block");
            sync.blpop(5, KEY);
        }).start();

        Thread.sleep(1000);
        sync.set("a", "1");
        System.out.println(sync.get("a"));
    }

    @Test
    public void asyncBlockTest() throws InterruptedException, ExecutionException {
        final StatefulRedisConnection<String, String> connect = redisClient.connect();
        // 即使使用异步Command，下面的set命令的执行也需要等待blpop命令返回，而async.get("a").get()又需要等待set命令，导致了阻塞
        final RedisAsyncCommands<String, String> async = connect.async();

        new Thread(() -> {
            System.out.println("start block");
            async.blpop(5, KEY);
        }).start();

        Thread.sleep(1000);
        async.set("a", "1");
        System.out.println(async.get("a").get());
    }

    @Test
    public void multiClientBlockTest() throws InterruptedException, ExecutionException {
        // 调用两次redisClient.connect()，创建两个Command对象分别执行命令不会导致阻塞
        final RedisCommands<String, String> sync = redisClient.connect().sync();
        final RedisAsyncCommands<String, String> async = redisClient.connect().async();

        final Thread thread = new Thread(() -> {
            System.out.println("start block");
            System.out.println(sync.blpop(5, KEY));
        });
        thread.start();

        Thread.sleep(1000);
        async.set("a", "1");
        System.out.println(async.get("a").get());
        thread.join();
    }
}
