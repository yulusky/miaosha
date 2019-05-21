package com.taotao.miaosha.exception;

import com.taotao.miaosha.result.CodeMsg;

public class GlobalException extends RuntimeException{
	//将错误信息：codeMsg包装起来
	//继承RuntimeException，产生GlobalException之后可以被拦截器拦截
	private static final long serialVersionUID = 31665074385012932L;
	private CodeMsg cm;
	
	public GlobalException(CodeMsg cm){
		this.cm = cm;
	}
	public CodeMsg getCm() {
		return cm;
	}
			
}
