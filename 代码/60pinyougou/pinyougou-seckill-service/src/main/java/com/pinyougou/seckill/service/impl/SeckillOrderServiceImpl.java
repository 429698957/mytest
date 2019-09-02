package com.pinyougou.seckill.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.pinyougou.common.pojo.SysConstants;

import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.pojo.SeckillStatus;
import com.pinyougou.seckill.thread.OrderHandler;
import com.pinyougou.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo; 									  
import org.apache.commons.lang3.StringUtils;
import com.pinyougou.core.service.CoreServiceImpl;

import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillOrder;  




/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl extends CoreServiceImpl<TbSeckillOrder>  implements SeckillOrderService {

	
	private TbSeckillOrderMapper seckillOrderMapper;

	@Autowired
	public SeckillOrderServiceImpl(TbSeckillOrderMapper seckillOrderMapper) {
		super(seckillOrderMapper, TbSeckillOrder.class);
		this.seckillOrderMapper=seckillOrderMapper;
	}

	
	

	
	@Override
    public PageInfo<TbSeckillOrder> findPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo,pageSize);
        List<TbSeckillOrder> all = seckillOrderMapper.selectAll();
        PageInfo<TbSeckillOrder> info = new PageInfo<TbSeckillOrder>(all);

        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbSeckillOrder> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }

	
	

	 @Override
    public PageInfo<TbSeckillOrder> findPage(Integer pageNo, Integer pageSize, TbSeckillOrder seckillOrder) {
        PageHelper.startPage(pageNo,pageSize);

        Example example = new Example(TbSeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();

        if(seckillOrder!=null){			
						if(StringUtils.isNotBlank(seckillOrder.getUserId())){
				criteria.andLike("userId","%"+seckillOrder.getUserId()+"%");
				//criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(StringUtils.isNotBlank(seckillOrder.getSellerId())){
				criteria.andLike("sellerId","%"+seckillOrder.getSellerId()+"%");
				//criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(StringUtils.isNotBlank(seckillOrder.getStatus())){
				criteria.andLike("status","%"+seckillOrder.getStatus()+"%");
				//criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(StringUtils.isNotBlank(seckillOrder.getReceiverAddress())){
				criteria.andLike("receiverAddress","%"+seckillOrder.getReceiverAddress()+"%");
				//criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(StringUtils.isNotBlank(seckillOrder.getReceiverMobile())){
				criteria.andLike("receiverMobile","%"+seckillOrder.getReceiverMobile()+"%");
				//criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(StringUtils.isNotBlank(seckillOrder.getReceiver())){
				criteria.andLike("receiver","%"+seckillOrder.getReceiver()+"%");
				//criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(StringUtils.isNotBlank(seckillOrder.getTransactionId())){
				criteria.andLike("transactionId","%"+seckillOrder.getTransactionId()+"%");
				//criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
        List<TbSeckillOrder> all = seckillOrderMapper.selectByExample(example);
        PageInfo<TbSeckillOrder> info = new PageInfo<TbSeckillOrder>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbSeckillOrder> pageInfo = JSON.parseObject(s, PageInfo.class);

        return pageInfo;
    }

     @Autowired
	 private RedisTemplate redisTemplate;

	@Autowired
	private TbSeckillGoodsMapper tbSeckillGoodsMapper;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private OrderHandler orderHandler;

	@Override
	public void submitOrder(Long id, String userId) {
		//1.根据ID 从redis中获取秒杀订单
		TbSeckillGoods tbSeckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).get(id);
	/*	if (tbSeckillGoods==null||tbSeckillGoods.getStockCount()<=0) {
			//2.判断商品是否已经售罄，如果卖完了 抛出异常
			throw new RuntimeException("卖完了");

		}*/
     //先判断用户是否已经在排队中 如果在 提示已经在排队中

		Object o2 = redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).get(userId);
		if (o2!=null) {
			throw new RuntimeException("您正在排队中");
		}


		//先判断 是否有未支付的订单
		Object o1 = redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).get(userId);
		if (o1!=null) {
			throw new RuntimeException("有未支付的订单");
		}


		Object o = redisTemplate.boundListOps(SysConstants.SEC_KILL_GOODS_PREFIX + id).rightPop();
		if (o==null) {
			throw new RuntimeException("卖完了");
		}

		redisTemplate.boundListOps(SysConstants.SEC_KILL_USER_ORDER_LIST).leftPush(new SeckillStatus(userId,id,SeckillStatus.SECKILL_queuing));


		//此时 应该 表示用户只能进入队列中，不能保证订单一定创建成功

		//标记 用户已经进入排队中
		redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).put(userId,id);



		orderHandler.handlerOrder();

	}

	@Override
	public TbSeckillOrder getUserOrderStatus(String userId) {
		return (TbSeckillOrder) redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).get(userId);
	}

    @Override
    public void updateOrderStatus(String userId, String transaction_id) {
        //1.根据用户的ID获取订单
		TbSeckillOrder tbSeckillOrder = getUserOrderStatus(userId);
		if (tbSeckillOrder!=null) {
			//2.更新订单的数据
			tbSeckillOrder.setStatus("1");
			tbSeckillOrder.setPayTime(new Date());
			tbSeckillOrder.setTransactionId(transaction_id);//微信支付生成的支付流水号
			//3.更新到数据库中
			seckillOrderMapper.insert(tbSeckillOrder);
			//4.删除预订单
			redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).delete(userId);
		}

    }
    @Autowired
	private TbSeckillGoodsMapper tbSeckillGoodsMapper;

	@Override
	public void deleteOrder(String userId) {
		TbSeckillOrder tbSeckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).get(userId);
		//删除订单
		//1.回复redis中的商品库存
		Long seckillId = tbSeckillOrder.getSeckillId();
		TbSeckillGoods tbSeckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).get(seckillId);

		if (tbSeckillGoods==null) {
            //从数据库中获取秒杀商品 此时的秒杀商品的剩余库存是0
			TbSeckillGoods tbSeckillGoods1 = tbSeckillGoodsMapper.selectByPrimaryKey(seckillId);
			tbSeckillGoods1.setStockCount(1);
			redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).put(seckillId,tbSeckillGoods1);
			//更新会数据库
			tbSeckillGoodsMapper.updateByPrimaryKey(tbSeckillGoods1);
		}else{
			tbSeckillGoods.setStockCount(tbSeckillGoods.getStockCount()+1);
			redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).put(seckillId,tbSeckillGoods);
		}

		//2.删除预订单
		redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).delete(userId);

		//4.回复队列
		redisTemplate.boundListOps(SysConstants.SEC_KILL_GOODS_PREFIX + seckillId).leftPush(seckillId);

		//3.关闭微信支付订单



	}

}
