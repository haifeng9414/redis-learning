package com.dhf.redislearning.item;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SubscribeTest {
    private static final long startMillis = System.currentTimeMillis();
    // Redis要求发布和订阅操作有两个不同的client对象执行，所以这里注入redisClient用于创建连接
    @Resource
    private RedisClient redisClient;
    private ArrayList<String> messageContainer = new ArrayList<>();
    private CountDownLatch messageReceivedLatch = new CountDownLatch(1);
    private CountDownLatch publishLatch = new CountDownLatch(1);

    private static void log(String string, Object... args) {
        long millisSinceStart = System.currentTimeMillis() - startMillis;
        System.out.printf("%20s %6d %s\n", Thread.currentThread().getName(), millisSinceStart,
                String.format(string, args));
    }

    @Test
    public void test() throws InterruptedException {
        setupPublisher();
        RedisPubSubListener<String, String> redisPubSubListener = setupSubscriber();

        // publish away!
        publishLatch.countDown();

        messageReceivedLatch.await();
        log("Got message: %s", messageContainer.iterator().next());

        redisPubSubListener.unsubscribed("test", 1);
//        redisPubSubListener.punsubscribed("test*", 1);
    }

    private void setupPublisher() {
        new Thread(() -> {
            try {
                log("Waiting to publish");
                publishLatch.await();
                log("Ready to publish, waiting one sec");
                Thread.sleep(1000);
                log("publishing");
                try (StatefulRedisPubSubConnection<String, String> client = redisClient.connectPubSub()) {
                    client.async().publish("test", "This is a message");
                    log("published, closing publishing connection");
                }
                log("publishing connection closed");
            } catch (Exception e) {
                log(">>> OH NOES Pub, " + e.getMessage());
                // e.printStackTrace();
            }
        }, "publisherThread").start();
    }

    private RedisPubSubListener<String, String> setupSubscriber() {
        final RedisPubSubListener<String, String> redisPubSubListener = new RedisPubSubListener<String, String>() {
            @Override
            public void unsubscribed(String channel, long subscribedChannels) {
                log("onUnsubscribe");
            }

            @Override
            public void punsubscribed(String pattern, long subscribedChannels) {
                log("onPUnsubscribe:" + pattern);
            }

            @Override
            public void subscribed(String channel, long subscribedChannels) {
                log("onSubscribe");
            }

            @Override
            public void psubscribed(String pattern, long subscribedChannels) {
                log("onPSubscribe:" + pattern);
            }

            @Override
            public void message(String pattern, String channel, String message) {
                messageContainer.add(message);
                log("Message received");
                messageReceivedLatch.countDown();
            }

            @Override
            public void message(String channel, String message) {
                messageContainer.add(message);
                log("Message received");
                messageReceivedLatch.countDown();
            }
        };
        new Thread(() -> {
            try {
                log("subscribing");
                try (StatefulRedisPubSubConnection<String, String> client = redisClient.connectPubSub()) {
                    client.addListener(redisPubSubListener);
                    client.async().subscribe("test");
                    // 还可以用psubscribe方法传入正则实现订阅多个channel，但是需要注意psubscribe对应的jedisPubSub中的回调方法和消息接收
                    // 接收方法与普通的subscribe方法是分开的，psubscribe方法对应的取消订阅方法是punsubscribe而不是unsubscribe
//                    client.sync().psubscribe("tes*");
                    log("subscribe returned, closing down");
                }
            } catch (Exception e) {
                log(">>> OH NOES Sub - " + e.getMessage());
                // e.printStackTrace();
            }
        }, "subscriberThread").start();
        return redisPubSubListener;
    }
}