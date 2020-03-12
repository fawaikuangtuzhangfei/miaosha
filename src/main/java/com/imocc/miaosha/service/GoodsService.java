package com.imocc.miaosha.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.imocc.miaosha.dao.GoodsDao;
import com.imocc.miaosha.domain.MiaoshaGoods;
import com.imocc.miaosha.vo.GoodsVo;

/**
 * 秒杀用户的service层
 * 1.业务逻辑
 * 2.将用户的session存入redis中来实现分布式的session
 * @author nanshoudabaojian
 *
 */
@Service
public class GoodsService {
	
	
	@Autowired
	GoodsDao goodsDao;
	
	public List<GoodsVo> listGoodsVo(){
		return goodsDao.listGoodsVo();
	}

	public GoodsVo getGoodsVoByGoodsId(long goodsId) {
		return goodsDao.getGoodsVoByGoodsId(goodsId);
	}

	public boolean reduceStock(GoodsVo goods) {
		MiaoshaGoods g = new MiaoshaGoods();
		g.setGoodsId(goods.getId());
		int ret = goodsDao.reduceStock(g);
		//一个也没更新 代表着库存已经没了 秒杀完了
		return ret>0;
	}

}
