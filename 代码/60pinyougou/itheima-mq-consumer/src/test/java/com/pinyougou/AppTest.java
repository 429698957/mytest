package com.pinyougou;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Unit test for simple App.
 */
@ContextConfiguration("classpath:spring-consumer.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class AppTest {

    @Test
    public void consumerMessage() throws Exception {
           Thread.sleep(10000000);


    }
}
