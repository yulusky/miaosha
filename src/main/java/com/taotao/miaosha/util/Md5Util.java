package com.taotao.miaosha.util;

import org.apache.commons.codec.digest.DigestUtils;

public class Md5Util {
	public static final String SALT="1a2b3c4d";//客户端和服务端共享的固定salt密码
	
	public static String md5(String src){
		return DigestUtils.md5Hex(src);
	}
	//第一次加密
	public static String inputPass2FormPass(String inputPass){
		String src = "" + SALT.charAt(0) + SALT.charAt(2)+ inputPass + SALT.charAt(5)+ SALT.charAt(4);
		return md5(src);
	}
	//第二次加密
	public static String formPass2DbPass(String formPass, String salt){
		//这里的salt取数据库的salt
		String src = "" + salt.charAt(0) + salt.charAt(2)+ formPass + salt.charAt(5)+ salt.charAt(4);
		return md5(src);
	}
	//将两次加密合并
	public static String inputPass2DbPass(String inputPass, String salt){
		String formPass = inputPass2FormPass(inputPass);
		String dbPass = formPass2DbPass(formPass, salt);
		return dbPass;
	}
	
	public static void main(String[] args) {
		String inputPass = "13632481101";
		String salt = "mysalt";
		String formPass = inputPass2FormPass(inputPass);
		String dbPass1 = formPass2DbPass(formPass, salt);
		String dbPass2 = inputPass2DbPass(inputPass, salt);
		System.out.println(formPass);
		System.out.println(dbPass1);
		System.out.println(dbPass2);
	}
}
