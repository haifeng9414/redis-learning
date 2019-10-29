package com.dhf.redislearning.item;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class TransactionCommand {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 开启Redis的基本事务，这种事务可以让一个客户端在不被其他客户端打断的情况下执行多个命令。和关系数据库那种可以在执行的过程
     * 中进行回滚（rollback）的事务不同，在Redis里面，被MULTI命令和EXEC命令包围的所有命令会一个接一个地执行，直到所有命令都执行完毕为止。
     * 当一个事务执行完毕之后，Redis才会处理其他客户端的命令
     * 当Redis从一个客户端那里接收到MULTI命令时，Redis会将这个客户端之后发送的所有命令都放入到一个队列里面，直到这个客户端发送EXEC
     * 命令为止，然后Redis就会在不被打断的情况下，一个接一个地执行存储在队列里面的命令
     * Redis事务在客户端上面是由流水线实现的：对连接对象调用multi()方法将创建一个事务并执行多个命令，在一切正常的情况下，客户端会自动地使用MULTI和EXEC
     * 包裹起用户输入的多个命令。此外，为了减少Redis与客户端之间的通信往返次数，提升执行多个命令时的性能，客户端会存储起事务包含的多个命令，然后在事务执行时一次
     * 性地将所有命令都发送给Redis
     * 
     * 在Redis里面使用流水线除了为了使用事务，还有另一个目的：提高性能，在执行一连串命令时，使用流水线减少Redis与客户端之间的通信往返次数可以大幅降低客户端等待回复所需的
     * 时间
     */
    public void multi() {
        stringRedisTemplate.multi();
    }

    /**
     * 执行事务，和multi方法相对应
     */
    public void exec() {
        stringRedisTemplate.exec();
    }

    /**
     * 使用watch方法对键进行监视之后，在执行exec方法之前，如果有其他客户端抢先对任何被监视的键进行了替换、更新或删除等操作，
     * 那么当尝试执行exec方法时，事务将失败并返回一个错误（之后用户可以选择重试事务或者放弃事务）。
     * 为什么Redis没有像MySQL一样实现典型的加锁功能？因为加锁有可能会造成长时间的等待，所以 Redis 为了尽可能地减少客户端的
     * 等待时间，并不会在执行 WATCH 命令时对数据进行加锁。相反地，Redis 只会在数据已经被其他客户端抢先修改了的情况下，通知执
     * 行了WATCH 命令的客户端，这种做法被称为乐观锁（optimistic locking），而关系数据库实际执行的加锁操作则被称为悲观
     * 锁（pessimistic locking）。
     */
    public void watch(String... keys) {
        stringRedisTemplate.watch(Arrays.stream(keys).collect(Collectors.toList()));
    }

    /**
     * 取消所有键的watch
     */
    public void unwatch() {
        stringRedisTemplate.unwatch();
    }

    /**
     * 取消事务，清空缓存下来的事务块中的命令，如果正在使用watch方法监视某个(或某些) key，那么取消所有监视，等同于执行命令unwatch
     */
    public void discard() {
        stringRedisTemplate.discard();
    }
}
