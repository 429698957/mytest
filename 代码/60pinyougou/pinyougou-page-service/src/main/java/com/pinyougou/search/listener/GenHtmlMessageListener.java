package com.pinyougou.search.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.common.pojo.MessageInfo;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbItem;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GenHtmlMessageListener implements MessageListenerConcurrently {
   @Autowired
   private ItemPageService itemPageService;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {

        if (msgs != null) {
            //1.循环遍历
            for (MessageExt msg : msgs) {
                //2.获取消息体 bytes
                byte[] body = msg.getBody();
                //3.转成字符串 MessageInfo
                String s = new String(body);
                //4.转成对象
                MessageInfo messageInfo = JSON.parseObject(s, MessageInfo.class);
                //5.判断 类型//add update delete 分别生成静态页 和 删除静态页

                switch (messageInfo.getMethod()){
                    case 1 :{

                        break;
                    }
                    case 2: {//update
                        Object context1 = messageInfo.getContext();
                        String s1 = context1.toString();
                        List<TbItem> itemList = JSON.parseArray(s1, TbItem.class);
                        Set<Long> set = new HashSet<>();
                        for (TbItem tbItem : itemList) {
                            Long goodsId = tbItem.getGoodsId();
                            set.add(goodsId);
                        }
                        for (Long aLong : set) {
                            itemPageService.genItemHtml(aLong);
                        }
                         break;
                    }
                    case 3: {//删除静态页面
                        String context1 = messageInfo.getContext().toString();
                        Long[] longs = JSON.parseObject(context1, Long[].class);
                        itemPageService.deleteById(longs);

                        break;
                    }
                    default:{
                        break;
                    }




                }


            }


        }


        return null;
    }
}
