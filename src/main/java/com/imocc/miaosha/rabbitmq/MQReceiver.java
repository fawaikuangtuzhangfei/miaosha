package com.imocc.miaosha.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.imocc.miaosha.domain.MiaoshaOrder;
import com.imocc.miaosha.domain.MiaoshaUser;
import com.imocc.miaosha.service.GoodsService;
import com.imocc.miaosha.service.MiaoshaService;
import com.imocc.miaosha.service.MiaoshaUserService;
import com.imocc.miaosha.service.OrderService;
import com.imocc.miaosha.service.RedisService;
import com.imocc.miaosha.vo.GoodsVo;

@Service
public class MQReceiver {
	
	private static Logger log = LoggerFactory.getLogger(MQReceiver.class);
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	MiaoshaUserService miaoshaUserService;
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	MiaoshaService miaoshaService;
	
	//接收秒杀队列传来的MiaoshaMessage对象 并进行秒杀操作
	@RabbitListener(queues=MQConfig.MIAOSHA_QUEUE)
	public void receiveMiaosha(String message){
		log.info("receive message:" + message);
		MiaoshaMessage mm = RedisService.stringToBean(message, MiaoshaMessage.class);
		MiaoshaUser user = mm.getUser();
		long goodsId = mm.getGoodsId();
		//判断库存
 		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
 		Integer stock = goods.getStockCount();
 		if(stock <= 0){
 			return;
 		}
 		//判断是否已经秒杀到了
 		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goods.getId());
 		if(order != null){
 			return;
 		}
 		//减库存 下订单 写入秒杀订单
 		miaoshaService.miaosha(user, goods);
	}
	
	@RabbitListener(queues=MQConfig.QUEUE)
	public void receive(String message){
		log.info("receive message:" + message);
	}
	
	@RabbitListener(queues=MQConfig.TOPIC_QUEUE1)
	public void receiveTopic1(String message){
		log.info("receive topic1 message:" + message);
	}
	
	@RabbitListener(queues=MQConfig.TOPIC_QUEUE2)
	public void receiveTopic2(String message){
		log.info("receive topic2 message:" + message);
	}
	
	@RabbitListener(queues=MQConfig.HEADER_QUEUE)
	public void receiveHeaders(byte[] message){
		log.info("receive headers message:" + new String(message));
	}
	
}
