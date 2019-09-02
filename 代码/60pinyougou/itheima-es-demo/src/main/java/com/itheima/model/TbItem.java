package com.itheima.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Map;

@Document(indexName = "pinyougou",type = "item")
public class TbItem implements Serializable {

    /**
     * @Document(indexName = "pinyougou",type="item")
     * - @Document 标识一个文档
     * - indexName 指定索引名
     * - type 指定类型
     * @Id 用于设置文档ID 可以设置在数据的主键字段上
     * @Field(index = true, analyzer = “ik_smart”, store = true, searchAnalyzer = “ik_smart”, type = FieldType.Text)
     * @field 用于标识字段
     * index: 是否索引 默认为true
     * analyzer ：索引时的分词器
     * searchAnalyzer ：搜索时的分词器
     * store:是否存储，默认是false,但是数据存储在了ES的_store中了。
     * type:指定该字段的类型 比如：文本类型，数据Long 类型 ,对象类型 （默认不用声明）
     */

    @Id//文档的唯一标识
    @Field(type = FieldType.Long)
    private Long id;


    // 表示是一个对象类型
    @Field(type = FieldType.Object)
    private Map<String,String> specMap;


    /**
     * 商品标题
     * index 是否索引，analyzer 分词 type 类型为文本 String copyTo
     */

    @Field(index = true,analyzer = "ik_smart",type = FieldType.Text,copyTo = "keyword")
    private String title;


    @Field(type = FieldType.Long)
    private Long goodsId;

    /**
     * 冗余字段 存放三级分类名称  关键字 只能按照确切的词来搜索 FieldType.Keyword指不分词 并且只做全词匹配
     */
    @Field(index = true,type = FieldType.Keyword,copyTo = "keyword")
    private String category;

    /**
     * 冗余字段 存放品牌名称 不需分词 需求同上
     */
    @Field(index = true,type = FieldType.Keyword,copyTo = "keyword")
    private String brand;

    /**
     * 冗余字段，用于存放商家的店铺名称
     */
    @Field(index = true,type = FieldType.Keyword,copyTo = "keyword")
    private String seller;
    //getter和setter


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public Map<String, String> getSpecMap() {
        return specMap;
    }

    public void setSpecMap(Map<String, String> specMap) {
        this.specMap = specMap;
    }

}
