package com.itheima.sms.listener;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.exceptions.ClientException;
import com.itheima.sms.util.SmsUtil;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;
import java.util.Map;

public class SmsMessageListener implements MessageListenerConcurrently {
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
            if (msgs!=null) {
                for (MessageExt msg : msgs) {
                    byte[] body = msg.getBody();
                    String mapstr = new String(body);
                    Map<String,String> map = JSON.parseObject(mapstr, Map.class);


                    //发短信
                    SmsUtil.sendSms(map);
                    System.out.println("发是发送到发送到发送到发送到发送到发斯蒂芬"+map);
                }
            }

            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (ClientException e) {
            e.printStackTrace();
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;

        }
    }
}
