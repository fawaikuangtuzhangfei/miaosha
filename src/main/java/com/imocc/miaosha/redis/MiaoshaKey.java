package com.imocc.miaosha.redis;

public class MiaoshaKey extends BasePrefix{
	
	private MiaoshaKey(String prefix) {
		super(prefix);
	}
	
	private MiaoshaKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}

	public static MiaoshaKey isGoodsOver = new MiaoshaKey("isGoodsOver ");
	//获取秒杀地址
	public static MiaoshaKey getMiaoshaPath = new MiaoshaKey(60, "miaoshaPath ");
	//获取验证码
	public static MiaoshaKey getMiaoshaVerifyCode = new MiaoshaKey(300, "miaoshaVerifyCode ");

}
