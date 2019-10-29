/**
 * 实现的功能是：一个用户（卖家）可以将自己的商品按照给定的价格放到市场上进行销售，当另一个用户（买家）购买这个商品时，卖家就会收到钱。
 * 保存数据的结构是：
 * 'user:' + userId作为key，使用hash保存某个用户的信息
 * 'inventory:' + userId作为key，使用set保存某个用户所拥有的商品信息
 * 'market:'作为key，使用zset保存商品标识和商品价格，商品标识由商品Id + 商品所属用户Id组成，商品价格作为分值
 */
package com.dhf.redislearning.item._03_purchase;