package com.imocc.miaosha.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.imocc.miaosha.interceptor.AccessInterceptor;
import com.imocc.miaosha.interceptor.LoginInterceptor;

/**
 * 
 * WebMvcConfigurerAdapter 采用JavaBean的形式来代替传统的xml配置文件形式进行针对框架个性化定制
 * 
 * @author nanshoudabaojian
 *
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter{
	
	@Autowired
	UserArgumentResolver userArgumentResolver;
	
	@Autowired
	LoginInterceptor loginInterceptor;
	
	@Autowired
	AccessInterceptor accessInterceptor;
	
	/**
	 * 参数解析
	 * 在触发controller 所有带参数的方法时会先走这个
	 */
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		//处理MiaoshaUser参数
		argumentResolvers.add(userArgumentResolver);
	}
	/**
	 * 拦截器
	 */
	public void addInterceptors(InterceptorRegistry registry) {
		//查看是否登录
		InterceptorRegistration needLogin = registry.addInterceptor(loginInterceptor);
		needLogin.addPathPatterns("/**"); //配置拦截器1的路径
		
		//
		registry.addInterceptor(accessInterceptor);
	}
	
}
