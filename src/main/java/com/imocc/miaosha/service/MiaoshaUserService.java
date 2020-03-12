package com.imocc.miaosha.service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.imocc.miaosha.dao.MiaoshaUserDao;
import com.imocc.miaosha.domain.MiaoshaUser;
import com.imocc.miaosha.exception.GlobalException;
import com.imocc.miaosha.redis.MiaoshaUserKey;
import com.imocc.miaosha.result.CodeMsg;
import com.imocc.miaosha.util.MD5Util;
import com.imocc.miaosha.util.UUIDUtil;
import com.imocc.miaosha.vo.LoginVo;

/**
 * 秒杀用户的service层
 * 1.业务逻辑
 * 2.将用户的session存入redis中来实现分布式的session
 * @author nanshoudabaojian
 *
 */
@Service
public class MiaoshaUserService {
	
	public static final String COOKIE_NAME_TOKEN = "token";
	
	@Autowired
	MiaoshaUserDao miaoshaUserDao;
	
	@Autowired
	RedisService redisService;
	
	/**
	 * 调用dao层与数据库交互 查询密码
	 */
	public MiaoshaUser getById(long id){
		//获取缓存
		MiaoshaUser user = redisService.get(MiaoshaUserKey.getById, ""+id, MiaoshaUser.class);
		if(user != null){
			return user;
		}
		//取数据库并加到将用户的信息存入缓存里面
		user = miaoshaUserDao.getById(id);
		if(user != null){
			redisService.set(MiaoshaUserKey.getById, ""+id, user);
		}
		return user;
	}
	
	//对象级缓存 必须处理缓存
	public boolean updatePassword(String token, long id, String formPass){
		//取user
		MiaoshaUser user = getById(id);
		if(user == null){
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		//更新数据库
		MiaoshaUser toBeUpdate = new MiaoshaUser();
		toBeUpdate.setId(id);
		toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass, user.getSalt()));
		//必须先更新数据库
		miaoshaUserDao.update(toBeUpdate);
		//处理缓存 1.删除掉原先的用户相关信息 2.设置token中用户的相关信息
		redisService.delete(MiaoshaUserKey.getById, ""+id);
		user.setPassword(toBeUpdate.getPassword());
		redisService.set(MiaoshaUserKey.token, token, user);
		return true;
	}
	
	/**
	 * 获取token
	 */
	public MiaoshaUser getByToken(HttpServletResponse response, String token) {
		if(StringUtils.isEmpty(token)){
			return null;
		}
		//获取token中存的用户信息
		MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
		//延长有效期
		if(user != null){
			addCookie(response, token, user);
		}
		return user;
	}

	/**
	 * 登录
	 */
	public boolean login(HttpServletResponse response, LoginVo loginVo) {
		if(loginVo == null){
			throw new GlobalException(CodeMsg.SERVER_ERROR);
		}
		String mobile = loginVo.getMobile();
		String formPass = loginVo.getPassword();
		MiaoshaUser user = getById(Long.parseLong(mobile));
		//验证手机号是否存在
		if(user == null){
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		//验证密码
		String dbPass = user.getPassword();
		String saltDB = user.getSalt();
		//第二次加密
		String calcPass = MD5Util.formPassToDBPass(formPass, saltDB);
		if(!calcPass.equals(dbPass)){
			throw new GlobalException(CodeMsg.PASSWORD_ERROR);
		}
		//生成cookie 并将用户信息存入全局session中
		String token = UUIDUtil.uuid();
		addCookie(response, token, user);
		return true;
	}

	//将用户信息存入全局session中
	private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
		redisService.set(MiaoshaUserKey.token, token, user);
		Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
		//设置cookie存活时间
		cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
		//可在同一应用服务器内共享方法
		cookie.setPath("/");
		//添加cookie
		response.addCookie(cookie);
	}
	
	/**
	 * 为了jmeter拿测试token
	 */
	public String getTokenByLogin(HttpServletResponse response, LoginVo loginVo) {
		if(loginVo == null){
			throw new GlobalException(CodeMsg.SERVER_ERROR);
		}
		String mobile = loginVo.getMobile();
		String formPass = loginVo.getPassword();
		MiaoshaUser user = getById(Long.parseLong(mobile));
		//验证手机号是否存在
		if(user == null){
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		//验证密码
		String dbPass = user.getPassword();
		String saltDB = user.getSalt();
		//第二次加密
		String calcPass = MD5Util.formPassToDBPass(formPass, saltDB);
		if(!calcPass.equals(dbPass)){
			throw new GlobalException(CodeMsg.PASSWORD_ERROR);
		}
		//生成cookie 并将用户信息存入全局session中
		String token = UUIDUtil.uuid();
		addCookie(response, token, user);
		return token;
	}

}
