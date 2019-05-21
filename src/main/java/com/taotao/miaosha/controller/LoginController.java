package com.taotao.miaosha.controller;


import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.taotao.miaosha.redis.RedisService;
import com.taotao.miaosha.result.Result;
import com.taotao.miaosha.service.MiaoshaUserService;
import com.taotao.miaosha.vo.LoginVo;

@Controller
@RequestMapping("/login")
public class LoginController {
	
	@Autowired
	RedisService redisService;
	@Autowired
	MiaoshaUserService userService;
	private static Logger log = LoggerFactory.getLogger(LoginController.class);
	
	@RequestMapping("/to_login")
	public String toLogin(){
		return "login";
	}

    @RequestMapping("/do_login")
    @ResponseBody
    //@Valid 开启参数校验
    public Result<Boolean> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
    	log.info(loginVo.toString());
    	//登录
    	userService.login(response,loginVo);
    	return Result.success(true);
    }
}
