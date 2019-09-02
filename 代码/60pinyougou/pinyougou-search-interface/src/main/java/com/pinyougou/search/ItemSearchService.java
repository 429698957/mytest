package com.pinyougou.search;

import com.pinyougou.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    /**
     *
     *
     * @param searchMap 页面传过来的条件 执行查询
     * @return 返回一个Map 包括所有的数据（集合，规格数据，总页数，总记录数.........）
     */
    Map<String, Object> search(Map<String, Object> searchMap);

    /**
     * 更新ES索引库
     *
     * @param tbItemList 要更新的数据
     */
    void updateIndex(List<TbItem> tbItemList);

    void deleteByIds(Long[] ids);
}
