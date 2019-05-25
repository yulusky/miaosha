package com.taotao.miaosha.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.taotao.miaosha.domain.MiaoshaOrder;
import com.taotao.miaosha.domain.MiaoshaUser;
import com.taotao.miaosha.domain.OrderInfo;
import com.taotao.miaosha.rabbitmq.MQSender;
import com.taotao.miaosha.rabbitmq.MiaoshaMessage;
import com.taotao.miaosha.redis.GoodsKey;
import com.taotao.miaosha.redis.MiaoshaKey;
import com.taotao.miaosha.redis.OrderKey;
import com.taotao.miaosha.redis.RedisService;
import com.taotao.miaosha.result.CodeMsg;
import com.taotao.miaosha.result.Result;
import com.taotao.miaosha.service.GoodsService;
import com.taotao.miaosha.service.MiaoshaService;
import com.taotao.miaosha.service.OrderService;
import com.taotao.miaosha.vo.GoodsVo;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController {
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	MiaoshaService miaoshaService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	MQSender mqSender;
	
	//标志位，当该商品没有售罄时为false，售罄时为true
	private HashMap<Long, Boolean> localOverMap =  new HashMap<Long, Boolean>();
	
	/**
	 * 系统初始化
	 * 将所有商品的库存加载到redis
	 * */
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
	
	@RequestMapping(value="/reset", method=RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset(Model model) {
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		for(GoodsVo goods : goodsList) {
			goods.setStockCount(10);
			redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), 10);
			localOverMap.put(goods.getId(), false);
		}
		redisService.delete(OrderKey.getMiaoshaOrderByUidGid);
		redisService.delete(MiaoshaKey.isGoodsOver);
		miaoshaService.reset(goodsList);
		return Result.success(true);
	}
	
	@RequestMapping("/do_miaosha")
	@ResponseBody
	public Result<Integer> list(Model model,MiaoshaUser user,
			@RequestParam("goodsId")long goodsId) {
		if (user==null) {
			return Result.error(CodeMsg.SESSION_ERROR) ;		
		}
		//内存标记，减少redis访问
		boolean over = localOverMap.get(goodsId);
    	if(over) {
    		return Result.error(CodeMsg.MIAO_SHA_OVER);
    	}
		//redis预减库存
    	long stock=redisService.decr(GoodsKey.getMiaoshaGoodsStock, ""+goodsId);
		if (stock<0) {
    		localOverMap.put(goodsId, true);
			return Result.error(CodeMsg.MIAO_SHA_OVER);
		}
		//判断是否已经下单
		MiaoshaOrder order=orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
		if (order!=null) {
			return Result.error(CodeMsg.REPEATE_MIAOSHA);
		}
		//入队
		//减库存 下订单 写入秒杀订单
		MiaoshaMessage message=new MiaoshaMessage();
		message.setGoodsId(goodsId);
		message.setUser(user);
		mqSender.sendMiaoshaMessage(message);
		
        return Result.success(0);//状态：排队中
	}
    /**
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     * */
    @RequestMapping(value="/result", method=RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(Model model,MiaoshaUser user,
    		@RequestParam("goodsId")long goodsId) {
    	model.addAttribute("user", user);
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	long result  =miaoshaService.getMiaoshaResult(user.getId(), goodsId);
    	return Result.success(result);
    }
	//不使用缓存
//	@RequestMapping("/do_miaosha")
//	public String list(Model model,MiaoshaUser user,
//			@RequestParam("goodsId")long goodsId) {
//		model.addAttribute("user", user);
//		if (user==null) {
//			return "login";		
//		}
//		//判断库存
//		GoodsVo goods=goodService.getGoodsVoByGoodsId(goodsId);
//		if (goods.getStockCount()<=0) {
//			model.addAttribute("errmsg",CodeMsg.MIAO_SHA_OVER.getMsg());
//			return "miaosha_fail";
//		}
//		//判断是否已经下单
//		MiaoshaOrder order=orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
//		if (order!=null) {
//			model.addAttribute("errmsg",CodeMsg.REPEATE_MIAOSHA.getMsg());
//			return "miasha_fail";
//		}
//		//正式下单
//		//减库存 下订单 写入秒杀订单
//		OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
//    	model.addAttribute("orderInfo", orderInfo);
//    	model.addAttribute("goods", goods);
//        return "order_detail";
//	}
}
