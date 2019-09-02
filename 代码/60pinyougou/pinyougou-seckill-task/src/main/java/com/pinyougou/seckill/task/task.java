package com.pinyougou.seckill.task;

import com.pinyougou.common.pojo.SysConstants;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class task {

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     *定时把数据库的数据 推送到redis中
     *cron:表达式用于指定何时去执行该方法
     *0/5 从第0秒开始 每隔5秒执行一次
     */


    @Scheduled(cron = "2/5 * * * * *")
    public void pushGoods(){

        //1.查询数据库
        Example example = new Example(TbSeckillGoods.class);
        Example.Criteria criteria = example.createCriteria();
        //状态为1
        criteria.andEqualTo("status", "1");
        //当前的时间再     开始时间<活动时间<结束时间以内
        Date date = new Date();
        criteria.andLessThan("startTime", date);
        criteria.andGreaterThan("endTime", date);
        //剩余库存大于0
        criteria.andGreaterThan("stockCount", 0);
        //排除redis中已有的商品的列表
        Set<Long> keys = redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).keys();
        if(keys!=null && keys.size()>0){
            criteria.andNotIn("id",keys);
        }
        List<TbSeckillGoods> tbSeckillGoods = seckillGoodsMapper.selectByExample(example);





        //2.将数据推送到redis中
        for (TbSeckillGoods tbSeckillGood : tbSeckillGoods) {
            pushGoodList(tbSeckillGood);
            redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).put(tbSeckillGood.getId(),tbSeckillGood);
        }

    }
    private void pushGoodList(TbSeckillGoods tbSeckillGood){
        for (Integer i = 0; i < tbSeckillGood.getStockCount(); i++) {
            redisTemplate.boundListOps(SysConstants.SEC_KILL_GOODS_PREFIX + tbSeckillGood.getId()).leftPush(tbSeckillGood.getId());

        }

    }
}
