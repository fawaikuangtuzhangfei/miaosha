package com.imocc.miaosha.redis;

public class GoodsKey extends BasePrefix{
	
	private GoodsKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}

	//60s的缓存时间
	public static GoodsKey getGoodsList = new GoodsKey(60,"goodsList");
	public static GoodsKey getGoodsDetail = new GoodsKey(60,"goodsDetail");
	//0 永不失效
	public static GoodsKey getMiaoshaGoodsStock = new GoodsKey(0,"goodsStock");
	
}
