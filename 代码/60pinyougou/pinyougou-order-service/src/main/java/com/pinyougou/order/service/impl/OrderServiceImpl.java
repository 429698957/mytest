package com.pinyougou.order.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import entity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import com.pinyougou.core.service.CoreServiceImpl;

import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.pojo.TbOrder;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl extends CoreServiceImpl<TbOrder> implements OrderService {


	private TbOrderMapper orderMapper;

	@Autowired
	public OrderServiceImpl(TbOrderMapper orderMapper) {
		super(orderMapper, TbOrder.class);
		this.orderMapper=orderMapper;
	}

	
	

	
	@Override
    public PageInfo<TbOrder> findPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo,pageSize);
        List<TbOrder> all = orderMapper.selectAll();
        PageInfo<TbOrder> info = new PageInfo<TbOrder>(all);

        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbOrder> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }

	
	

	 @Override
    public PageInfo<TbOrder> findPage(Integer pageNo, Integer pageSize, TbOrder order) {
        PageHelper.startPage(pageNo,pageSize);

        Example example = new Example(TbOrder.class);
        Example.Criteria criteria = example.createCriteria();

        if(order!=null){			
						if(StringUtils.isNotBlank(order.getPaymentType())){
				criteria.andLike("paymentType","%"+order.getPaymentType()+"%");
				//criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(StringUtils.isNotBlank(order.getPostFee())){
				criteria.andLike("postFee","%"+order.getPostFee()+"%");
				//criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(StringUtils.isNotBlank(order.getStatus())){
				criteria.andLike("status","%"+order.getStatus()+"%");
				//criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(StringUtils.isNotBlank(order.getShippingName())){
				criteria.andLike("shippingName","%"+order.getShippingName()+"%");
				//criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(StringUtils.isNotBlank(order.getShippingCode())){
				criteria.andLike("shippingCode","%"+order.getShippingCode()+"%");
				//criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(StringUtils.isNotBlank(order.getUserId())){
				criteria.andLike("userId","%"+order.getUserId()+"%");
				//criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(StringUtils.isNotBlank(order.getBuyerMessage())){
				criteria.andLike("buyerMessage","%"+order.getBuyerMessage()+"%");
				//criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(StringUtils.isNotBlank(order.getBuyerNick())){
				criteria.andLike("buyerNick","%"+order.getBuyerNick()+"%");
				//criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(StringUtils.isNotBlank(order.getBuyerRate())){
				criteria.andLike("buyerRate","%"+order.getBuyerRate()+"%");
				//criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(StringUtils.isNotBlank(order.getReceiverAreaName())){
				criteria.andLike("receiverAreaName","%"+order.getReceiverAreaName()+"%");
				//criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(StringUtils.isNotBlank(order.getReceiverMobile())){
				criteria.andLike("receiverMobile","%"+order.getReceiverMobile()+"%");
				//criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(StringUtils.isNotBlank(order.getReceiverZipCode())){
				criteria.andLike("receiverZipCode","%"+order.getReceiverZipCode()+"%");
				//criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(StringUtils.isNotBlank(order.getReceiver())){
				criteria.andLike("receiver","%"+order.getReceiver()+"%");
				//criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(StringUtils.isNotBlank(order.getInvoiceType())){
				criteria.andLike("invoiceType","%"+order.getInvoiceType()+"%");
				//criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(StringUtils.isNotBlank(order.getSourceType())){
				criteria.andLike("sourceType","%"+order.getSourceType()+"%");
				//criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(StringUtils.isNotBlank(order.getSellerId())){
				criteria.andLike("sellerId","%"+order.getSellerId()+"%");
				//criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
        List<TbOrder> all = orderMapper.selectByExample(example);
        PageInfo<TbOrder> info = new PageInfo<TbOrder>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbOrder> pageInfo = JSON.parseObject(s, PageInfo.class);

        return pageInfo;
    }

	@Override
	public TbPayLog getPayLogByUserId(String userId) {
		return  (TbPayLog)  redisTemplate.boundHashOps("TbPayLog").get(userId);
	}

	@Override
	public void updateStatus(String out_trade_no,String transaction_id) {
                // 1.根据订单号 查询支付日志对象 更新状态
		TbPayLog tbPayLog = tbPayLogMapper.selectByPrimaryKey(out_trade_no);

		tbPayLog.setPayTime(new Date());
		tbPayLog.setTransactionId(transaction_id);
		tbPayLog.setTradeState("1");
		 tbPayLogMapper.updateByPrimaryKey(tbPayLog);
		// 2.根据支付日志获取到商品的订单列表 更新订单状态
		String orderList = tbPayLog.getOrderList();
		String[] split = orderList.split(",");//从日志里拿出所有订单ID
		for (String orderIdString : split) {
			TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.valueOf(orderIdString));
			tbOrder.setStatus("2");//把订单的支付状态变成2 即为已经支付
			tbOrder.setUpdateTime(new Date());
			tbOrder.setPaymentTime(tbOrder.getUpdateTime());
			orderMapper.updateByPrimaryKey(tbOrder);
		}
		// 3.根据支付日志 获取到user_id 删除redis中的日志
		redisTemplate.boundHashOps("TbPayLog").delete(tbPayLog.getUserId());
	}

	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private IdWorker idWorker;
	@Autowired
	private TbOrderItemMapper tbOrderItemMapper;
	@Autowired
	private TbPayLogMapper tbPayLogMapper;

	@Override
	public void add(TbOrder order) {
		//获取页面传递的数据

		//插入到订单中， 拆单 订单的ID 要生成
		   //获取redis中的购物车数据
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("CART_REDIS_KEY").get(order.getUserId());
		double totalMoney=0;

		List<String> orderListId = new ArrayList<>();
		//循环遍历每一个cart对象。 每一个cart对象 就是一个商家
		for (Cart cart : cartList) {
			//生成一个订单号
			long orderId = idWorker.nextId();
			orderListId.add(orderId+"");
			TbOrder tbOrder = new TbOrder();
			tbOrder.setOrderId(orderId);

            tbOrder.setPaymentType(order.getPaymentType());
            tbOrder.setPostFee("0");
            tbOrder.setStatus("1");//未付款的状态
			tbOrder.setCreateTime(new Date());
			tbOrder.setUpdateTime(tbOrder.getCreateTime());
			tbOrder.setUserId(order.getUserId());
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());
			tbOrder.setReceiverMobile(order.getReceiverMobile());
			tbOrder.setReceiverZipCode("515300");
 			tbOrder.setReceiver(order.getReceiver());
 			tbOrder.setSourceType("2");
 			tbOrder.setSellerId(cart.getSellerId());
 			double money=0;
			for (TbOrderItem orderItem : cart.getOrderItemList()) {
				money += orderItem.getTotalFee().doubleValue();

				//插入到订单选项中
				long l = idWorker.nextId();
				orderItem.setId(l);
				orderItem.setOrderId(orderId);
				tbOrderItemMapper.insert(orderItem);

			}

			tbOrder.setPayment(new BigDecimal(money));
			totalMoney+=money;

			orderMapper.insert(tbOrder);
		}
		//添加支付日志

		TbPayLog tbPayLog = new TbPayLog();
		tbPayLog.setOutTradeNo(idWorker.nextId()+"");
		tbPayLog.setCreateTime(new Date());
		long fen = (long)(totalMoney*100);
		tbPayLog.setTotalFee(fen);
		tbPayLog.setUserId(order.getUserId());
		tbPayLog.setTradeState("0");
        tbPayLog.setOrderList(orderListId.toString().replace("[","" ).replace("]","" ));
        tbPayLog.setPayType(order.getPaymentType());
        tbPayLogMapper.insert(tbPayLog);

        //存储到redis
		redisTemplate.boundHashOps("TbPayLog").put(order.getUserId(),tbPayLog);

		//移除屌redis的购物车数据
		redisTemplate.boundHashOps("CART_REDIS_KEY").delete(order.getUserId());


	}
}
