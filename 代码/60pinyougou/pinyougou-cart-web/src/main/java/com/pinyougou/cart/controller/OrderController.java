package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;


import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Reference
    private OrderService orderService;

    @RequestMapping("/submitOrder")
    public Result submitOrder(@RequestBody TbOrder order){
        try {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            order.setUserId(name);
            orderService.add(order);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }


    }
}
