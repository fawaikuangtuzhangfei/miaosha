package com.imocc.miaosha.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imocc.miaosha.dao.OrderDao;
import com.imocc.miaosha.domain.MiaoshaOrder;
import com.imocc.miaosha.domain.MiaoshaUser;
import com.imocc.miaosha.domain.OrderInfo;
import com.imocc.miaosha.redis.OrderKey;
import com.imocc.miaosha.vo.GoodsVo;

/**
 * 订单
 * @author nanshoudabaojian
 *
 */
@Service
public class OrderService {
	
	@Autowired
	OrderDao orderDao;

	@Autowired
	RedisService redisService;
	
	//通过用户id和商品id来得知 该用户是否已经秒杀到了 防止重复秒杀
	//第5天的优化：将订单信息写入到了缓存中 直接读取缓存 减少与数据库的交互
	public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(Long userId, Long goodsId) {
//		return orderDao.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
		return redisService.get(OrderKey.getMiaoshaOrderByUidGid, "" + userId + "_" + goodsId, MiaoshaOrder.class);
	}

	//生成订单 写miaosha_order miaosha_order_info
	@Transactional
	public OrderInfo createOrder(MiaoshaUser user, GoodsVo goods) {
		OrderInfo orderInfo = new OrderInfo();
		orderInfo.setCreateDate(new Date());
		orderInfo.setDeliveryAddrId(0L);
		orderInfo.setGoodsCount(1);
		orderInfo.setGoodsId(goods.getId());
		orderInfo.setGoodsName(goods.getGoodsName());
		orderInfo.setGoodsPrice(goods.getMiaoshaPrice());
		orderInfo.setOrderChannel(1);
		orderInfo.setStatus(0);
		orderInfo.setUserId(user.getId());
		orderDao.insert(orderInfo);
		//建立唯一索引 防止同时发送两个请求 致使一个人重复下单
		MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
		miaoshaOrder.setGoodsId(goods.getId());
		miaoshaOrder.setOrderId(orderInfo.getId());
		miaoshaOrder.setUserId(user.getId());
		
		orderDao.insertMiaoshaOrder(miaoshaOrder);
		//将订单存入缓存
		redisService.set(OrderKey.getMiaoshaOrderByUidGid, "" + user.getId() + "_" + goods.getId(), miaoshaOrder);

		return orderInfo;
	}

	public OrderInfo getOrderById(long orderId) {
		return orderDao.getOrderById(orderId);
	}
	

}
