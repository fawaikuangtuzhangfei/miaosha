package com.imocc.miaosha.result;

public class CodeMsg {
	private int code;
	private String msg;
	
	//通用异常
	public static CodeMsg SUCCESS = new CodeMsg(0, "success");
	public static CodeMsg SERVER_ERROR = new CodeMsg(500100, "服务端异常");
	public static CodeMsg BIND_ERROR = new CodeMsg(500101, "参数校验异常：%s");
	public static CodeMsg REQUEST_ILLEGAL = new CodeMsg(500102, "请求非法");
	public static CodeMsg ACCESS_LIMIT_REACHED = new CodeMsg(500103, "请求过于频繁,请稍后再试");

	//登录模块 5002xx
	public static CodeMsg SESSION_ERROR = new CodeMsg(500210, "Session不存在或者已经失效");
	public static CodeMsg PASSWORD_EMPTY = new CodeMsg(500211, "密码不能为空");
	public static CodeMsg MOBILE_EMPTY = new CodeMsg(500212, "手机号不能为空");
	public static CodeMsg MOBILE_ERROR = new CodeMsg(500213, "手机号格式错误");
	public static CodeMsg MOBILE_NOT_EXIST = new CodeMsg(500214, "手机号不存在");
	public static CodeMsg PASSWORD_ERROR = new CodeMsg(500215, "密码错误");
	public static CodeMsg USER_NOT_LOGIN = new CodeMsg(500216, "请登录");

	
	//商品模块 5003xx
	public static CodeMsg GOODS_NOT_EXIST = new CodeMsg(500300, "商品不存在");
	
	//订单模块 5004xx
	public static CodeMsg ORDER_NOT_EXIST = new CodeMsg(500400, "订单不存在");

	//秒杀模块 5005xx
	public static CodeMsg MIAO_SHA_OVER = new CodeMsg(500500, "商品已经秒杀完毕");
	public static CodeMsg REPEATE_MIAOSHA = new CodeMsg(500501, "不能重复秒杀");
	public static CodeMsg MIAOSHA_FAIL = new CodeMsg(500502, "秒杀失败");
	public static CodeMsg VERIFYCODE_ERROR = new CodeMsg(500503, "验证码错误");

	
	private CodeMsg(int code, String msg){
		this.code = code;
		this.msg = msg;
	}
	
	
	public int getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}
	
	public CodeMsg fillArgs(Object...args){
		int code = this.code;
		//String.format(xx, abc, cbd) 将可变参数添加到xx中占位符(eg:%s)的相应位置
		String message = String.format(this.msg, args);
		return new CodeMsg(code, message);
	}

	@Override
	public String toString() {
		return "CodeMsg [code=" + code + ", msg=" + msg + "]";
	}
	
}
