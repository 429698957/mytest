package com.pinyougou.order.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.core.service.CoreService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbPayLog;

/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface OrderService extends CoreService<TbOrder> {
	
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	 PageInfo<TbOrder> findPage(Integer pageNo, Integer pageSize);
	
	

	/**
	 * 分页
	 * @param pageNo 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	PageInfo<TbOrder> findPage(Integer pageNo, Integer pageSize, TbOrder Order);

	/**
	 * 根据用户的	ID 获取该用户在redis中的支付日志
	 *
	 * @param userId
	 * @return
	 */
	TbPayLog getPayLogByUserId(String userId);

	/**
	 * 更新状态
	 * 1.根据订单号 查询支付日志对象 更新状态
	 * 2.根据支付日志获取到商品的订单列表 更新订单状态
	 * 3.根据支付日志 获取到user_id 删除redis中的日志
	 *
	 * @param out_trade_no
	 */
	void updateStatus(String out_trade_no,String transaction_id);
}
