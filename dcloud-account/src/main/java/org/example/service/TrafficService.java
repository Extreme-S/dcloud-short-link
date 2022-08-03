package org.example.service;

import org.example.controller.request.TrafficPageRequest;
import org.example.model.EventMessage;
import org.example.vo.TrafficVO;

import java.util.Map;

public interface TrafficService {

    void handleTrafficMessage(EventMessage eventMessage);

    Map<String, Object> pageAvailable(TrafficPageRequest request);

    TrafficVO detail(long trafficId);

    /**
     * 删除过期流量包
     */
    boolean deleteExpireTraffic();

}
