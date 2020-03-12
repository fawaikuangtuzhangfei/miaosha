package com.imocc.miaosha.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

public class ValidatorUtil {
	
	private static final Pattern mobile_pattern = Pattern.compile("1\\d{10}");
	
	public static boolean isMobile(String src){
		if(StringUtils.isEmpty(src)){
			return false;
		}
		//构造方法
		Matcher m = mobile_pattern.matcher(src);
		//匹配上则true
		return m.matches();
	}
	
}
