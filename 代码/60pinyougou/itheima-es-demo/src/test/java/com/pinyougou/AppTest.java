package com.pinyougou;

import static org.junit.Assert.assertTrue;

import com.itheima.dao.ItemDao;
import com.itheima.model.TbItem;
import org.apache.lucene.search.Query;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit test for simple App.
 */

@ContextConfiguration("classpath:spring-es.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class AppTest {

    /**
     * Rigorous Test :-)
     */
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ItemDao itemDao;


    @Test
    public void shouldAnswerWithTrue() {
        elasticsearchTemplate.createIndex(TbItem.class);
        elasticsearchTemplate.putMapping(TbItem.class);

    }

    @Test
    public void addDocument() {
        //更新和添加一样，当有相同的ID 时直接就更新


        TbItem tbItem = new TbItem();
        tbItem.setId(100L);
        tbItem.setGoodsId(1000L);
        tbItem.setTitle("华为手机");
        tbItem.setCategory("手机");
        tbItem.setBrand("华为");
        tbItem.setSeller("华为旗舰店");
        Map<String, String> specMap = new HashMap<>();
        specMap.put("网络制式", "移动4G");
        specMap.put("机身内存", "16G");
        tbItem.setSpecMap(specMap);
        itemDao.save(tbItem);


    }

    @Test
    public void deleteById() {


        itemDao.deleteById(1000L);


    }

    @Test
    public void findAll() {


        Iterable<TbItem> all = itemDao.findAll();
        for (TbItem tbItem : all) {
            System.out.println(tbItem.getTitle());
        }


    }

    @Test
    public void QueryById() {
        System.out.println(itemDao.findById(98L).get().getTitle());
    }


    @Test
    public void queryByPageable() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<TbItem> page = itemDao.findAll(pageable);

        System.out.println("获取总记录数" + page.getTotalElements());
        //总页数
        System.out.println("总页数" + page.getTotalPages());

        List<TbItem> content = page.getContent();
        for (TbItem tbItem : content) {
            System.out.println(tbItem.getTitle());
        }
    }

    @Test
    //spring date elasticasearch 中的elasticasearchTemplate对象的查询方法
    public void queryBywildcardQuery() {

        //1.创建一个查询对象  //2.设置查询的条件
        SearchQuery query = new NativeSearchQuery(QueryBuilders.wildcardQuery("title", "手?"));


        //3.执行查询
        AggregatedPage<TbItem> tbItems = elasticsearchTemplate.queryForPage(query, TbItem.class);

        //4.获得查询结果，处理结果
        System.out.println("获取总记录数数" + tbItems.getTotalElements());
        System.out.println("获取页数" + tbItems.getTotalPages());
        List<TbItem> content = tbItems.getContent();
        for (TbItem tbItem : content) {
            System.out.println(tbItem.getTitle());
        }


    }

    //分词查询
    @Test
    public void matchQuery() {
        SearchQuery query = new NativeSearchQuery(QueryBuilders.matchQuery("title", "三星手"));
        AggregatedPage<TbItem> tbItems = elasticsearchTemplate.queryForPage(query, TbItem.class);


        System.out.println("获取总记录数数" + tbItems.getTotalElements());
        System.out.println("获取页数" + tbItems.getTotalPages());
        List<TbItem> content = tbItems.getContent();
        for (TbItem tbItem : content) {
            System.out.println(tbItem.getTitle());
        }


    }


    //组合域查询
    @Test
    public void matchZuheQuery() {
        SearchQuery query = new NativeSearchQuery(QueryBuilders.matchQuery("keyword", "手机"));
        AggregatedPage<TbItem> tbItems = elasticsearchTemplate.queryForPage(query, TbItem.class);


        System.out.println("获取总记录数数" + tbItems.getTotalElements());
        System.out.println("获取页数" + tbItems.getTotalPages());
        List<TbItem> content = tbItems.getContent();
        for (TbItem tbItem : content) {
            System.out.println(tbItem.getTitle());
        }


    }

    //对象域查询
    @Test
    public void queryByObject() {
        SearchQuery query = new NativeSearchQuery(QueryBuilders.matchQuery("specMap.网络制式.keyword", "移动4G"));
        AggregatedPage<TbItem> tbItems = elasticsearchTemplate.queryForPage(query, TbItem.class);
        System.out.println("获取总记录数数" + tbItems.getTotalElements());
        System.out.println("获取页数" + tbItems.getTotalPages());
        List<TbItem> content = tbItems.getContent();
        for (TbItem tbItem : content) {
            System.out.println(tbItem.getTitle());

        }
    }

    //多条件组合查询
    @Test
    public void booleanquery(){
         //1.创建查询对象的构建对象
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();


        //2. 设置查询的条件
        queryBuilder.withIndices("pinyougou");
        queryBuilder.withTypes("item");
        queryBuilder.withQuery(QueryBuilders.matchQuery("title","华为手机"));//设置查询条件

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();//组合多个条件
        //组合查询中 如果都是must 可以使用filter查询，性能比must要高
        boolQuery.filter(QueryBuilders.termQuery("specMap.网络制式.keyword","移动4G"));
        boolQuery.filter(QueryBuilders.termQuery("specMap.机身内存.keyword","16Gx"));
        queryBuilder.withFilter(boolQuery);
        //3.创建查询对象

        SearchQuery query = queryBuilder.build();
        //4.执行查询
        AggregatedPage<TbItem> tbItems = elasticsearchTemplate.queryForPage(query, TbItem.class);
        System.out.println("获取总记录数数" + tbItems.getTotalElements());
        System.out.println("获取页数" + tbItems.getTotalPages());
        List<TbItem> content = tbItems.getContent();
        for (TbItem tbItem : content) {
            System.out.println(tbItem.getTitle());

        }
    }
}