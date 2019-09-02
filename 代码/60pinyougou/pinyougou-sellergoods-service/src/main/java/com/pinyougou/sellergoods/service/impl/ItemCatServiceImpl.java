package com.pinyougou.sellergoods.service.impl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo; 									  
import org.apache.commons.lang3.StringUtils;
import com.pinyougou.core.service.CoreServiceImpl;

import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.pojo.TbItemCat;  

import com.pinyougou.sellergoods.service.ItemCatService;



/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ItemCatServiceImpl extends CoreServiceImpl<TbItemCat>  implements ItemCatService {

	
	private TbItemCatMapper itemCatMapper;

	@Autowired
	public ItemCatServiceImpl(TbItemCatMapper itemCatMapper) {
		super(itemCatMapper, TbItemCat.class);
		this.itemCatMapper=itemCatMapper;
	}

	
	

	
	@Override
    public PageInfo<TbItemCat> findPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo,pageSize);
        List<TbItemCat> all = itemCatMapper.selectAll();
        PageInfo<TbItemCat> info = new PageInfo<TbItemCat>(all);

        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbItemCat> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }

	
	

	 @Override
    public PageInfo<TbItemCat> findPage(Integer pageNo, Integer pageSize, TbItemCat itemCat) {
        PageHelper.startPage(pageNo,pageSize);

        Example example = new Example(TbItemCat.class);
        Example.Criteria criteria = example.createCriteria();

        if(itemCat!=null){			
						if(StringUtils.isNotBlank(itemCat.getName())){
				criteria.andLike("name","%"+itemCat.getName()+"%");
				//criteria.andNameLike("%"+itemCat.getName()+"%");
			}
	
		}
        List<TbItemCat> all = itemCatMapper.selectByExample(example);
        PageInfo<TbItemCat> info = new PageInfo<TbItemCat>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbItemCat> pageInfo = JSON.parseObject(s, PageInfo.class);

        return pageInfo;
    }

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public List<TbItemCat> findByParentId(Long parentId) {


        //将商品分类的数据放入redis中
        //1.查询所有的商品的分类数据

        List<TbItemCat> all =  super.findAll();
        //2.存储到redis中
        for (TbItemCat tbItemCat : all) {


            redisTemplate.boundHashOps("itemCat").put(tbItemCat.getName(),tbItemCat.getTypeId());

        }

        TbItemCat itemcat = new TbItemCat();
        itemcat.setParentId(parentId);

        return itemCatMapper.select(itemcat);
    }



    @Autowired
    private TbItemCatMapper tbItemCatMapper;

    /**
     * 首页分类查询 放入redis减轻mysql的查询压力
     * @param parentId 获取1级目录的父ID
     * @return
     */
    @Override
    public List<Map> getAll(Long parentId) {
        //根据传入的父ID来查询1级目录pojo
        List<Map> totalList= new ArrayList<>();
        TbItemCat tbItemCat = new TbItemCat();
        tbItemCat.setParentId(parentId);
        List<TbItemCat> parentList = tbItemCatMapper.select(tbItemCat);
        //创建一个map来存放1,2,3级目录的pojo

        for (TbItemCat itemCat : parentList) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("parent", itemCat);
            Long id = itemCat.getId();

            tbItemCat.setParentId(id);
            List<TbItemCat> secondList = tbItemCatMapper.select(tbItemCat);




        }
        return totalList;

    }

    /**
     * 根据传入的上一级目录的ID查询下一级的目录分类
     *
     * @param list 上一级的目录的pojo集合
     * @return
     */
   private List<TbItemCat> searchByParentId(List<TbItemCat> list){
       //创建一个上级目录的所有ID的集合
       ArrayList<Long> ids = new ArrayList<>();
       //创建一个下级目录的集合
       List<TbItemCat> newList = new ArrayList<>();
       for (TbItemCat tbItemCat : list) {
           //list是上一级目录的pojo集合，遍历后把每一个1级目录的ID放入集合中
           Long parentId = tbItemCat.getId();
           ids.add(parentId);
       }
       for (Long parentId : ids) {
           //遍历上一级的所有ID，作为下一级目录的父ID查询
           TbItemCat tbItemCat1 = new TbItemCat();
           tbItemCat1.setParentId(parentId);
           List<TbItemCat> select = tbItemCatMapper.select(tbItemCat1);
           for (TbItemCat tbItemCat2 : select) {
               newList.add(tbItemCat2);
           }

       }


      return newList;

   }
}
