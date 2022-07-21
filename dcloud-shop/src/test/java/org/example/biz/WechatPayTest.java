package org.example.biz;

import lombok.extern.slf4j.Slf4j;
import org.example.ShopApplication;
import org.example.config.PayBeanConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = ShopApplication.class)
@Slf4j
public class WechatPayTest {

    @Autowired
    private PayBeanConfig payBeanConfig;


    @Test
    public void testLoadPrivateKey() throws IOException {

        log.info(payBeanConfig.getPrivateKey().getAlgorithm());

    }




}

