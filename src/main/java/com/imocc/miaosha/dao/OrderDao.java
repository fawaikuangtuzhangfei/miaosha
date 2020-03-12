package com.imocc.miaosha.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;

import com.imocc.miaosha.domain.MiaoshaOrder;
import com.imocc.miaosha.domain.OrderInfo;

/**
 * 订单操作
 * 
@SelectKey 1、生成主键；2、获取刚刚插入数据的主键。
statement属性：填入将会被执行的 SQL 字符串数组，
keyProperty表示查询结果赋值给代码中的哪个对象，keyColumn表示将查询结果赋值给数据库表中哪一列
before属性：填入 true 或 false 以指明 SQL 语句应被在插入语句的之前还是之后执行。
resultType属性：填入 keyProperty 的 Java 类型，
statementType属性：填入Statement、 PreparedStatement 和 CallableStatement 中的 STATEMENT、 PREPARED 或 CALLABLE 中任一值填入 。默认值是 PREPARED。
 *      
 * @author nanshoudabaojian
 *
 */
@Mapper
public interface OrderDao {
	
	@Select("select * from miaosha_order where user_id=#{userId} and goods_id = #{goodsId}")
	public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(@Param("userId")Long userId, @Param("goodsId")Long goodsId);

	@Insert("insert into miaosha_order_info(user_id, goods_id, goods_name, goods_count, goods_price,order_channel, status, create_date)"
			+ "values(#{userId}, #{goodsId}, #{goodsName}, #{goodsCount}, #{goodsPrice}, #{orderChannel}, #{status}, #{createDate})")
	@SelectKey(keyColumn="id", keyProperty="id", resultType=long.class, before=false, statement="select last_insert_id()")
	public long insert(OrderInfo orderInfo);

	@Insert("insert into miaosha_order(user_id,goods_id,order_id) values(#{userId}, #{goodsId},#{orderId})")
	public long insertMiaoshaOrder(MiaoshaOrder miaoshaOrder);

	@Select("select * from miaosha_order_info where id = #{orderId}")
	public OrderInfo getOrderById(@Param("orderId")Long orderId);
}
