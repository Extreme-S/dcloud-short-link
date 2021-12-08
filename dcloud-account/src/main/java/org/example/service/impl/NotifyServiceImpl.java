package org.example.service.impl;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.example.service.NotifyService;
import org.example.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@Slf4j
public class NotifyServiceImpl implements NotifyService {


    @Autowired
    private RestTemplate restTemplate;

    @Override
    @Async("threadPoolTaskExecutor")
    public void testSend() {
        long beginTime = CommonUtil.getCurrentTimestamp();
        try {
            TimeUnit.MILLISECONDS.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = CommonUtil.getCurrentTimestamp();
        log.info("耗时={}",endTime-beginTime);

//        ResponseEntity<String> forEntity = restTemplate.getForEntity("http://old.xdclass.net", String.class);
//        ResponseEntity<String> forEntity = restTemplate.getForEntity("https://www.baidu.com/", String.class);
//        String body = forEntity.getBody();
//        log.info(body);
    }
}
