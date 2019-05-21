package com.taotao.miaosha.redis;

public interface KeyPrefix {
		
	public int expireSeconds();//返回过期时间
	
	public String getPrefix();//获取key
	
}
