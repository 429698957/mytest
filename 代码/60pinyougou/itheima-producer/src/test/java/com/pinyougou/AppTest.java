package com.pinyougou;

import static org.junit.Assert.assertTrue;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Unit test for simple App.
 */

@ContextConfiguration("classpath:spring-producer.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class AppTest 
{
    @Autowired
    DefaultMQProducer defaultMQProducer;
    @Test
    public void sendMessage() throws Exception {
        Message msg = new Message("TopicSpringTest","TAGA","唯一的key","spring发来的数据".getBytes(RemotingHelper.DEFAULT_CHARSET));
      defaultMQProducer.send(msg);
      Thread.sleep(10000000);

    }
}
