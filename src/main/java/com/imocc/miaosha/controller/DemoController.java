package com.imocc.miaosha.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imocc.miaosha.domain.User;
import com.imocc.miaosha.rabbitmq.MQSender;
import com.imocc.miaosha.redis.UserKey;
import com.imocc.miaosha.result.CodeMsg;
import com.imocc.miaosha.result.Result;
import com.imocc.miaosha.service.RedisService;
import com.imocc.miaosha.service.UserService;

@Controller
@RequestMapping("/demo")
public class DemoController {
	
		@Autowired
		UserService userService;
	
		@Autowired
		RedisService redisService;
		
		@Autowired
		MQSender sender;
		
		//swagger?????
		@RequestMapping("/mq/headers")
	    @ResponseBody
	    String headers() {
			sender.sendHeader("hello headers");
	        return "Hello World!";
	    }
		
		//swagger?????
		@RequestMapping("/mq/fanout")
	    @ResponseBody
	    String fanout() {
			sender.sendFanout("hello immoc");
	        return "Hello World!";
	    }
		
		@RequestMapping("/mq/topic")
	    @ResponseBody
	    String topic() {
			sender.sendTopic("hello immoc");
	        return "Hello World!";
	    }
		
		@RequestMapping("/mq")
	    @ResponseBody
	    String mq() {
			sender.send("hello immoc");
	        return "Hello World!";
	    }
		
	 	@RequestMapping("/helloword")
	    @ResponseBody
	    String home() {
	        return "Hello World!";
	    }
	 	
	 	//1.rest api json输出 2.页面
	 	
	 	
	 	@RequestMapping("/hello")
	    @ResponseBody
	    public Result<String> hello() {
	 		return Result.success("hello immoc");
	    }
	 	
	 	@RequestMapping("/helloError")
	    @ResponseBody
	    public Result<String> helloError() {
	 		return Result.error(CodeMsg.SERVER_ERROR);
	    }
	 	
	 	@RequestMapping("/thymeleaf")
	    public String thymeleaf(Model model) {
	 		model.addAttribute("name", "Josha");
	 		return "hello";
	    }
	 	
	 	@RequestMapping("/dbget")
	    @ResponseBody
	    public Result<User> dbGet() {
	 		User user = userService.getById(1);
	 		return Result.success(user);
	    }
	 	
	 	@RequestMapping("/db/tx")
	    @ResponseBody
	    public Result<Boolean> dbTx() {
	 		return Result.success(userService.tx());
	    }
	 	
	 	@RequestMapping("/redis/get")
	    @ResponseBody
	    public Result<User> redisGet() {
	 		User user = redisService.get(UserKey.getById, ""+1, User.class);
	 		return Result.success(user);
	    }
	 	
	 	@RequestMapping("/redis/set")
	    @ResponseBody
	    public Result<Boolean> redisSet() {
	 		User user = new User();
	 		user.setId(1);
	 		user.setName("11111");
	 		boolean ret = redisService.set(UserKey.getById, ""+1, user);
	 		return Result.success(ret);
	    }
	 	
}
