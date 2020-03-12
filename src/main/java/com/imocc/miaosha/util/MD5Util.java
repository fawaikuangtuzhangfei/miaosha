package com.imocc.miaosha.util;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * 两次MD5：
 * 用户端：PASS=MD5(明文+固定salt)：防止明文密码在网络传输时被截取
 * 服务端：PASS=MD5(用户输入+随机salt)：防止数据库被盗
 * @author nanshoudabaojian
 *
 */
public class MD5Util {
	
	public static String md5(String src){
		return DigestUtils.md5Hex(src);
	}
	
	private static final String salt = "1a2b3c4d";
	
	//用户第一次输入在网络中加密
	public static String inputPassFormPass(String inputPass){
		String str = "" + salt.charAt(0) + salt.charAt(2) + inputPass + salt.charAt(5) + salt.charAt(4);
		return md5(str);
	}
	
	//存入数据库时加密 第二次md5
	public static String formPassToDBPass(String formPass, String salt){
		String str = salt.charAt(0) + salt.charAt(2) + formPass + salt.charAt(5) + salt.charAt(4);
		return md5(str);
	}
	
	//把表格中输入的转换成数据库的
	public static String inputPassToDBPass(String input, String saltDB){
		String formPass = inputPassFormPass(input);
		String dbPass = formPassToDBPass(formPass, saltDB);
		return dbPass;
	}
}
