package com.imocc.miaosha.result;

/**
 * 返回结果
 * @author nanshoudabaojian
 *
 * @param <T>
 */
public class Result<T> {
	
	private int code;
	private String msg;
	private T data;
	
	public static <T> Result<T> success(T data){
		return new Result<T>(data);
		
	}
	
	private Result(T data) {
		this.code = 0;
		this.msg = "success";
		this.data = data;
	}
	
	public static <T> Result<T> error(CodeMsg cm){
		return new Result<T>(cm);
	}
	
	private Result(CodeMsg cm) {
		if(cm == null){
			return;
		}
		this.code = cm.getCode();
		this.msg = cm.getMsg();
	}
	public int getCode() {
		return code;
	}
	
	public String getMsg() {
		return msg;
	}

	public T getData() {
		return data;
	}
	
}
