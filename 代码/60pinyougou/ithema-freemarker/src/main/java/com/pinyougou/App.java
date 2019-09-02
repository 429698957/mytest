package com.pinyougou;

import com.pinyougou.pojo.Person;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Hello world!
 *
 */
public class  App {


    public static void main( String[] args ) throws Exception {
        //1.创建配置类configuration设置文件的编码 以及模板文件所在的位置
        Configuration configuration = new Configuration(Configuration.getVersion());
        configuration.setDefaultEncoding("utf-8");
        configuration.setDirectoryForTemplateLoading(new File("D:\\ideaEncoding\\pinyougou\\代码\\60pinyougou\\ithema-freemarker\\src\\main\\resources\\template"));
        //2.创建模板文件 官方推荐.ftl后缀

        //3.加载模板文件 获取模板对象,相对路径
        Template template = configuration.getTemplate("template.ftl");
        //4.创建数据集(将来从数据库查询) map
        Map<String,Object> model = new HashMap<>();
        model.put("name", "我尼玛!!!");
        model.put("user", new Person(1000L,"养鸟"));
        List<Person> list = new ArrayList<>();
        list.add(new Person(1L,"你好吗"));
        list.add(new Person(2L,"你好buha吗"));
        list.add(new Person(3L,"你a好吗"));
        model.put("mylist", list);
        model.put("date", new Date());
        model.put("nullkey", "舒服舒服师傅师傅师傅");
        //5.创建输出流 指定输出文件的路径
        FileWriter writer = new FileWriter(new File("D:\\ideaEncoding\\pinyougou\\代码\\60pinyougou\\ithema-freemarker\\src\\main\\resources\\output\\1234.html"));
        //6.执行输出的动作 生成静态页面
        template.process(model,writer);
        //7.关闭流
        writer.close();
    }
}
