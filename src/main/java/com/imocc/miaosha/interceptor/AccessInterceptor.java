package com.imocc.miaosha.interceptor;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.alibaba.fastjson.JSON;
import com.imocc.miaosha.access.UserContext;
import com.imocc.miaosha.annotation.AccessLimit;
import com.imocc.miaosha.configuration.UserArgumentResolver;
import com.imocc.miaosha.domain.MiaoshaUser;
import com.imocc.miaosha.redis.AccessKey;
import com.imocc.miaosha.result.CodeMsg;
import com.imocc.miaosha.result.Result;
import com.imocc.miaosha.service.MiaoshaUserService;
import com.imocc.miaosha.service.RedisService;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter{
	
	@Autowired
	MiaoshaUserService miaoshaUserService;
	
	@Autowired
	UserArgumentResolver userArgumentResolver;
	
	@Autowired
	RedisService redisService;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		//HandlerMethod封装方法定义相关的信息,如类,方法,参数等.
		if(handler instanceof HandlerMethod){
			//取用户 并存入ThreadLocal 因为处理一个响应的是同一个线程
			MiaoshaUser miaoshaUser = getUser(request, response);
			UserContext.setUser(miaoshaUser);
			
			HandlerMethod method = (HandlerMethod)handler;
			AccessLimit accessLimit = method.getMethodAnnotation(AccessLimit.class);
			if(accessLimit == null){
				return true;
			}
			int seconds = accessLimit.seconds();
			int maxCount = accessLimit.maxCount();
			boolean needLogin = accessLimit.needLogin();
			
			String key = request.getRequestURI();
			//是否需要登录
			if(needLogin){
				//如果没登录 就返回错误信息给页面
				if(miaoshaUser == null){
					render(response, CodeMsg.USER_NOT_LOGIN);
					return false;
				}
				//如果已登录 就设置key 也就是redis中的key 此key的value就是可登录的次数
				key += "_" + miaoshaUser.getId();
			}else{
				//do nothing
			}
			//martine flower,重构-改善既有代码的设计
			
			//设置访问次数和key的存活时间
			AccessKey accessKey = AccessKey.withExpire(seconds);
	 		Integer count = redisService.get(accessKey, key, Integer.class);
	 		if(count == null){
	 			redisService.set(accessKey, key, 1);
	 		}else if(count < maxCount){
	 			redisService.incr(accessKey, key);
	 		}else{
	 			//将错误信息写回给页面
	 			render(response, CodeMsg.ACCESS_LIMIT_REACHED);
	 			return false;
	 		}
		}
		return true;
	}
	
	/**
	 * 将错误码写给response 写回给页面
	 * @param response
	 * @param uSER_NOT_LOGIN
	 * @throws IOException 
	 */
	private void render(HttpServletResponse response, CodeMsg codeMsg) throws IOException {
		//设置页面编码
		response.setContentType("application/json;charset=UTF-8");
		ServletOutputStream outputStream = response.getOutputStream();
		String str = JSON.toJSONString(Result.error(codeMsg));
		outputStream.write(str.getBytes("UTF-8"));
		outputStream.flush();
		outputStream.close();
	}

	/**
	 * 获取当前用户
	 * @param request
	 * @param response
	 * @return
	 */
	private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response){
		//获取token
		String paramToken = request.getParameter(MiaoshaUserService.COOKIE_NAME_TOKEN);
		String cookieToken = userArgumentResolver.getCookieValue(request, MiaoshaUserService.COOKIE_NAME_TOKEN);
		if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)){
 			return null;
 		}
		//此处优先取param中的token
 		String token = StringUtils.isEmpty(paramToken)? cookieToken:paramToken;
		MiaoshaUser user = miaoshaUserService.getByToken(response, token);
		return user;
	}
	
}
