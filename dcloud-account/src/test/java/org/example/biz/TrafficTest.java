package org.example.biz;

import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.example.AccountApplication;
import org.example.mapper.TrafficMapper;
import org.example.model.TrafficDO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = AccountApplication.class)
@Slf4j
public class TrafficTest {


    @Autowired
    private TrafficMapper trafficMapper;


    @Test
    public void testSaveTraffic() {
        Random random = new Random();
        for (int i = 0; i < 1; i++) {
            TrafficDO trafficDO = new TrafficDO();
            trafficDO.setAccountNo((long) random.nextInt(100));
            trafficMapper.insert(trafficDO);
        }

    }


}
