package com.pinyougou.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.service.WeixinPayService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pay")
public class payController {

@Reference
private WeixinPayService weixinPayService;
    @Reference
    private OrderService orderService;

    @RequestMapping("/createNative")
    public Map<String,String> createNative(){
        //1.获取用户的ID
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog tbPayLog = orderService.getPayLogByUserId(userId);
        //2.调用orderService的服务 从redis获取支付日志对象

     /*   //1.生成一个支付订单
        String out_trade_no = new IdWorker().nextId() + "";
        //2.获取商品订单的总金额
         String total_fee="1"; //单位是分*/


        //3.调用服务（内部实现统一调用下单api）
          return weixinPayService.createNative(tbPayLog.getOutTradeNo(),tbPayLog.getTotalFee()+"");


    }
    @RequestMapping("/queryStatus")
    public Result queryStatus(String out_trade_no){
            //直接轮休查询调用 pay-service的接口方法，查询该out_trade_no对应的支付状态 返回数据
        Result result = new Result(false,"支付失败");

        //设置一个超时时间
        int count = 0;
        while (true){
            Map<String,String> resultMap = weixinPayService.queryStatus(out_trade_no);

            count++;

            if(count>=100){
                result = new Result(false,"支付超时");
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
               orderService.updateStatus(out_trade_no,resultMap.get("transaction_id"));




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
