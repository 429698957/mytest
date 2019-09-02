package com.pinyougou.seckill.controller;


import com.alibaba.dubbo.config.annotation.Reference;

import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.service.SeckillOrderService;
import com.pinyougou.service.WeixinPayService;
import entity.Result;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class payController {

@Reference
private WeixinPayService weixinPayService;
@Reference
private SeckillOrderService seckillOrderService;


    @RequestMapping("/createNative")
    public Map<String,String> createNative(){
        //1.获取用户的ID
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        //2.从redis获取预订单，获取预订单的金额 和 支付订单号
        TbSeckillOrder order = (TbSeckillOrder) seckillOrderService.getUserOrderStatus(userId);

        if (order != null) {
            double v = order.getMoney().doubleValue() * 100;
            long x = (long) v;
            return weixinPayService.createNative(order.getId() + "", x + "");
        }
        return new HashMap();


    }
    @RequestMapping("/queryStatus")
    public Result queryStatus(String out_trade_no){

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        Result result = new Result(false,"支付失败");

        int count = 0;
        while (true){
            Map<String,String> resultMap = weixinPayService.queryStatus(out_trade_no);

            count++;

            if(count>=100){
                result = new Result(false,"支付超时");



                Map<String,String> resultMap2 = weixinPayService.closePay(out_trade_no);

                if ("SUCCESS".equals(resultMap2.get("result_code"))) {
                    //关闭微信订单
                    //删除订单
                    //1.回复redis中的商品库存

                    //2.删除预订单

                    //3.关闭微信支付订单

                    //4.回复队列
                    seckillOrderService.deleteOrder(userId);

                }else if("ORDERPAID".equals(resultMap2.get("error_code"))){
                    seckillOrderService.updateOrderStatus(userId,resultMap.get("transaction_id"));
                }else{


                }
                break;
            }

            if (resultMap==null) {
                result = new Result(false,"支付失败");
                break;
            }
           if("SUCCESS".equals(resultMap.get("trade_state"))){
               //支付成功
               result = new Result(true,"支付成功");
               //修改商品的订单状态
               //支付日志的订单状态
               //删除redis的日志
            //  orderService.updateStatus(out_trade_no,resultMap.get("transaction_id"));
                seckillOrderService.updateOrderStatus(userId,resultMap.get("transaction_id"));



               break;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
         return result;
    }

}
