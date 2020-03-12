package com.imocc.miaosha.access;

import com.imocc.miaosha.domain.MiaoshaUser;

/**
 * ThreadLocal????
 * 多线程中线程安全的一种方式 与当前线程绑定的
 * 存入了当前线程
 * @author nanshoudabaojian
 *
 */
public class UserContext {
	
	private static ThreadLocal<MiaoshaUser> userHolder = new ThreadLocal<MiaoshaUser>();
	
	public static void setUser(MiaoshaUser user){
		userHolder.set(user);
	}
	
	public static MiaoshaUser getUser(){
		return userHolder.get();
	}
}
