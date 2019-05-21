package com.taotao.miaosha.redis;

public abstract class BasePrefix implements KeyPrefix{
	
	private int expireSeconds;
	
	private String prefix;
	
	public BasePrefix(String prefix) {//0代表永不过期
		this(0, prefix);
	}
	
	public BasePrefix( int expireSeconds, String prefix) {
		this.expireSeconds = expireSeconds;
		this.prefix = prefix;
	}
	
	public int expireSeconds() {//默认0代表永不过期
		return expireSeconds;
	}
 
	public String getPrefix() {
		String className = getClass().getSimpleName();//利用反射获取子类的名称
		return className+":" + prefix;//使用类名+前缀（id）拼接
	}
 
}
