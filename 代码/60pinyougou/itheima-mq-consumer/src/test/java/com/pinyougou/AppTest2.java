package com.pinyougou;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Unit test for simple App.
 */
@ContextConfiguration("classpath:spring-consumer2.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class AppTest2 {

    @Test
    public void consumerMessage() throws Exception {
           Thread.sleep(10000000);


    }
}
