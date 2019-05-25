package com.taotao.miaosha.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taotao.miaosha.dao.OrderDao;
import com.taotao.miaosha.domain.MiaoshaOrder;
import com.taotao.miaosha.domain.MiaoshaUser;
import com.taotao.miaosha.domain.OrderInfo;
import com.taotao.miaosha.redis.OrderKey;
import com.taotao.miaosha.redis.RedisService;
import com.taotao.miaosha.vo.GoodsVo;

@Service
public class OrderService {
	@Autowired
	OrderDao orderDao;

	@Autowired
	RedisService redisService;
	public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(long userId, long goodsId) {
		MiaoshaOrder miaoshaOrder=redisService.get(OrderKey.getMiaoshaOrderByUidGid, 
				""+userId+"_"+goodsId, MiaoshaOrder.class);
		if (miaoshaOrder!=null) {
			return miaoshaOrder;
		}
		return orderDao.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
	}

	@Transactional
	public OrderInfo createOrder(MiaoshaUser user, GoodsVo goods) {
		OrderInfo orderInfo = new OrderInfo();
		orderInfo.setCreateDate(new Date());
		orderInfo.setDeliveryAddrId(0L);
		orderInfo.setGoodsCount(1);
		orderInfo.setGoodsId(goods.getId());
		orderInfo.setGoodsName(goods.getGoodsName());
		orderInfo.setGoodsPrice(goods.getMiaoshaPrice());
		orderInfo.setOrderChannel(1);
		orderInfo.setStatus(0);
		orderInfo.setUserId(user.getId());
		long orderId = orderDao.insert(orderInfo);
		MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
		miaoshaOrder.setGoodsId(goods.getId());
		miaoshaOrder.setOrderId(orderId);
		miaoshaOrder.setUserId(user.getId());
		orderDao.insertMiaoshaOrder(miaoshaOrder);
		redisService.set(OrderKey.getMiaoshaOrderByUidGid, 
				""+user.getId()+"_"+goods.getId(), miaoshaOrder);
		return orderInfo;
	}

	public OrderInfo getOrderById(long orderId) {
		return orderDao.getOrderById(orderId);
	}

	public void deleteOrders() {
		orderDao.deleteOrders();
		orderDao.deleteMiaoshaOrders();
	}
	
}
