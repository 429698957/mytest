package com.pinyougou.service;

import java.util.Map;

public interface WeixinPayService {

    /**
     *
     *调用统一下单API 发送请求给支付系统 获取支付的结果（二维码链接）
     * @param out_trade_no 支付订单号
     * @param total_fee 订单的总金额
     * @return
     */
    Map<String, String> createNative(String out_trade_no, String total_fee);


    /**
     * 根据支付订单号 查询该支付订单号的支付的状态 返回map
     * @param out_trade_no
     * @return
     */
    Map<String, String> queryStatus(String out_trade_no);

    /**
     *关闭微信订单
     *
     * @param out_trade_no
     * @return
     */
    Map<String, String> closePay(String out_trade_no);
}
