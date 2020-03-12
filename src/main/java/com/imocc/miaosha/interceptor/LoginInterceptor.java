package com.imocc.miaosha.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.imocc.miaosha.annotation.NeedLogin;
import com.imocc.miaosha.configuration.UserArgumentResolver;
import com.imocc.miaosha.domain.MiaoshaUser;
import com.imocc.miaosha.service.MiaoshaUserService;

/**
 * 用户判断当前用户是否已经登录 减少重复代码
 * @author nanshoudabaojian
 *
 */
@Service
public class LoginInterceptor implements HandlerInterceptor{
	
	@Autowired
	MiaoshaUserService miaoshaUserService;
	
	@Autowired
	UserArgumentResolver userArgumentResolver;

	/**
	 * true为放行 false为不放行
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		//判断如果不是请求control方法直接返回true        
		if(! (handler instanceof HandlerMethod)){            
			return true;        
		}        
		HandlerMethod method = (HandlerMethod) handler;        
		//判断访问的control是否添加LoginRequired注解        
		NeedLogin needLogin =  method.getMethodAnnotation(NeedLogin.class);
		if(needLogin != null){
			//获取token
			String paramToken = request.getParameter(MiaoshaUserService.COOKIE_NAME_TOKEN);
			String cookieToken = userArgumentResolver.getCookieValue(request, MiaoshaUserService.COOKIE_NAME_TOKEN);
			String path = request.getContextPath() + "/login/to_login";
			if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)){
				//不跳转 暂时不知道咋解决？并且没有错误提示 陷入瓶颈
				response.sendRedirect(path);
	 			return false;
	 		}
			//此处优先取param中的token
	 		String token = StringUtils.isEmpty(paramToken)? cookieToken:paramToken;
			MiaoshaUser user = miaoshaUserService.getByToken(response, token);
			if(user == null){
				response.sendRedirect(path);
				return false;
			}
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	
}
