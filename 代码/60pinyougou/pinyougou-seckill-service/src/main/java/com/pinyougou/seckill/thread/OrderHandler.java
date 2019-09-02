package com.pinyougou.seckill.thread;

import com.pinyougou.common.pojo.SysConstants;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.pojo.SeckillStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;


public class OrderHandler {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbSeckillGoodsMapper tbSeckillGoodsMapper;
    @Autowired
    private IdWorker idWorker;

    @Async //启用异步调用
    public void handlerOrder(){

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //1.从队列中获取元素
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps(SysConstants.SEC_KILL_USER_ORDER_LIST).rightPop();

        if (seckillStatus != null) {
            //2.获取userID和秒杀商品的ID
            TbSeckillGoods tbSeckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).get(seckillStatus.getGoodsId());
            //3.-库存 再判断库存是否为0 如果卖完了 抛出异常
            tbSeckillGoods.setStockCount(tbSeckillGoods.getStockCount() - 1);
            redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).put(tbSeckillGoods.getGoodsId(), tbSeckillGoods);
            if (tbSeckillGoods.getStockCount() == 0) {
                //4.如果库存为0  更新到数据库中
                tbSeckillGoodsMapper.updateByPrimaryKeySelective(tbSeckillGoods);
                redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).delete(seckillStatus.getGoodsId());
            }
            //5.创建一个预订单到redis中
            TbSeckillOrder tbSeckillOrder = new TbSeckillOrder();
            tbSeckillOrder.setId(idWorker.nextId());
            tbSeckillOrder.setSeckillId(tbSeckillGoods.getId());
            tbSeckillOrder.setMoney(tbSeckillGoods.getCostPrice());
            tbSeckillOrder.setSellerId(tbSeckillGoods.getSellerId());
            tbSeckillOrder.setCreateTime(new Date());
            tbSeckillOrder.setStatus("0");

            redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).put(seckillStatus.getUserId(), tbSeckillOrder);


            redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).delete(seckillStatus.getUserId());



        }

    }


}
