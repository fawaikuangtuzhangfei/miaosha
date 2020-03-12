package com.imocc.miaosha.controller;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imocc.miaosha.annotation.AccessLimit;
import com.imocc.miaosha.annotation.NeedLogin;
import com.imocc.miaosha.domain.MiaoshaOrder;
import com.imocc.miaosha.domain.MiaoshaUser;
import com.imocc.miaosha.domain.OrderInfo;
import com.imocc.miaosha.rabbitmq.MQSender;
import com.imocc.miaosha.rabbitmq.MiaoshaMessage;
import com.imocc.miaosha.redis.GoodsKey;
import com.imocc.miaosha.result.CodeMsg;
import com.imocc.miaosha.result.Result;
import com.imocc.miaosha.service.GoodsService;
import com.imocc.miaosha.service.MiaoshaService;
import com.imocc.miaosha.service.MiaoshaUserService;
import com.imocc.miaosha.service.OrderService;
import com.imocc.miaosha.service.RedisService;
import com.imocc.miaosha.vo.GoodsVo;

/**
 * 秒杀控制器
 * 优化：
 * InitializingBean？？？
 * @author nanshoudabaojian
 *
 */
@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean{
	
	private static Logger log = LoggerFactory.getLogger(MiaoshaController.class);
	
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
		
		@Autowired
		MQSender mqSender;
		
		//标记位 如果卖完了 就不要和redis交互了
		private Map<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();
		
		/**
		 * 实现了InitializingBean接口 会先调用此方法
		 * 系统初始化
		 * 将商品数量加入缓存中
		 * @throws Exception
		 */
		@Override
		public void afterPropertiesSet() throws Exception {
			List<GoodsVo> goodsList = goodsService.listGoodsVo();
			if(goodsList == null){
				return;
			}
			for(GoodsVo goods : goodsList){
				log.info("加载之前 将"+goods.getId()+"商品的库存存入redis中,库存为" + goods.getStockCount());
				//将商品数量加入缓存中
				redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), goods.getStockCount());
				//如果卖完了 就不要和redis交互了
				localOverMap.put(goods.getId(), false);
			}
		}
		
		//新的秒杀 用户请求成功后加入队列 @PathVariable???
		//@PathVariable是spring3.0的一个新功能：接收请求路径中占位符的值
		@RequestMapping(value = "/{path}/do_miaosha", method = RequestMethod.POST)
		@ResponseBody
		@NeedLogin
	    public Result<Integer> doMiaosha(Model model, MiaoshaUser user, @RequestParam("goodsId")long goodsId, @PathVariable String path){
	 		model.addAttribute("user", user);
	 		if(user == null){
	 			return Result.error(CodeMsg.SESSION_ERROR);
	 		}
	 		//验证path
	 		boolean check = miaoshaService.checkPath(user, goodsId, path);
	 		if(!check){
	 			return Result.error(CodeMsg.MIAO_SHA_OVER); 
	 		}
	 		Boolean over = localOverMap.get(goodsId);
	 		//标记如果为true 直接返回 不用请求redis
	 		if(over){
	 			return Result.error(CodeMsg.MIAO_SHA_OVER); 
	 		}
	 		
	 		//预减库存
	 		long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, ""+ goodsId);
	 		
	 		if(stock < 0){
	 			//卖完了就标记 下一次就直接返回了 不需要再请求redis了 
	 			localOverMap.put(goodsId, true);
	 			return Result.error(CodeMsg.MIAO_SHA_OVER); 
	 		}
	 		//判断是否已经秒杀到了
	 		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
	 		if(order != null){
	 			//如果订单重复了 就incr一下
	 			redisService.incr(GoodsKey.getMiaoshaGoodsStock, ""+ goodsId);
	 			return Result.error(CodeMsg.REPEATE_MIAOSHA);
	 		}
	 		log.info("库存"+stock);
	 		//入队
	 		MiaoshaMessage mm = new MiaoshaMessage();
	 		mm.setGoodsId(goodsId);
	 		mm.setUser(user);
	 		mqSender.sendMiaoshaMessage(mm);
	 		return Result.success(0);//排队中
	 		
	    }
		
		/**
		 * 获取秒杀结果
		 * 成功返回orderId
		 * 失败返回-1
		 * 排队中返回0
		 */
		@RequestMapping(value = "/getMiaoshaResult", method = RequestMethod.GET)
		@ResponseBody
		@NeedLogin
	    public Result<Long> getMiaoshaResult(Model model, MiaoshaUser user, @RequestParam("goodsId")long goodsId){
	 		model.addAttribute("user", user);
	 		if(user == null){
	 			return Result.error(CodeMsg.SESSION_ERROR);
	 		}
	 		long result = miaoshaService.getMiaoshaResult(user.getId(), goodsId);
	 		
			return Result.success(result);
	    }
		
		//获取秒杀的地址
		@AccessLimit(seconds=5, maxCount=5, needLogin=true)
		@RequestMapping(value = "/getMiaoshaPath", method = RequestMethod.GET)
		@ResponseBody
		@NeedLogin
	    public Result<String> getMiaoshaPath(HttpServletRequest request, Model model, MiaoshaUser user, @RequestParam("goodsId")long goodsId, @RequestParam(value="verifyCode", defaultValue="0")int verifyCode){
	 		model.addAttribute("user", user);
	 		if(user == null){
	 			return Result.error(CodeMsg.SESSION_ERROR);
	 		}
	 		
	 		//进行校验
	 		boolean check = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
	 		if(!check){
	 			return Result.error(CodeMsg.VERIFYCODE_ERROR);
	 		}
	 		//创建path
	 		String path = miaoshaService.createMiaoshaPath(user, goodsId);
			return Result.success(path);
	    }
		
		//生成图片验证码
		@RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
		@ResponseBody
		@NeedLogin
	    public Result<String> getMiaoshaVerifyCode(HttpServletResponse response, MiaoshaUser user, @RequestParam("goodsId")long goodsId){
	 		if(user == null){
	 			return Result.error(CodeMsg.SESSION_ERROR);
	 		}
	 		
	 		BufferedImage image = miaoshaService.createMiaoshaVerifyCode(user, goodsId);
			try{
				ServletOutputStream out = response.getOutputStream();
				ImageIO.write(image, "JPEG", out);
				out.flush();
				out.close();
				return null;
			}catch(Exception e){
				e.printStackTrace();
				return Result.error(CodeMsg.MIAOSHA_FAIL);
			}
	    }
		
		//前后端分离
		@RequestMapping("/do_miaosha2")
	    public String toList(Model model, MiaoshaUser user, @RequestParam("goodsId")long goodsId){
	 		model.addAttribute("user", user);
	 		if(user == null){
	 			return "login";
	 		}
	 		//判断库存
	 		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
	 		Integer stock = goods.getStockCount();
	 		if(stock <= 0){
	 			model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER.getMsg());
	 			return "miaosha_fail";
	 		}
	 		//判断是否已经秒杀到了
	 		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goods.getId());
	 		if(order != null){
	 			model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
	 			return "miaosha_fail";
	 		}
	 		//减库存 下订单 写入秒杀订单
	 		OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
	 		model.addAttribute("orderInfo", orderInfo);
	 		model.addAttribute("goods", goods);
			return "order_detail";
	    }
		
		/**
		 * 最开始的版本 前后端没分离
		 * GET和POST的区别
		 * Get是幂等的 无论调用多少次 对服务端都不会有影响
		 * <a href = "/detail?id=1231">是错误的 对服务端发生变化的用post
		 */
		@RequestMapping(value = "/do_miaosha3", method = RequestMethod.POST)
		@ResponseBody
		@NeedLogin
	    public Result<OrderInfo> doMiaosha3(Model model, MiaoshaUser user, @RequestParam("goodsId")long goodsId){
	 		model.addAttribute("user", user);
	 		if(user == null){
	 			return Result.error(CodeMsg.SESSION_ERROR);
	 		}
	 		//判断库存
	 		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
	 		Integer stock = goods.getStockCount();
	 		if(stock <= 0){
	 			return Result.error(CodeMsg.MIAO_SHA_OVER);
	 		}
	 		//判断是否已经秒杀到了
	 		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goods.getId());
	 		if(order != null){
	 			return Result.error(CodeMsg.REPEATE_MIAOSHA);
	 		}
	 		//减库存 下订单 写入秒杀订单
	 		OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
			return Result.success(orderInfo);
	    }
		
		
		/**
		 * 跳转到商品详情页
		 * //snowflake 生成id！！！！！！！！！！百度一下
		 * @PathVariable是spring3.0的一个新功能：接收请求路径中占位符的值
		 */
		@RequestMapping("/to_detail/{goodsId}")
	    public String detail(Model model,  MiaoshaUser user, @PathVariable("goodsId")long goodsId){
			log.info("跳转到商品:" + goodsId + "详情页");
			
	 		model.addAttribute("user", user);
	 		//查询商品详情
	 		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
	 		model.addAttribute("goods", goods);
	 		//获取秒杀开始、结束的时间 以及现在的时间
	 		long startAt = goods.getStartDate().getTime();
	 		long endAt = goods.getEndDate().getTime();
	 		long now = System.currentTimeMillis();
	 		int miaoshaStatus = 0;//秒杀状态
	 		int remainSeconds = 0;
	 		//秒杀还没开始 倒计时
	 		if(now < startAt){
	 			miaoshaStatus = 0;
	 			remainSeconds = (int) ((startAt-now)/1000);
	 		}else if(now > endAt){
	 			//秒杀已经结束
	 			miaoshaStatus = 2;
	 			remainSeconds = -1;
	 		}else{
	 			//秒杀进行中
	 			miaoshaStatus = 1;
	 			remainSeconds = 0;
	 		}
	 		model.addAttribute("miaoshaStatus", miaoshaStatus);
	 		model.addAttribute("remainSeconds", remainSeconds);
			return "goods_detail";
	    }

	 	
		
		
}
