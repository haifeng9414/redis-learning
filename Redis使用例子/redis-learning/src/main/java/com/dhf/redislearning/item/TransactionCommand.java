package com.dhf.redislearning.item;

import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import javax.annotation.Resource;

@Service
public class TransactionCommand {
    @Resource
    private Jedis jedis;

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
     *
     * @return 返回事务对象，通过该对象能够执行所有普通的redis操作，如lpop、set、get等
     */
    public Transaction multi() {
        return jedis.multi();
    }

    /**
     * 执行事务，和multi方法相对应
     */
    public void exec(Transaction transaction) {
        transaction.exec();
    }
}
