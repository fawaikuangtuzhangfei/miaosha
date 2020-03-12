package com.imocc.miaosha.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imocc.miaosha.domain.MiaoshaOrder;
import com.imocc.miaosha.domain.MiaoshaUser;
import com.imocc.miaosha.domain.OrderInfo;
import com.imocc.miaosha.redis.MiaoshaKey;
import com.imocc.miaosha.util.MD5Util;
import com.imocc.miaosha.util.UUIDUtil;
import com.imocc.miaosha.vo.GoodsVo;

@Service
public class MiaoshaService {
	
	//通常在service层中只应用自己的dao
	//若涉及其他的 则引用他的service
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	RedisService redisService;
	
	@Transactional
	public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
		//减库存 下订单 写入秒杀订单
		boolean success = goodsService.reduceStock(goods);
		//减库存成功 创建订单
		if(success){
			return orderService.createOrder(user, goods);
		}else{
			setGoodsOver(goods.getId());
			return null;
		}
	}

	/**
	 * 获取秒杀订单id
	 * @param id
	 * @param goodsId
	 * @return
	 */
	public long getMiaoshaResult(Long userId, long goodsId) {
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
		//订单查到了说明成功了 返回订单的id
		if(order != null){
			return order.getOrderId();
		}else{
			//判断失败的原因 是秒杀完了 还是排队中
			boolean isOver = getGoodsOver(goodsId);
			//如果商品已经秒杀完了 返回-1 派对中返回0 继续轮询
			if(isOver){
				return -1;
			}else{
				return 0;
			}
		}
	}

	private void setGoodsOver(Long goodsId) {
		redisService.set(MiaoshaKey.isGoodsOver, ""+goodsId, true);
	}
	
	private boolean getGoodsOver(long goodsId) {
		return redisService.exists(MiaoshaKey.isGoodsOver, ""+goodsId);
	}

	public boolean checkPath(MiaoshaUser user, long goodsId, String path) {
		if(user == null || path == null){
			return false;
		}
		String pathOld = redisService.get(MiaoshaKey.getMiaoshaPath, ""+user.getId()+"_"+goodsId, String.class);
		return path.equals(pathOld);
	}

	public String createMiaoshaPath(MiaoshaUser user, long goodsId) {
		if(user == null || goodsId <= 0){
			return null;
		}
		//生成一个随机的字符串
 		String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
 		//将path存入redis中
 		redisService.set(MiaoshaKey.getMiaoshaPath, ""+user.getId()+"_"+goodsId, str);
		return str;
	}

	//生成验证码图像
	public BufferedImage createMiaoshaVerifyCode(MiaoshaUser user, long goodsId) {
		if(user == null || goodsId <=0) {
			return null;
		}
		int width = 80;
		int height = 32;
		//create the image
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		// set the background color
		g.setColor(new Color(0xDCDCDC));
		g.fillRect(0, 0, width, height);
		// draw the border
		g.setColor(Color.black);
		g.drawRect(0, 0, width - 1, height - 1);
		// create a random instance to generate the codes
		Random rdm = new Random();
		// make some confusion
		for (int i = 0; i < 50; i++) {
			int x = rdm.nextInt(width);
			int y = rdm.nextInt(height);
			g.drawOval(x, y, 0, 0);
		}
		// generate a random code
		String verifyCode = generateVerifyCode(rdm);
		g.setColor(new Color(0, 100, 0));
		g.setFont(new Font("Candara", Font.BOLD, 24));
		g.drawString(verifyCode, 8, 24);
		g.dispose();
		//把验证码存到redis中
		int rnd = calc(verifyCode);
		redisService.set(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, rnd);
		//输出图片	
		return image;
	}
	
	//校验算术结果 ->验证成功后删除key
	public boolean checkVerifyCode(MiaoshaUser user, long goodsId, int verifyCode) {
		if(user == null || goodsId <=0) {
			return false;
		}
		Integer codeOld = redisService.get(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, Integer.class);
		if(codeOld == null || codeOld - verifyCode != 0 ) {
			return false;
		}
		redisService.delete(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId);
		return true;
	}
	
	/**
	 * 传入数学公示后 用来计算结果的
	 * ScriptEngineManager是Java和Js互相调用的引擎
	 * eval() 函数可计算某个字符串，并执行其中的的 JavaScript 代码。
	 * @param exp
	 * @return
	 */
	private static int calc(String exp) {
		try {
			ScriptEngineManager manger = new ScriptEngineManager();
			ScriptEngine engine = manger.getEngineByName("JavaScript");
			return (Integer)engine.eval(exp);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	private static char[]ops = new char[]{'+', '-', '*'};
	/**
	 * 生成随机的数学公式验证码  + - *操作
	 * @param rdm
	 * @return
	 */
	private String generateVerifyCode(Random rdm) {
		//随机生成三个数字
		int num1 = rdm.nextInt(10);
		int num2 = rdm.nextInt(10);
		int num3 = rdm.nextInt(10);
		//获取操作符号
		char op1 = ops[rdm.nextInt(3)];
		char op2 = ops[rdm.nextInt(3)];
		String exp = "" + num1 + op1 + num2 +op2 + num3;
		return exp;
	}

}
