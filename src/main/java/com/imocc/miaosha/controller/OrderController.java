package com.imocc.miaosha.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import com.imocc.miaosha.annotation.NeedLogin;
import com.imocc.miaosha.domain.MiaoshaUser;
import com.imocc.miaosha.domain.OrderInfo;
import com.imocc.miaosha.result.CodeMsg;
import com.imocc.miaosha.result.Result;
import com.imocc.miaosha.service.GoodsService;
import com.imocc.miaosha.service.MiaoshaUserService;
import com.imocc.miaosha.service.OrderService;
import com.imocc.miaosha.service.RedisService;
import com.imocc.miaosha.vo.GoodsVo;
import com.imocc.miaosha.vo.OrderDetailVo;

/**
 * 商品类控制器
 * @author nanshoudabaojian
 *
 */
@Controller
@RequestMapping("/order")
public class OrderController {
	
		@Autowired
		RedisService redisService;
		
		@Autowired
		MiaoshaUserService miaoshaUserService;
		
		@Autowired
		GoodsService goodsService;
		
		@Autowired
		ThymeleafViewResolver thymeleafViewResolver;
		
		@Autowired
		ApplicationContext applicationContext;
		
		@Autowired
		OrderService orderService;
		
		
		/** 
		 * 跳转到订单详情页   
		 * 
		 */
		@RequestMapping(value="/detail")
		@ResponseBody
		@NeedLogin
	    public Result<OrderDetailVo> detail(Model model,  MiaoshaUser user, @RequestParam("orderId") long orderId){
			OrderInfo order = orderService.getOrderById(orderId);
			if(order == null){
				return Result.error(CodeMsg.ORDER_NOT_EXIST);
			}
			long goodsId = order.getGoodsId();
			GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
			OrderDetailVo vo = new OrderDetailVo();
			vo.setGoods(goods);
			vo.setOrder(order);
			return Result.success(vo);
	    }
		
		
	 	
}
