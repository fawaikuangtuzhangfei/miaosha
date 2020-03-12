package com.imocc.miaosha.controller;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imocc.miaosha.result.Result;
import com.imocc.miaosha.service.MiaoshaUserService;
import com.imocc.miaosha.service.RedisService;
import com.imocc.miaosha.vo.LoginVo;

@Controller
@RequestMapping("/login")
public class LoginController {
	
	private static Logger log = LoggerFactory.getLogger(LoginController.class);
	
	
		@Autowired
		MiaoshaUserService userService;
	
		@Autowired
		RedisService redisService;
		
		@Autowired
		MiaoshaUserService miaoshaUserService;
		
		/**
		 * 前往登录页
		 * @return
		 */
		@RequestMapping("/to_login")
	    public String toLogin() {
	 		return "login";
	    }
	 	
	 	/*
	 	 * 登录功能
	 	 * 
	 	 * @Valid注解可以实现数据的验证，你可以定义实体，在实体的属性上添加校验规则，
	 	 * 而在API接收数据时添加@valid关键字，这时你的实体将会开启一个校验的功能。
	 	 */
	 	@RequestMapping("/do_login")
	    @ResponseBody
	    public Result<Boolean> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
	 		log.info("当前登录用户为:" + loginVo.toString());
	 		//登录
	 		miaoshaUserService.login(response, loginVo);
	 		return Result.success(true);
	    }
	 	
	 	/*
	 	 * 登录功能
	 	 * 
	 	 */
	 	@RequestMapping("/get_token_util")
	    @ResponseBody
	    public Result<String> getToken(HttpServletResponse response, @Valid LoginVo loginVo) {
	 		log.info("当前登录用户为:" + loginVo.toString());
	 		//登录
	 		String token = miaoshaUserService.getTokenByLogin(response, loginVo);
	 		return Result.success(token);
	    }
	 	
	 	
}
