[TOC]

## 内容

**1.Redis预减库存减少数据库访问**
**2.内存标记减少Redis访问**
**3.请求先入队缓冲，异步下单，增强用户体验**
4.RabbitMQ安装与Spring Boot集成
5.Nginx水平扩展
6.压测

## 超卖问题

### 解决超卖

**1.数据库加唯一索引：防止用户重复购买**
**2.SQL加库存数量判断：防止库存变成负数**

## rabbitmq

```java
@Bean
public Queue queue() {
    return new Queue(QUEUE, true);
}

public void send(Object message) {
    String msg = RedisService.beanToString(message);
    log.info("send message:"+msg);
    amqpTemplate.convertAndSend(MQConfig.QUEUE, msg);
}

@RabbitListener(queues=MQConfig.QUEUE)
public void receive(String message) {
    log.info("receive message:"+message);
}
```

## 秒杀接口优化

思路：减少数据库访问
1.系统初始化，把商品库存数量加载到Redis

```java
public void afterPropertiesSet() throws Exception {
    List<GoodsVo> goodsList = goodsService.listGoodsVo();
    if(goodsList == null) {
        return;
    }
    for(GoodsVo goods : goodsList) {
        redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), goods.getStockCount());
        localOverMap.put(goods.getId(), false);
    }
}
```

2.收到请求，Redis预减库存，库存不足，直接返回，否则进入3

```java
@RequestMapping(value="/{path}/do_miaosha", method=RequestMethod.POST)
@ResponseBody
public Result<Integer> miaosha(Model model,MiaoshaUser user,
                               @RequestParam("goodsId")long goodsId,
                               @PathVariable("path") String path) {
    model.addAttribute("user", user);
    if(user == null) {
        return Result.error(CodeMsg.SESSION_ERROR);
    }
    //验证path
    boolean check = miaoshaService.checkPath(user, goodsId, path);
    if(!check){
        return Result.error(CodeMsg.REQUEST_ILLEGAL);
    }
    //内存标记，标记是否已经成功下单，减少redis访问
    boolean over = localOverMap.get(goodsId);
    if(over) {
        return Result.error(CodeMsg.MIAO_SHA_OVER);
    }
    //预减库存
    long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, ""+goodsId);//10
    if(stock < 0) {
        localOverMap.put(goodsId, true);
        return Result.error(CodeMsg.MIAO_SHA_OVER);
    }
    //判断是否已经秒杀到了
    MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
    if(order != null) {
        return Result.error(CodeMsg.REPEATE_MIAOSHA);
    }
    //入队
    MiaoshaMessage mm = new MiaoshaMessage();
    mm.setUser(user);
    mm.setGoodsId(goodsId);
    sender.sendMiaoshaMessage(mm);
    return Result.success(0);//排队中
    /*
    	//判断库存
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);//10个商品，req1 req2
    	int stock = goods.getStockCount();
    	if(stock <= 0) {
    		return Result.error(CodeMsg.MIAO_SHA_OVER);
    	}
    	//判断是否已经秒杀到了
    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
    	if(order != null) {
    		return Result.error(CodeMsg.REPEATE_MIAOSHA);
    	}
    	//减库存 下订单 写入秒杀订单
    	OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
        return Result.success(orderInfo);
        */
}
```

3.请求入队，立即返回排队中

4.请求出队，生成订单，减少库存。

```java
//从队列接受消息，准备下单
@RabbitListener(queues=MQConfig.MIAOSHA_QUEUE)
public void receive(String message) {
    log.info("receive message:"+message);
    MiaoshaMessage mm  = RedisService.stringToBean(message, MiaoshaMessage.class);
    MiaoshaUser user = mm.getUser();
    long goodsId = mm.getGoodsId();

    GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
    int stock = goods.getStockCount();
    if(stock <= 0) {
        return;
    }
    //判断是否已经秒杀到了
    MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
    if(order != null) {
        return;
    }
    //减库存 下订单 写入秒杀订单
    miaoshaService.miaosha(user, goods);
}
```

5.客户端轮询，是否秒杀成功

## nginx横向扩展配置

![nginx横向扩展配置.png](.\pic\nginx横向扩展配置.png)

## 软件安装

安装erlang

下载安装otp_src_20.1.tar.gz
./configure --prefix=/usr/local/erlang20 --without-javac
make -j 4
make install

安装RabbitMQ

安装python:yum install python-y
安装simplejson:yum install xmlto -y
yum install python-simplejson -y
解压

启动RabbitMQ

1./rabbitmq-server启动rabbitMQ server
2.netstat -nap l grep 5672

export PATH=$PATH:/usr/1ocal/ruby/bin:/usr/local/erlang20/bin:/usr/1ocal/rabbitmq/sbin

**解决guest用户不能远程登录rabbitmq的问题**

[{rabbit,[{loopback_users,[]}]}]. 

## 参考

