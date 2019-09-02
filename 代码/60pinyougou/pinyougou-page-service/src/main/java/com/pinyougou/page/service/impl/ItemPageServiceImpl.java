package com.pinyougou.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private TbGoodsMapper tbGoodsMapper;

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    private TbGoodsDescMapper tbGoodsDescMapper;

    @Autowired
    private TbItemCatMapper tbItemCatMapper;

    @Autowired
    private TbItemMapper tbItemMapper;

    @Value("${pageDir}")
    private String pageDir;
    @Override
    public void genItemHtml(Long id) {
        //1.根据spu的ID获取spu的数据
        TbGoods tbGoods = tbGoodsMapper.selectByPrimaryKey(id);
        TbGoodsDesc tbGoodsDesc = tbGoodsDescMapper.selectByPrimaryKey(id);


        //2.使用freemarker生成静态页面(模板+数据集=html）
        genHTML("item.ftl",tbGoods,tbGoodsDesc);

    }

    @Override
    public void deleteById(Long[] goodsId) {
        try {
            for (Long aLong : goodsId) {
                FileUtils.forceDelete(new File(pageDir + aLong + ".html"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void genHTML(String templateName,TbGoods tbGoods,TbGoodsDesc tbGoodsDesc)  {
        //1.创建配置类configuration设置文件的编码 以及模板文件所在的位置
        //1.1 以上步骤交给xml配置

        FileWriter writer=null;
        try {
            //2.设置编码和模板所在的目录
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            //3.创建模板文件
            Map<String,Object> model=new HashMap<>();
            model.put("tbGoods",tbGoods);
            model.put("tbGoodsDesc",tbGoodsDesc);
            //根据分类的ID 获取分类的对象里面的名称 设置到数据集中 返回给页面显示
            TbItemCat cat1 = tbItemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id());
            TbItemCat cat2 = tbItemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id());
            TbItemCat cat3 = tbItemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
            model.put("cat1", cat1.getName());
            model.put("cat2", cat2.getName());
            model.put("cat3", cat3.getName());

            //获取该spu所有的sku的列表数据
            Example example = new Example(TbItem.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("goodsId",tbGoods.getId());
            criteria.andEqualTo("status", 1);
            example.setOrderByClause("is_default desc");
            List<TbItem> itemList = tbItemMapper.selectByExample(example);
            model.put("skuList", itemList);
            //4.加载模板对象
            Template template = configuration.getTemplate(templateName);

            //5.创建输出流 指定输出文件的路径
            writer = new FileWriter(new File(pageDir+tbGoods.getId()+".html"));

            //6.执行输出的动作 生成静态页面
            template.process(model, writer);

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            //7.关闭流
            if (writer!=null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }



    }
}
