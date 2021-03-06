整数集合（intset）是redis的集合（set）底层实现之一，如果一个集合只包含整数，并且元素数量不多时，则redis会用整数集合作为set的实现，如：
```
127.0.0.1:6379> sadd numbers 1 2 3 4 5
(integer) 5
127.0.0.1:6379> OBJECT ENCODING numbers
"intset"
127.0.0.1:6379> 
```

整数集合可以保存类型为int16_t、int32_t或者int64_t的整数值，并且保证集合中不会出现重复元素，整数集合定义如下：
```
typedef struct intset {
    // 编码方式
    uint32_t encoding;
    // 集合包含的元素数量
    uint32_t length;
    // 保存元素的数组
    int8_t contents[];
} intset;
```

虽然intset结构将contents属性声明为int8_t类型的数组，但实际上contents数组并不保存任何int8_t类型的值，contents数组的真正类型取决于encoding属性的值：
- 如果encoding属性的值为INTSET_ENC_INT16，那么contents就是一个int16_t类型的数组，数组里的每个项都是一个int16_t类型的整数值（最小值为-32,768，最大值为 32,767）。
- 同上，如果encoding属性的值为INTSET_ENC_INT32，数组里的每个项都是一个int32_t类型的整数值（最小值为-2,147,483,648 ，最大值为2,147,483,647）。
- 同上，如果encoding属性的值为INTSET_ENC_INT64，数组里的每个项都是一个int64_t类型的整数值（最小值为-9,223,372,036,854,775,808，最大值为9,223,372,036,854,775,807）。

### 升级
每当我们要将一个新元素添加到整数集合里面，并且新元素的类型比整数集合现有所有元素的类型都要长时，整数集合需要先进行升级，然后才能将新元素添加到整数集合里面。

升级整数集合并添加新元素共分为三步进行：
- 根据新元素的类型，扩展整数集合底层数组的空间大小，并为新元素分配空间
- 将底层数组现有的所有元素都转换成与新元素相同的类型，并将类型转换后的元素放置到正确的位上，而且在放置元素的过程中，需要继续维持底层数组的有序性质不变
- 将新元素添加到底层数组里面

#### 举例：
现在有一个INTSET_ENC_INT16编码的整数集合，集合中包含三个int16_t类型的元素，因为每个元素都占用16位空间，所以整数集合底层数组的大小为3 * 16 = 48位。假设我们要将类型为int32_t的整数值65535添加到整数集合里面，因为65535的类型int32_t比整数集合当前所有元素的类型都要长，所以在将65535添加到整数集合之前，程序需要先对整数集合进行升级。

升级首先要做的是，根据新类型的长度，以及集合元素的数量（包括要添加的新元素在内），对底层数组进行空间重分配。整数集合目前有三个元素，再加上新元素65535，整数集合需要分配四个元素的空间，因为每个int32_t整数值需要占用32位空间，所以在空间重分配之后，底层数组的大小将是32 * 4 = 128位，之后将原先的三个元素转换成int32_t类型，并将转换后的元素放置到正确的位上面，而且在放置元素的过程中，需要维持底层数组的有序性质不变。最后，程序将整数集合encoding属性的值从INTSET_ENC_INT16改为INTSET_ENC_INT32 ，并将length属性的值从3改为4

#### 升级的好处
- 提升灵活性，用一个数据结构就能支持不同类型的整数
- 节约内存，只有在需要的时候才将数据结构改成必要的类型

### 降级
整数集合不支持降级操作，一旦对数组进行了升级，编码就会一直保持升级后的状态。
