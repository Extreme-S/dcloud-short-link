package org.example.job;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.example.service.TrafficService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TrafficJobHandler {


    @Autowired
    private TrafficService trafficService;

    /**
     * 过期流量包处理
     */
    @XxlJob(value = "trafficExpiredHandler", init = "init", destroy = "destroy")
    public ReturnT<String> execute(String param) {
        log.info("xxl-job execute 任务方法触发成功,删除过期流量包");
        boolean flag = trafficService.deleteExpireTraffic();
        return ReturnT.SUCCESS;
    }

    private void init() {
        log.info("小滴课堂 MyJobHandler init >>>>>");
    }

    private void destroy() {
        log.info("小滴课堂 MyJobHandler destroy >>>>>");
    }

}
