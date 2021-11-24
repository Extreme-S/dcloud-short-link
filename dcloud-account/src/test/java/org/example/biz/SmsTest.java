package org.example.biz;

import lombok.extern.slf4j.Slf4j;
import org.example.AccountApplication;
import org.example.component.SmsComponent;
import org.example.config.SmsConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = AccountApplication.class)
@Slf4j
public class SmsTest {

    @Autowired
    private SmsComponent smsComponent;

    @Autowired
    private SmsConfig smsConfig;

    @Test
    public void testSendSms() {
        smsComponent.send("15571696970", smsConfig.getTemplateId(), "666888");
    }


}
