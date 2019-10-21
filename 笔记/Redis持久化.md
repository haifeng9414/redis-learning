Redis的持久化有两个方式：
- 快照：将存在于某一时刻的所有数据都写入硬盘里面
  
  快照相关选项：
  - dbfilename：控制创建的快照文件名
  - dir：控制快照文件存储路径
  - 

  发起快照的方式：
  - 向Redis发送BGSAVE命令，Redis将开启子线程创建快照
  - 向Redis发送SAVE命令，Redis在快照创建完成前不会响应其他命令
  - 如果用户设置了save配置选项，比如save 60 10000，那么从Redis最近一次创建快照之后开始算起，当“60秒之内有10000次写入”这个条件被满足时，Redis就会自动触发BGSAVE命令。
  - 当Redis通过SHUTDOWN命令接收到关闭服务器的请求时，或者接收到标准TERM信号时，会执行一个SAVE命令，并在执行完毕后关闭服务器
  - 当一个Redis服务器连接另一个Redis服务器，并向对方发送SYNC命令来开始一次复制操作的时候，如果主服务器目前没有在执行BGSAVE操作，或者主服务器并非刚刚执行完BGSAVE操作，那么主服务器就会执行BGSAVE命令

  快照的持久化方式缺点是最后一次快照后到系统奔溃时之间的数据将会丢失，而且如果Redis的数据库很大，则BGSAVE命令会执行很长时间，可能也会导致Redis停顿

- 只追加文件（AOF）：在执行写命令时，将被执行的写命令复制到硬盘里面
  
  AOF相关选项：
  - appendonly：是否开启AOF
  - appendfsync：影响AOF文件的同步频率，由于AOF是将命令写入文件，而写入文件操作再调用file.write()方法时，写入的内容首先会被存储到缓冲区，然后操作系统会在将来的某个时候将缓冲区存储的内容写入硬盘，而数据只有在被写入硬盘之后，才算是真正地保存到了硬盘里面，用户可以通过调用file.flush()方法来请求操作系统尽快地将缓冲区存储的数据写入硬盘里，但具体何时执行写入操作仍然由操作系统决定。除此之外，用户还可以命令操作系统将文件同步（sync）到硬盘，同步操作会一直阻塞直到指定的文件被写入硬盘为止，appendfsync选项可以控制合适执行sync
    - always：每个Redis写命令都要同步写入硬盘。这样做会严重降低Redis的速度
    - everysec：每秒执行一次同步，显式地将多个写命令同步到硬盘
    - no：让操作系统来决定应该何时进行同步，这个选项在一般情况下不会对Redis的性能带来影响，但系统崩溃将导致使用这种选项的Redis服务器丢失不定数量的数据。另外，如果用户的硬盘处理写入操作的速度不够快的话，那么当缓冲区被等待写入硬盘的
    数据填满时，Redis的写入操作将被阻塞，并导致Redis处理命令请求的速度变慢。因为这个原因，一般来说并不推荐使用appendfsync no选项
  - auto-aof-rewrite-percentage：配置执行BGREWRITEAOF的时机
  - auto-aof-rewrite-min-size：配置执行BGREWRITEAOF的时机

  随着Redis的允许，AOF文件的体积也会越来越大，为了解决 AOF 文件体积不断增大的问题，用户可以向Redis发送BGREWRITEAOF命令，这
  个命令会通过移除AOF文件中的冗余命令来重写（rewrite）AOF文件，使AOF文件的体积变得尽可能地小。BGREWRITEAOF的工作原理和BGSAVE创建快照的工作原理非常相似：Redis 会创建一个子进程，然后由子进程负责对 AOF 文件进行重写。

  跟快照持久化可以通过设置save选项来自动执行BGSAVE一样，AOF持久化也可以通过设置auto-aof-rewrite-percentage选项和auto-aof-rewrite-min-size 选项来自动执行BGREWRITEAOF。假设用户对Redis设置了配置选项auto-aof-rewrite-percentage
  100和auto-aof-rewrite-min-size 64mb，并且启用了AOF持久化，那么当AOF文件的体积大于64 MB，并且AOF文件的体积比上一次重写之后的体积大了至少一倍（100%）的时候，Redis将执行BGREWRITEAOF命令。
