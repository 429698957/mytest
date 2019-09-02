package com.pinyougou;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.util.Collection;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception {
      //1.创建生产者 指定生产者的组名
        DefaultMQProducer producer = new DefaultMQProducer("product_cluster_group1");
        //2.设置nameserver的地址
        producer.setNamesrvAddr("192.168.25.129:9876");
        //3.开启连接
        producer.start();
        //4.发送消息
        String messageInfo = "HelloWorld!!!";
        /**
         *参数1：消息的（大的业务）主题
         *2：消息的标签（小分类）
         *3：key业务的唯一标识
         *4：消息体
         */
        Message msg = new Message("TopicTest1","TAGA","唯一的key",messageInfo.getBytes(RemotingHelper.DEFAULT_CHARSET));
        producer.send(msg);
        //5.关闭连接
        producer.shutdown();
    }


}
