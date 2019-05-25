package com.taotao.miaosha.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.taotao.miaosha.domain.User;
import com.taotao.miaosha.rabbitmq.MQSender;
import com.taotao.miaosha.redis.RedisService;
import com.taotao.miaosha.redis.UserKey;
import com.taotao.miaosha.result.CodeMsg;
import com.taotao.miaosha.result.Result;
import com.taotao.miaosha.service.UserService;

@Controller
public class DemoController {
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	MQSender mQSender;
	@RequestMapping("/")
	@ResponseBody
	public String index()
	{
		return "index";
	}
	@RequestMapping("/hello")
	@ResponseBody
	public Result<String> hello()
	{
		return Result.success("hello");
	}
	@RequestMapping("/helloError")
	@ResponseBody
	public Result<String> helloError()
	{
		return Result.error(CodeMsg.SERVER_ERROR);
	}
    @RequestMapping("/thymeleaf")
	public String thymeleaf(Model model){
		model.addAttribute("name", "Wings");
		return "hello";
	}
	@RequestMapping("/getUserById")
	@ResponseBody
	public Result<User> getById(){
		User user = userService.getUserById(1);
		return Result.success(user);
	}
	@RequestMapping("/tx")
	@ResponseBody
	@Transactional
	public Result<Integer> tx(){
		int result = userService.createUser(new User(8581, "Fiorina"));
		int result2 = userService.createUser(new User(8581, "Fiorina"));
		return Result.success(result2);
	}
	@RequestMapping("/redis/get")
	@ResponseBody
	public Result<User> redisGet(){
		User user=redisService.get(UserKey.getById, ""+123,User.class);
		return Result.success(user);
	}
	@RequestMapping("/redis/set")
	@ResponseBody
	public Result<Boolean> redisSet(){
		User user=new User(123, "yuxu");
		redisService.set(UserKey.getById, ""+123, user);
		return Result.success(true);
	}
	@RequestMapping("/mq")
	@ResponseBody
	public Result<Boolean> mq() {
		mQSender.send("Wings you're the hero,路飞是成为海贼王的男人！");
		return Result.success(true);
	}
}
