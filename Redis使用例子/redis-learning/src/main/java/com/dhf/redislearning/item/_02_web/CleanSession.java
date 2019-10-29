package com.dhf.redislearning.item._02_web;

import com.dhf.redislearning.item.AllKindOfBaseCommand;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 实现清理用户记录功能
 * 暂时未考虑并发的问题
 */
@Service
public class CleanSession {
    @Resource
    private AllKindOfBaseCommand allKindOfBaseCommand;
    // 保存的用户记录数量最大值
    private final static int LIMIT =  1000;

    public void cleanSession() throws InterruptedException {
        while (true) {
            // zcard命令返回有序集合中元素的数量，这里获取最近登录的用户数量
            Long size = allKindOfBaseCommand.zcard("recent:");
            if (size <= 1000) {
                TimeUnit.SECONDS.sleep(1);
                continue;
            }

            // 最多减少到100
            long endIndex = Math.min(100, size - LIMIT);
            List<String> tokens = allKindOfBaseCommand.zrange("tokens", 0, endIndex - 1);

            List<String> deleteTokens = new ArrayList<>();
            for (String token : tokens) {
                // 删除用户浏览记录
                deleteTokens.add("viewed:" + token);
                // 删除用户购物车记录
                deleteTokens.add("cart:" + token);
            }

            allKindOfBaseCommand.del(deleteTokens.toArray(new String[0]));
            String[] tokenArray = tokens.toArray(new String[]{});
            allKindOfBaseCommand.hdel("login:", tokenArray);
            allKindOfBaseCommand.zrem("recent:", tokenArray);
        }
    }
}
