package com.imocc.miaosha.rabbitmq;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MQConfig {
	
	public static final String MIAOSHA_QUEUE = "miaoshaqueue";
	public static final String QUEUE = "queue";
	public static final String TOPIC_QUEUE1 = "topic.queue1";
	public static final String TOPIC_QUEUE2 = "topic.queue2";
	public static final String TOPIC_EXCHANGE = "topicExchange";
	public static final String ROOTING_KEY1 = "topic.key1";
	//#代表1个或者多个  *代表1
	public static final String ROOTING_KEY2 = "topic.#";
	public static final String FANOUT_EXCHANGE = "fanoutExchange";
	//迷惑行为 为什么headersExchange就可以 headerExchange就不行？？？？？？
	public static final String HEADERS_EXCHANGE = "headerExchange";
	public static final String HEADER_QUEUE = "header.queue";
	
	@Bean
	public Queue miaosQueue() {
		return new Queue(MIAOSHA_QUEUE, true);
	}

	//创建队列  名字 是否持久化
	//Direct模式 交换机Exchange
	@Bean
	public Queue queue() {
		return new Queue(QUEUE, true);
	}
	
	//Topic模式 交换机Exchange
	@Bean
	public Queue topicQueue1() {
		return new Queue(TOPIC_QUEUE1, true);
	}
	@Bean
	public Queue topicQueue2() {
		return new Queue(TOPIC_QUEUE2, true);
	}
	@Bean
	public TopicExchange topicExchage(){
		return new TopicExchange(TOPIC_EXCHANGE);
	}
	@Bean
	public Binding topicBinding1() {
		return BindingBuilder.bind(topicQueue1()).to(topicExchage()).with(ROOTING_KEY1);
	}
	@Bean
	public Binding topicBinding2() {
		return BindingBuilder.bind(topicQueue2()).to(topicExchage()).with(ROOTING_KEY2);
	}
	
	//Fanout模式 交换机Exchange 广播
	@Bean
	public FanoutExchange fanoutExchange(){
		return new FanoutExchange(FANOUT_EXCHANGE);
	}
	@Bean
	public Binding FanoutBinding1(){
		return BindingBuilder.bind(topicQueue1()).to(fanoutExchange());
	}
	@Bean
	public Binding FanoutBinding2(){
		return BindingBuilder.bind(topicQueue2()).to(fanoutExchange());
	}
	
	//Header模式 交换机Exchange
	@Bean
	public HeadersExchange headersExchange(){
		return new HeadersExchange(HEADERS_EXCHANGE);
	}
	@Bean
	public Queue headerQueue(){
		return new Queue(HEADER_QUEUE, true);
	}
	@Bean
	public Binding headerBinding(){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("header1", "value1");
		map.put("header2", "value2");
		//满足条机才送
		return BindingBuilder.bind(headerQueue()).to(headersExchange()).whereAll(map).match();

	}
}
