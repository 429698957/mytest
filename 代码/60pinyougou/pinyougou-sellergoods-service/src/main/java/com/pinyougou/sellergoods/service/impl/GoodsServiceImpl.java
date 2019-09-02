package com.pinyougou.sellergoods.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.core.service.CoreServiceImpl;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.Goods;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;


/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class GoodsServiceImpl extends CoreServiceImpl<TbGoods> implements GoodsService {


    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbSellerMapper sellerMapper;
    @Autowired
    private TbBrandMapper tbBrandMapper;
    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    public GoodsServiceImpl(TbGoodsMapper goodsMapper) {
        super(goodsMapper, TbGoods.class);
        this.goodsMapper = goodsMapper;
    }


    @Override
    public PageInfo<TbGoods> findPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<TbGoods> all = goodsMapper.selectAll();
        PageInfo<TbGoods> info = new PageInfo<TbGoods>(all);

        String s = JSON.toJSONString(info);
        PageInfo<TbGoods> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }


    @Override
    public PageInfo<TbGoods> findPage(Integer pageNo, Integer pageSize, TbGoods goods) {
        PageHelper.startPage(pageNo, pageSize);

        Example example = new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("isDelete", false);

        if (goods != null) {
            if (StringUtils.isNotBlank(goods.getSellerId())) {
                criteria.andLike("sellerId", "%" + goods.getSellerId() + "%");
                //criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
            }
            if (StringUtils.isNotBlank(goods.getGoodsName())) {
                criteria.andLike("goodsName", "%" + goods.getGoodsName() + "%");
                //criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
            }
            if (StringUtils.isNotBlank(goods.getAuditStatus())) {
                criteria.andLike("auditStatus", "%" + goods.getAuditStatus() + "%");
                //criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
            }
            if (StringUtils.isNotBlank(goods.getIsMarketable())) {
                criteria.andLike("isMarketable", "%" + goods.getIsMarketable() + "%");
                //criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
            }
            if (StringUtils.isNotBlank(goods.getCaption())) {
                criteria.andLike("caption", "%" + goods.getCaption() + "%");
                //criteria.andCaptionLike("%"+goods.getCaption()+"%");
            }
            if (StringUtils.isNotBlank(goods.getSmallPic())) {
                criteria.andLike("smallPic", "%" + goods.getSmallPic() + "%");
                //criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
            }
            if (StringUtils.isNotBlank(goods.getIsEnableSpec())) {
                criteria.andLike("isEnableSpec", "%" + goods.getIsEnableSpec() + "%");
                //criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
            }

        }
        List<TbGoods> all = goodsMapper.selectByExample(example);
        PageInfo<TbGoods> info = new PageInfo<TbGoods>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbGoods> pageInfo = JSON.parseObject(s, PageInfo.class);

        return pageInfo;
    }

    @Override
    public void add(Goods goods) {
        //1.获取到spu的数据
        TbGoods tbGoods = goods.getGoods();
        tbGoods.setIsDelete(false);
        goodsMapper.insert(tbGoods);

        tbGoods.setAuditStatus("0");

        TbGoodsDesc tbGoodsDesc = goods.getGoodsDesc();
        tbGoodsDesc.setGoodsId(tbGoods.getId());
        goodsDescMapper.insert(tbGoodsDesc);

        List<TbItem> itemList = goods.getItemList();

        saveItems(goods, tbGoods, tbGoodsDesc, itemList);


    }

    private void saveItems(Goods goods, TbGoods tbGoods, TbGoodsDesc tbGoodsDesc, List<TbItem> itemList) {
        if ("1".equals(goods.getGoods().getIsEnableSpec())) {
            //启用规格
            for (TbItem tbItem : itemList) {
                String spec = tbItem.getSpec();
                String title = tbGoods.getGoodsName();
                Map<String, String> map = JSON.parseObject(spec, Map.class);
                String goodsName = tbGoods.getGoodsName();
                for (String key : map.keySet()) {
                    title = " " + map.get(key);
                }
                tbItem.setTitle(title);
                //设置图片地址
                String image = JSON.parseArray(tbGoodsDesc.getItemImages(), Map.class).get(0).get("url").toString();
                tbItem.setImage(image);
                tbItem.setCategoryid(tbGoods.getCategory3Id());
                TbItemCat cat = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
                tbItem.setCategory(cat.getName());

                tbItem.setCreateTime(new Date());
                tbItem.setUpdateTime(tbItem.getCreateTime());

                tbItem.setGoodsId(tbGoods.getId());

                tbItem.setSeller(tbGoods.getSellerId());
                TbSeller tbSeller = sellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
                tbItem.setSeller(tbSeller.getNickName());
                TbBrand tbBrand = tbBrandMapper.selectByPrimaryKey(tbGoods.getBrandId());
                tbItem.setBrand(tbBrand.getName());


                itemMapper.insert(tbItem);
            }

        } else {
               //不启用规格
            TbItem tbItem = new TbItem();
            tbItem.setTitle(tbGoods.getGoodsName());
            tbItem.setPrice(tbGoods.getPrice());
            tbItem.setNum(999);
            String image = JSON.parseArray(tbGoodsDesc.getItemImages(), Map.class).get(0).get("url").toString();
            tbItem.setImage(image);


            tbItem.setCategoryid(tbGoods.getCategory3Id());

            TbItemCat cat = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
            tbItem.setCategory(cat.getName());

            tbItem.setStatus("1");//默认正常

            //设置时间
            tbItem.setCreateTime(new Date());
            tbItem.setUpdateTime(tbItem.getCreateTime());

            tbItem.setIsDefault("1");

            //设置外键
            tbItem.setGoodsId(tbGoods.getId());

            //设置商家
            tbItem.setSellerId(tbGoods.getSellerId());
            TbSeller tbSeller = sellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
            tbItem.setSeller(tbSeller.getNickName());//商家的店铺名
            TbBrand brand = tbBrandMapper.selectByPrimaryKey(tbGoods.getBrandId());
            tbItem.setBrand(brand.getName());

            tbItem.setSpec("{}");

            itemMapper.insert(tbItem);

        }
    }

    @Override
    public Goods findOne(Long id) {
        //1.创建组合对象
        Goods goods = new Goods();
        //2.获取spu的数据
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        //3.获取spu对应的描述的表的数据
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        //4.获取商品的SKU的列表数据
        TbItem tbItem = new TbItem();
        //5.组合 数据 返回
       tbItem.setGoodsId(id);
        List<TbItem> itemList = itemMapper.select(tbItem);

        goods.setGoods(tbGoods);
        goods.setGoodsDesc(tbGoodsDesc);
        goods.setItemList(itemList);
        return goods;
    }


    public void update(Goods goods) {


        TbGoods goods1 = goods.getGoods();
        goods1.setAuditStatus("0");//商家修改 新增 都需要设置为0 未审核的状态
        goodsMapper.updateByPrimaryKey(goods1);
        //2.获取描述对象信息
        TbGoodsDesc goodsDesc = goods.getGoodsDesc();
        goodsDescMapper.updateByPrimaryKey(goodsDesc);
        //3.获取SKU的列表
        List<TbItem> itemList = goods.getItemList();
        //4.分别进行更新

        //先删除
        //delete from tb_item where goods_id=1
        TbItem condition = new TbItem();
        condition.setGoodsId(goods1.getId());
        itemMapper.delete(condition);

        //判断 是否启用规格 并添加SKU到数据库中
        saveItems(goods,goods1,goodsDesc,itemList);
    }

    @Override
    public void updateStatus(Long[] ids, String status) {


        TbGoods tbGoods = new TbGoods();
        tbGoods.setAuditStatus(status);

        Example example=new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id",Arrays.asList(ids));
        goodsMapper.updateByExampleSelective(tbGoods, example);


    }

    @Override
    public void delete(Object[] ids){
        //update tb_goods set is_delete=1 where id in (1,2,3)

        TbGoods tbGoods = new TbGoods();
        tbGoods.setIsDelete(true);

        Example example=new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id",Arrays.asList(ids));
        goodsMapper.updateByExampleSelective(tbGoods, example);
    }

    @Override
    public List<TbItem> findTbitemListByIds(Long[] ids) {
        Example example = new Example(TbItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("goodsId",Arrays.asList(ids));
        criteria.andEqualTo("status","1");

        List<TbItem> tbItems = itemMapper.selectByExample(example);


        return tbItems;
    }
}
