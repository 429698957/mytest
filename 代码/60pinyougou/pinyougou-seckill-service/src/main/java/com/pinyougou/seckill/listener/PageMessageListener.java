package com.pinyougou.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.common.pojo.MessageInfo;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 监听器 目的是监听秒杀商品的ID，生成静态页面
 */
public class PageMessageListener implements MessageListenerConcurrently {

    @Autowired
    private FreeMarkerConfigurer markerConfigurer;

    @Autowired
    private TbSeckillGoodsMapper tbSeckillGoodsMapper;


    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        if (msgs != null) {
            for (MessageExt msg : msgs) {
                //1.获取消息体
                byte[] body = msg.getBody();
                //2.转成string
                String s = new String(body);
                //3.转成JSON对象
                MessageInfo messageInfo = JSON.parseObject(s, MessageInfo.class);
                //4.判断是否是ADD 生成静态页面
                switch (messageInfo.getMethod()) {
                    case 1: {//ADD
                        //获取对象
                        String context = messageInfo.getContext().toString();
                        //转成数组
                        Long[] aLongs = JSON.parseObject(context, Long[].class);
                        //查询数据库的数据

                        for (Long aLong : aLongs) {
                            //使用freemarker生成静态页面
                            genHTML("item.ftl", aLong);
                        }
                        break;
                    }
                    case 2: {

                        break;
                    }
                    case 3: {

                        break;
                    }
                    default: {


                    }
                }
            }
        }


        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    @Value("${PageDir}")
    private String pageDir;

    //生成静态页面 数据集+模板=html
    private void genHTML(String templateName, Long id) {
        FileWriter writer = null;
        try {
            //1.创建一个配置类configruation

            //2.设置utf-8编码

            //3.设置模板文件所在目录
            Configuration configuration = markerConfigurer.getConfiguration();

            //4.创建模板文件（针对秒杀详情页的模板）

            //5.加载模板对象
            Template template = configuration.getTemplate(templateName);
            //6.从数据库中获取数据集
            TbSeckillGoods tbSeckillGoods = tbSeckillGoodsMapper.selectByPrimaryKey(id);
            Map<String, Object> model = new HashMap<>();
            model.put("seckillGoods", tbSeckillGoods);
            //7.创建一个writer
            writer = new FileWriter(new File(pageDir + id + ".html"));
            //8.处理生成页面
            template.process(model, writer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //9.关闭流
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
