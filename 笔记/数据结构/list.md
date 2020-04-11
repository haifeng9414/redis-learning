redis中的链表是个双向链表，定义如下：
```c
typedef struce listNode {
    // 前置节点
    struct listNode *prev；
    // 后置节点
    struct listNode *next；
    // 节点的值
    void *value;  
}listNode;

typedef struct list {
    // 表头节点
    listNode.head;
    // 表尾节点
    listNode.tail;
    // 链表所包含的节点数量
    unsigned long len;
    // 节点值复制函数
    void *(*dup)(void *ptr);
    // 节点值释放函数
    void *(*free)(void *ptr);
    // 节点值对比函数，判断两个节点是否相等
    int (*match)(void *ptr,void *key);
} list;
```

redis的链表实现的特点：
- 双向链表
- 无环，表头节点的prev与表尾节点的next为null
- 带有链表长度计数器：len
- 多态，链表可以保存各种不同类型的值