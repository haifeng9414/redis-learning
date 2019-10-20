package com.dhf.redislearning.item;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SubscribeTest {
    @Resource
    private Jedis jedis;
    // Redis要求发布和订阅操作有两个不同的client对象执行，所以这里注入jedisPool用于发布
    @Resource
    private JedisPool jedisPool;

	private ArrayList<String> messageContainer = new ArrayList<>();

	private CountDownLatch messageReceivedLatch = new CountDownLatch(1);
	private CountDownLatch publishLatch = new CountDownLatch(1);

	@Test
	public void test() throws InterruptedException {
        setupPublisher();
        JedisPubSub jedisPubSub = setupSubscriber();

        // publish away!
        publishLatch.countDown();

        messageReceivedLatch.await();
        log("Got message: %s", messageContainer.iterator().next());

        jedisPubSub.unsubscribe();
//        jedisPubSub.punsubscribe("test*");
	}

	private void setupPublisher() {
		new Thread(() -> {
            try {
                log("Waiting to publish");
                publishLatch.await();
                log("Ready to publish, waiting one sec");
                Thread.sleep(1000);
                log("publishing");
                Jedis jedis = jedisPool.getResource();
                jedis.publish("test", "This is a message");
                log("published, closing publishing connection");
                jedis.quit();
                log("publishing connection closed");
            } catch (Exception e) {
                log(">>> OH NOES Pub, " + e.getMessage());
                // e.printStackTrace();
            }
        }, "publisherThread").start();
	}

	private JedisPubSub setupSubscriber() {
		final JedisPubSub jedisPubSub = new JedisPubSub() {
			@Override
			public void onUnsubscribe(String channel, int subscribedChannels) {
				log("onUnsubscribe");
			}

			@Override
			public void onSubscribe(String channel, int subscribedChannels) {
				log("onSubscribe");
			}

			@Override
			public void onPUnsubscribe(String pattern, int subscribedChannels) {
			    log("onPUnsubscribe:" + pattern);
			}

			@Override
			public void onPSubscribe(String pattern, int subscribedChannels) {
                log("onPSubscribe:" + pattern);
            }

			@Override
			public void onPMessage(String pattern, String channel, String message) {
                messageContainer.add(message);
                log("Message received");
                messageReceivedLatch.countDown();
			}

			@Override
			public void onMessage(String channel, String message) {
				messageContainer.add(message);
				log("Message received");
				messageReceivedLatch.countDown();
			}
		};
		new Thread(() -> {
            try {
                log("subscribing");
                jedis.subscribe(jedisPubSub, "test");
                // 还可以用psubscribe方法传入正则实现订阅多个channel，但是需要注意psubscribe对应的jedisPubSub中的回调方法和消息接收
                // 接收方法与普通的subscribe方法是分开的，psubscribe方法对应的取消订阅方法是punsubscribe而不是unsubscribe
                // jedis.psubscribe(jedisPubSub, "tes*");
                log("subscribe returned, closing down");
                jedis.quit();
            } catch (Exception e) {
                log(">>> OH NOES Sub - " + e.getMessage());
                // e.printStackTrace();
            }
        }, "subscriberThread").start();
		return jedisPubSub;
	}

	private static final long startMillis = System.currentTimeMillis();

	private static void log(String string, Object... args) {
		long millisSinceStart = System.currentTimeMillis() - startMillis;
		System.out.printf("%20s %6d %s\n", Thread.currentThread().getName(), millisSinceStart,
				String.format(string, args));
	}
}