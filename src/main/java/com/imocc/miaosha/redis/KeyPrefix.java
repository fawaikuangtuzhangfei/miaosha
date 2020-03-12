package com.imocc.miaosha.redis;

/**
 * 模板模式
 * 接口
 * 抽象类
 * 实现类
 * 
 * 此处为接口
 * @author nanshoudabaojian
 *
 */
public interface KeyPrefix {
	
	public int expireSeconds();
	
	public String getPrefix();
}
