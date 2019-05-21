package com.taotao.miaosha.controller;
 
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
 
import com.taotao.miaosha.domain.MiaoshaUser;;
 
@Controller
@RequestMapping("/goods")
public class GoodsController {
 
	@RequestMapping("/to_list")
	public String toList(Model model, MiaoshaUser miaoshaUser) {
		model.addAttribute("user", miaoshaUser);
		return "goods_list";
	}
	
}