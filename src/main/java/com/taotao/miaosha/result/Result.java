package com.taotao.miaosha.result;

public class Result<T> {
	private int code;
	private String msg;
	private T data;//这里data为泛型，因为可以返回很多种不同类型的对象
	private Result(T data) {
		this.code=0;
		this.msg="success";
		this.data=data;
	}
	private Result(CodeMsg codeMsg) {
		if (codeMsg!=null) {
			this.code=codeMsg.getCode();
			this.msg=codeMsg.getMsg();
			this.data=null;
		}
	}
	public static <T> Result<T> success(T data) {
		return new Result(data);
	}
	public static <T> Result<T> error(CodeMsg codeMsg) {
		return new Result(codeMsg);
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
	
}
