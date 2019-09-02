package com.pinyougou.cart.service;

import entity.Cart;

import java.util.List;

public interface cartService {

    /**
     * 向已有的购物车中添加商品 返回一个最新的购物车列表
     *
     *
     *
     * @param cookieList 已有的购物车列表
     * @param itemId 要添加的商品ID
     * @param num  要购买的数量
     * @return
     */
    List<Cart> addGoodsToCartList(List<Cart> cookieList, Long itemId, Integer num);

    /**
     * 存储购物车到Redis中
     * @param name 用户名
     * @param newestList 最新的购物车数据
     */
    void saveToRedis(String name, List<Cart> newestList);

    /**
     *
     * @param name 用户名
     * @return 该用户名对应的购物车数据
     */
    List<Cart> getCartListFromRedis(String name);

    /**
     *返回一个合并后的购物车数据 存入redis，并清空cookie
     *
     * @param cookiecartList cookie中的购物车数据
     * @param redisList redis中的购物车数据
     * @return
     */
    List<Cart> mergeCartList(List<Cart> cookiecartList, List<Cart> redisList);
}
