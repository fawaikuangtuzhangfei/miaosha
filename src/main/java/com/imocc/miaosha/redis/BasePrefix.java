package com.imocc.miaosha.redis;

/**
 * 模板模式中的抽象类
 * @author nanshoudabaojian
 *
 */

public abstract class BasePrefix implements KeyPrefix {
	
	//过期时间
	private int expireSeconds;
	//前缀
	private String prefix;
	
	public BasePrefix(String prefix){
		//默认为0 永不过期
		this(0, prefix);
	}

	public BasePrefix(int expireSeconds, String prefix){
		this.expireSeconds = expireSeconds;
		this.prefix = prefix;
	}
	
	public int expireSeconds() { //默认0 永不过期
		return expireSeconds;
	}

	public String getPrefix() {
		String className = getClass().getSimpleName();
		return className + ":" + prefix;
	}

}
