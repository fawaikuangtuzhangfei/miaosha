package com.imocc.miaosha.vo;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.imocc.miaosha.validator.IsMobile;

/**
 * 用来校验用户的一些参数 比如目前的手机号、密码
 * @author nanshoudabaojian
 *
 */
public class LoginVo {
	
	@NotNull
	@IsMobile
	private String mobile;
	
	@NotNull
	@Length(min=32)
	private String password;
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@Override
	public String toString() {
		return "LoginVo [mobile=" + mobile + ", password=" + password + "]";
	}
	
	
}
