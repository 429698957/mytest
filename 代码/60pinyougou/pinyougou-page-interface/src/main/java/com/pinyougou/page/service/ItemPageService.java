package com.pinyougou.page.service;

public interface ItemPageService  {
    /**
     *
     * 生成商品详细页
     * @param id
     */
    void genItemHtml(Long id);

    public void deleteById(Long[] goodsId);
}
