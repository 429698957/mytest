package com.pinyougou;

import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
        //1.创建消费者，指定消费者组
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumer_cluster_group1");
        //2.设置nameserver地址
        consumer.setNamesrvAddr("192.168.25.129:9876");
        //3.设置订阅的主题
        //参数1；指定主题
        //参数2；指定主题里的TAG,指定具体的TAG 或者使用表达式 *表示所有的TAG
        consumer.subscribe("TopicTest1", "*");
        //4.设置消费的模式（集群模式默认和广播模式）
        consumer.setMessageModel(MessageModel.BROADCASTING);
        //5.设置监听器（目的监听主题 获取里面的信息）
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                //获取消息
                try {
                    if (msgs!=null) {
                        for (MessageExt msg : msgs) {
                            System.out.println("topic"+msg.getTopic());
                            System.out.println("tag"+msg.getTags());
                            System.out.println("keys"+msg.getKeys());
                            byte[] body = msg.getBody();
                            String messageinfo = new String(body);
                            System.out.println("你好啊111"+messageinfo);

                        }
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } catch (Exception e) {
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }

            }
        });
        //6.开始连接
         consumer.start();
        //7.关闭资源

    }
}
