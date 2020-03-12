package com.imocc.miaosha.configuration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.imocc.miaosha.access.UserContext;
import com.imocc.miaosha.domain.MiaoshaUser;

/**
 * 获取token
 * @author nanshoudabaojian
 *
 */
@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
	
	/**
	 * supportsParameter：用于判定是否需要处理该参数分解，返回true为需要，并会去调用下面的方法resolveArgument。
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		Class<?> clazz = parameter.getParameterType();
		return clazz == MiaoshaUser.class;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		//已经将user存入了ThreadLocal 所以直接取就行
		return UserContext.getUser();
	}
	
	
	/**
	 * 获取cookie中的值
	 * 	遍历所有cookie 找到其中相等的
	 * @param request
	 * @param cookieName
	 * @return
	 */
	public String getCookieValue(HttpServletRequest request, String cookieName){
		Cookie[] cookies = request.getCookies();
		if(cookies == null || cookies.length <= 0){
			return null;
		}
		for(Cookie cookie : cookies){
			if(cookie.getName().equals(cookieName)){
				return cookie.getValue();
			}
		}
		return null;
	}

}
