package com.imocc.miaosha.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import com.imocc.miaosha.domain.MiaoshaUser;
import com.imocc.miaosha.redis.GoodsKey;
import com.imocc.miaosha.result.CodeMsg;
import com.imocc.miaosha.result.Result;
import com.imocc.miaosha.service.GoodsService;
import com.imocc.miaosha.service.MiaoshaUserService;
import com.imocc.miaosha.service.RedisService;
import com.imocc.miaosha.vo.GoodsDetailVo;
import com.imocc.miaosha.vo.GoodsVo;

/**
 * 商品类控制器
 * @author nanshoudabaojian
 *
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {
	
	private static Logger log = LoggerFactory.getLogger(GoodsController.class);
	
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
		
		/**
		 * 优化前:
		 * 	手机端可能将此直接放入请求参数中 所以用@RequestParam
		 * 	@CookieValue注解主要是将请求的Cookie数据，映射到功能处理方法的参数上。
		 * 	@CookieValue(value=MiaoshaUserService.COOKIE_NAME_TOKEN, required=false)String cookieToken, 
	     * 	@RequestParam(value=MiaoshaUserService.COOKIE_NAME_TOKEN, required=false)String paramToken) {
	     *	
	     * 优化后: 
	     *  添加拦截器来处理参数，之后若有变化，只需修改拦截器即可
		 */
		@RequestMapping(value="/to_list", produces="text/html")
		@ResponseBody
	    public String toList(Model model, HttpServletResponse response, MiaoshaUser user, HttpServletRequest request){
			//取缓存
	 		String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
	 		if(!StringUtils.isEmpty(html)){
	 			return html;
	 		}
			model.addAttribute("user", user);
	 		log.info("登录成功，跳转到商品列表页");
	 		//查询商品列表
	 		List<GoodsVo> goodsList = goodsService.listGoodsVo();
	 		model.addAttribute("goodsList", goodsList);
//	 		return "goods_list";
	 		
	 		SpringWebContext ctx = new SpringWebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap(), applicationContext);
	 		//手动渲染
	 		html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
	 		if(!StringUtils.isEmpty(html)){
	 			redisService.set(GoodsKey.getGoodsList, "", html);
	 		}
	 		return html;
		}
		
		/** 
		 * 跳转到商品详情页   
		 * //snowflake 生成id！！！！！！！！！！百度一下
		 * @PathVariable是spring3.0的一个新功能：接收请求路径中占位符的值
		 */
		@RequestMapping(value="/detail/{goodsId}")
		@ResponseBody
	    public Result<GoodsDetailVo> detail(Model model,  MiaoshaUser user, HttpServletResponse response, @PathVariable("goodsId")long goodsId, HttpServletRequest request){
	 		//查询商品详情
	 		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
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
	 		GoodsDetailVo vo = new GoodsDetailVo();
	 		vo.setGoods(goods);
	 		vo.setMiaoshaStatus(miaoshaStatus);
	 		vo.setRemainSeconds(remainSeconds);
	 		vo.setUser(user);
	 		return Result.success(vo);
	    }
		
		@RequestMapping(value="/to_detail2/{goodsId}", produces="text/html")
		@ResponseBody
	    public String detail2(Model model,  MiaoshaUser user, HttpServletResponse response, @PathVariable("goodsId")long goodsId, HttpServletRequest request){
			log.info("跳转到商品:" + goodsId + "详情页");
			
	 		model.addAttribute("user", user);
	 		//查询商品详情
	 		//取缓存
	 		String html = redisService.get(GoodsKey.getGoodsDetail, "" + goodsId, String.class);
	 		if(!StringUtils.isEmpty(html)){
	 			return html;
	 		}
	 		
	 		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
	 		if(goods == null){
	 			model.addAttribute("errmsg", CodeMsg.GOODS_NOT_EXIST.getMsg());
	 			return "goods_not_exists";
	 		}
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
//			return "goods_detail";
	 		
	 		SpringWebContext ctx = new SpringWebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap(), applicationContext);
	 		//手动渲染
	 		html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
	 		if(!StringUtils.isEmpty(html)){
	 			redisService.set(GoodsKey.getGoodsDetail, "" + goodsId, html);
	 		}
	 		return html;
	 		
	    }
		
	 	
}
