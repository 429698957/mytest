package com.pinyougou.es.service.impl;

import com.alibaba.fastjson.JSON;
import com.pinyougou.es.dao.ItemDao;
import com.pinyougou.es.service.ImportService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class ImportServiceImpl implements ImportService {
    @Autowired
    private TbItemMapper tbItemMapper;

    @Autowired
    private ItemDao itemDao;
    @Override
    public void importDBToES() {


        //1.使用dao 根据条件查询数据库中的数据
        TbItem condition= new TbItem();
        condition.setStatus("1");//状态为正常的数据
        List<TbItem> itemList = tbItemMapper.select(condition);

        //循环遍历 获取规格属性 放入specMap{"网络"："移动4G"，"...":"..."}
        //转成json对象
        for (TbItem tbItem : itemList) {
            String spec = tbItem.getSpec();
            Map map = JSON.parseObject(spec, Map.class);
              tbItem.setSpecMap(map);
        }

        //2.使用es的dao 保存数据到es服务器中
        itemDao.saveAll(itemList);

    }
}
