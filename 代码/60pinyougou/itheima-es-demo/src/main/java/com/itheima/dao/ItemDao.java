package com.itheima.dao;

import com.itheima.model.TbItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
                                                           //主键类型
public interface ItemDao extends ElasticsearchRepository<TbItem,Long> {


}
