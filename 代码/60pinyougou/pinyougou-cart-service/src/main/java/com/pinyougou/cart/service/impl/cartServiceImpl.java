package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.cartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import entity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
public class cartServiceImpl implements cartService {
    @Autowired
    private TbItemMapper tbItemMapper;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cookieList, Long itemId, Integer num) {
        //1.根据商品的ID获取商品的对象
        TbItem tbItem = tbItemMapper.selectByPrimaryKey(itemId);
        //2.获取商品对象数据中所属的商家的ID
        String sellerId = tbItem.getSellerId();

        Cart cart = searchCartBySellerId(cookieList,sellerId);

        if (cart==null) {
            //3.判断 已有的购物车中是否有商家的ID 如果没有 直接添加商品即可
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(tbItem.getSeller());
            List<TbOrderItem> orderitemList = new ArrayList<>();

            TbOrderItem tborderItem = new TbOrderItem();

            tborderItem.setItemId(itemId);
            tborderItem.setGoodsId(tbItem.getGoodsId());
            tborderItem.setTitle(tbItem.getTitle());
            tborderItem.setPrice(tbItem.getPrice());
            tborderItem.setNum(num);
            double v = tbItem.getPrice().doubleValue() * num;
            tborderItem.setTotalFee(new BigDecimal(v));
            tborderItem.setPicPath(tbItem.getImage());
            tborderItem.setSellerId(sellerId);

            orderitemList.add(tborderItem);

            cart.setOrderItemList(orderitemList);
            cookieList.add(cart);
        }else {

            //4.判断 已有的购物车中 是否 有商家ID 如果有
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            TbOrderItem tbOrderItemif = searchItemByItemId(orderItemList,itemId);
            if (tbOrderItemif!=null) {
                //4.1判断 商家的购物明细中 是否有 要添加的商品 如果 有 数量相加
                    tbOrderItemif.setNum(tbOrderItemif.getNum()+num);
                double v = tbOrderItemif.getPrice().doubleValue()*tbOrderItemif.getNum();
                tbOrderItemif.setTotalFee(new BigDecimal(v));
                //当商品数量为0的时候删除
                if(tbOrderItemif.getNum()==0){
                         orderItemList.remove(tbOrderItemif);

                }
                //当商家里没商品的时候删除商家
                if (orderItemList.size()==0){

                    cookieList.remove(cart);
                }


            }else {
                //4.1判断 商家的购物明细中 是否有 要添加的商品 如果 没有 直接添加
                tbOrderItemif = new TbOrderItem();
                tbOrderItemif.setItemId(itemId);
                tbOrderItemif.setGoodsId(tbItem.getGoodsId());
                tbOrderItemif.setTitle(tbItem.getTitle());
                tbOrderItemif.setPrice(tbItem.getPrice());
                tbOrderItemif.setNum(num);
                double v = tbItem.getPrice().doubleValue() * num;
                tbOrderItemif.setTotalFee(new BigDecimal(v));
                tbOrderItemif.setPicPath(tbItem.getImage());
                tbOrderItemif.setSellerId(sellerId);

                orderItemList.add(tbOrderItemif);

            }
        }



        return cookieList;
    }


    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void saveToRedis(String name, List<Cart> newestList) {
            //key feile value
              redisTemplate.boundHashOps("CART_REDIS_KEY").put(name, newestList);

    }

    @Override
    public List<Cart> getCartListFromRedis(String name) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("CART_REDIS_KEY").get(name);
        if (cartList==null) {
            cartList=new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cookiecartList, List<Cart> redisList) {
        for (Cart cart : cookiecartList) {
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            for (TbOrderItem orderItem : orderItemList) {
                redisList = addGoodsToCartList(redisList, orderItem.getItemId(), orderItem.getNum());
            }
        }


        return redisList;
    }

    private TbOrderItem searchItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId()==itemId.longValue()) {
               return orderItem;
            }
        }
        return null;
    }

    private Cart searchCartBySellerId(List<Cart> cookieList, String sellerId) {
        for (Cart cart : cookieList) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }


        return null;
    }
}
