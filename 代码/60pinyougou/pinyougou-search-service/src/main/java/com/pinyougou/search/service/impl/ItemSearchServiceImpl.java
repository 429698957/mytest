package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.ItemSearchService;
import com.pinyougou.search.dao.ItemDao;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ItemDao itemDao;


    @Override
    public Map<String, Object> search(Map<String, Object> searchMap) {
        Map<String, Object> resultMap = new HashMap<>();

        //1.获取关键字
        String keyword = (String) searchMap.get("keyword");
        //2.创建查询对象的构建对象
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //3.设置查询的条件
       /* queryBuilder.withIndices("pinyougou"); //指定索引
        queryBuilder.withTypes("item");//指定类型，默认 查询所有的类型
        queryBuilder.withQuery(QueryBuilders.matchQuery("keyword",keyword));*/
        queryBuilder.withQuery(QueryBuilders.multiMatchQuery(keyword, "seller", "category", "brand", "title"));

        //设置一个聚合查询的条件  1.设置聚合查询的名称（ 别名） 2.设置分组的字段
        queryBuilder.addAggregation(AggregationBuilders.terms("category_group").field("category").size(50));


        //3.1设置高亮显示的域（字段） 设置前缀和后缀

        queryBuilder
                .withHighlightFields(new HighlightBuilder.Field("title"))
                .withHighlightBuilder(new HighlightBuilder().preTags("<em style=\"color:red\">").postTags("</em>"));

        //3.2过滤查询 --商品分类的过滤查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();


        String category = (String) searchMap.get("category");
        if (StringUtils.isNotBlank(category)) {

            boolQueryBuilder.filter(QueryBuilders.termQuery("category", category));
        }

        //3.3过滤查询 ---商品的品牌的过滤查询
        String brand = (String) searchMap.get("brand");
        if (StringUtils.isNotBlank(brand)) {

            boolQueryBuilder.filter(QueryBuilders.termQuery("brand", brand));
        }

        //3.4规格过滤 ---规格的过滤查询 获取到规格的名称
        Map<String,String> spec = (Map<String, String>) searchMap.get("spec");//{'网络':'移动4G'，'机身内存':'16G'}
        if (spec!=null) {

            for (String key : spec.keySet()) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("specMap."+key+".keyword", spec.get(key)));

            }
        }

        //3.5 过滤查询 ---价格区间的过滤查询 范围查询

        String price = (String) searchMap.get("price");

        if (StringUtils.isNotBlank(price)) {
            String[] split = price.split("-");
            if (split[1]=="*") {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(split[0]));

            }else {

                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").from(split[0],true).to(split[1],true));

            }

        }


        queryBuilder.withFilter(boolQueryBuilder);

        //4.构建查询对象
        NativeSearchQuery query = queryBuilder.build();

        //分页条件设置
        Integer pageNo = (Integer) searchMap.get("pageNo");
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (pageNo==null) {
            pageNo=1;
        }
        if (pageSize==null) {
            pageSize=10;
        }

        Pageable pageable= PageRequest.of(pageNo-1, pageSize);
        query.setPageable(pageable);

        //设置排序
        String sortField = (String) searchMap.get("sortField");
        String sortType = (String) searchMap.get("sortType");

        if (StringUtils.isNotBlank(sortField)&&StringUtils.isNotBlank(sortType)){
             if(sortType.equals("ASC")){
                 Sort sort = new Sort(Sort.Direction.ASC,"price");
                 query.addSort(sort);
             }else if (sortType.equals("DESC")){
                 Sort sort = new Sort(Sort.Direction.DESC,"price");
                 query.addSort(sort);

             }else {

             }
        }

        //5.执行查询
        AggregatedPage<TbItem> tbItems = elasticsearchTemplate.queryForPage(query, TbItem.class, new SearchResultMapper() {


            //自定义结果集的映射 //获取高亮
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                //创建当前页的集合

                List<T> content = new ArrayList<>();

                //获取查询的结果 获取总记录数
                SearchHits hits = response.getHits();


                //判断是否有记录，如果没有 直接返回

                if (hits == null || hits.getTotalHits() <= 0) {
                    return new AggregatedPageImpl(content);
                }

                //有记录 获取高亮的数据
                for (SearchHit hit : hits) {
                    Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                    HighlightField highlightField = highlightFields.get("title");
                    if (highlightField != null) {
                        Text[] fragments = highlightField.getFragments();
                        StringBuffer stringBuffer = new StringBuffer();
                        if (fragments != null && fragments.length > 0) {
                            for (Text fragment : fragments) {
                                String string = fragment.string();//<em style=\"color:red\">aaaa...</em>
                                stringBuffer.append(string);
                            }
                            String sourceAsString = hit.getSourceAsString();//btitem的数据JSON格式
                            TbItem tbItem = JSON.parseObject(sourceAsString, TbItem.class);
                            tbItem.setTitle(stringBuffer.toString());//有高亮
                            content.add((T) tbItem);
                        } else {
                            //设置值

                        }
                    } else {
                        //没有高亮的数据
                        String sourceAsString = hit.getSourceAsString();//btitem的数据JSON格式
                        TbItem tbItem = JSON.parseObject(sourceAsString, TbItem.class);
                        content.add((T) tbItem);
                    }
                }

                return new AggregatedPageImpl<T>(content, pageable, hits.getTotalHits(), response.getAggregations(), response.getScrollId());
            }
        });
        //6.获取结果集 获取分组的结果
        Aggregation category_group = tbItems.getAggregation("category_group");
        StringTerms stringTerms = (StringTerms) category_group;
        List<String> categoryList = new ArrayList<>();

        if (stringTerms != null) {
            List<StringTerms.Bucket> buckets = stringTerms.getBuckets();
            for (StringTerms.Bucket bucket : buckets) {
                categoryList.add(bucket.getKeyAsString());//分类的名称
            }
        }

        //搜索之后 默认 展示第一个商品分类的品牌和规格的列表

        //判断商品分类是否为空，如果不为空 根据点击到的商品进行分类查询 该分类下所有的品牌和规格列表
        //否则 查询默认的商品分类下的品牌和规格列表
        if (StringUtils.isNotBlank(category)) {
            Map map= searchBrandAndSpecList(category);
            resultMap.putAll(map);
        }else{
            if (categoryList!=null&&categoryList.size()>0) {
                Map map= searchBrandAndSpecList(categoryList.get(0));
                resultMap.putAll(map);

            }else{
                resultMap.put("specList", new HashMap<>());
                resultMap.put("brandList", new HashMap<>());
            }
        }


        //7.设置结果集到map 返回(总页数，总记录数 当前页的集合。。。。。。。。。。。）
        resultMap.put("total", tbItems.getTotalElements());
        resultMap.put("rows", tbItems.getContent());//当前页的集合
        resultMap.put("totalPages", tbItems.getTotalPages());//总页数
        resultMap.put("categoryList", categoryList);


        return resultMap;
    }

    @Override
    public void updateIndex(List<TbItem> tbItemList) {

        for (TbItem tbItem : tbItemList) {
            String spec = tbItem.getSpec();
            Map map = JSON.parseObject(spec, Map.class);
            tbItem.setSpecMap(map);
        }

        itemDao.saveAll(tbItemList);



    }

    @Override
    public void deleteByIds(Long[] ids) {
        DeleteQuery query = new DeleteQuery();
        query.setQuery(QueryBuilders.termsQuery("goodsId",ids));
        elasticsearchTemplate.delete(query,TbItem.class);
    }

    @Autowired
    private RedisTemplate redisTemplate;
    //根据分类的名称获取分类下的品牌列表和规格列表
    private Map searchBrandAndSpecList(String category) {
        //1.集成redis

       //2.注入redisTemplate

        //3.获取分类的名称对应的模板的ID
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        //4.根据模板的ID获取品牌和规格的列表
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
        //5.存入MAP中并返回
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("specList", specList);
        stringObjectHashMap.put("brandList", brandList);
        return stringObjectHashMap;
    }
}
