package com.imocc.miaosha.exception;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imocc.miaosha.result.CodeMsg;
import com.imocc.miaosha.result.Result;

/**
 * 全局的异常拦截器 对异常进行统一的处理
 * 
 * @ControllerAdvice，是Spring3.2提供的新注解,它是一个Controller增强器,可对controller中被
 * @RequestMapping注解的方法加一些逻辑处理。最常用的就是异常处理
 * 需要配合@ExceptionHandler使用。
 * 当将异常抛到controller时,可以对异常进行统一处理,规定返回的json格式或是跳转到一个错误页面
 * @author nanshoudabaojian
 *
 */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
	
	//@ExceptionHandler(value=xx.class) 处理哪些异常 默认所有异常都处理
	@ExceptionHandler(value=Exception.class)
	public Result<String> execeptionHandler(HttpServletRequest request, Exception e){
		//如果是全局的其他异常就返回相应的错误码
		if(e instanceof GlobalException){
			GlobalException ex = (GlobalException)e;
			return Result.error(ex.getCm());
		//如果是绑定异常就把 错误参数列表中的第一个填充到绑定异常的可变参数里面并返回
		}else if(e instanceof BindException){
			BindException ex = (BindException)e;
			List<ObjectError> errors = ex.getAllErrors();
			ObjectError error = errors.get(0);
			String msg =error.getDefaultMessage();
			return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
		//不是上面两种的就服务器默认错误码
		}else{
			e.printStackTrace();
			return Result.error(CodeMsg.SERVER_ERROR);
		}
		
	}
}
