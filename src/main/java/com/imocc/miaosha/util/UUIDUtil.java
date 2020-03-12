package com.imocc.miaosha.util;

import java.util.UUID;

/**
 * 生成uuid
 * @author nanshoudabaojian
 *
 */
public class UUIDUtil {
	public static String uuid(){
		return UUID.randomUUID().toString().replace("-", "");
	}
}
