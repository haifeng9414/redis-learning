redis的字典就是哈希表，和JDK7的HashMap实现差不多，数组加链表，redis字典定义如下：
```c
typedef struct dictht { // 哈希表
    dictEntry **table;      // 数组
    unsigned long size;     // **table数组长度
    unsigned long sizemask; // 用于将哈希值映射到table的位置索引，它的值总是等于size - 1。
    unsigned long used;     // 记录哈希表已有的节点（键值对）数量。
} dictht;

typedef struct dictEntry { // 哈希表的节点
    void *key;
    union { // 值
        void *val;
        uint64_t u64;
        int64_t s64;
    } v;
    struct dictEntry *next;// 指向下一个dictEntry，形成链表
} dictEntry;

typedef struct dictType { // 类型特定函数
    unsigned int (*hashFunction)(const void *key);      // 计算hash值的函数
    void *(*keyDup)(void *privdata, const void *key);   // 复制key的函数
    void *(*valDup)(void *privdata, const void *obj);   // 复制value的函数
    int (*keyCompare)(void *privdata, const void *key1, const void *key2);  // 比较key的函数
    void (*keyDestructor)(void *privdata, void *key);   // 销毁key的析构函数
    void (*valDestructor)(void *privdata, void *obj);   // 销毁val的析构函数
} dictType;

typedef struct dict { // 字典
    dictType *type;     // 指向dictType结构，dictType结构中包含自定义的函数，这些函数使得key和value能够存储任何类型的数据。
    void *privdata;     // 私有数据，保存着dictType结构中函数的参数。
    dictht ht[2];       // 两张哈希表，用于渐进式rehash
    long rehashidx;     // rehash的标记，rehashidx==-1，表示没在进行rehash
    int iterators;      // 正在迭代的迭代器数量
} dict;
```

### rehash
为了保持哈希表的负载因子在一定范围内，需要对哈希表执行扩容或所容操作，触发条件：
在每次向字典添加新键值对之前，都会对哈希表ht[0]进行检查，对于ht[0]的size和used属性，如果它们之间的比率ratio = used / size满足以下任何一个条件的话，rehash过程就会被激活：
- 自然rehash：ratio >= 1 ，且变量dict_can_resize为真（当redis使用子进程对数据库执行后台持久化任务时（比如执行BGSAVE或BGREWRITEAOF时），为了最大化地利用系统的copy on write机制， 程序会暂时将dict_can_resize设为假，避免执行自然rehash，从而减少程序对内存的触碰）
- 强制rehash：ratio大于变量dict_force_resize_ratio（默认为5）

字典的rehash操作过程如下：
- 为字典的ht[1]哈希表分配空间，这个哈希表的空间大小取决于要执行的操作，以及ht[0]当前包含的键值对数量（也即是ht[0].used属性的值）：
  - 如果执行的是扩展操作，那么ht[1]的大小为第一个大于等于ht[0].used * 2的2^n（2 的 n 次方幂）
  - 如果执行的是收缩操作，那么ht[1]的大小为第一个大于等于ht[0].used的2^n
- 将保存在ht[0]中的所有键值对rehash到ht[1]上面
- 当ht[0]包含的所有键值对都迁移到了ht[1]之后（ht[0]变为空表），释放ht[0]，将ht[1]设置为ht[0]，并在ht[1]新创建一个空白哈希表，为下一次rehash做准备

### 渐进式rehash
如果ht[0]里只保存着四个键值对，那么服务器可以在瞬间就将这些键值对全部rehash到ht[1]。但是，如果哈希表里保存的键值对数量非常多，那么要一次性将这些键值对全部rehash到ht[1]的话，庞大的计算量可能会导致服务器在一段时间内停止服务，所以rehash过程是分多次、渐进式的，具体过程如下：
- 为ht[1]分配空间，让字典同时持有ht[0]和ht[1]两个哈希表
- 在字典中维持一个索引计数器变量rehashidx，并将它的值设置为0，表示rehash工作正式开始
- 在rehash进行期间，每次对字典执行添加、删除、查找或者更新操作时，程序除了执行指定的操作以外，还会顺带将ht[0]哈希表在rehashidx索引上的所有键值对rehash到ht[1]，当rehash工作完成之后，程序将rehashidx属性的值增一
- 随着字典操作的不断执行，最终在某个时间点上，ht[0]的所有键值对都会被rehash至ht[1]，这时程序将rehashidx属性的值设为-1，表示rehash操作已完成