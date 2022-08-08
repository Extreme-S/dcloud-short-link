package org.example.manager;

import org.example.model.TrafficTaskDO;

public interface TrafficTaskManager {

    int add(TrafficTaskDO trafficTaskDO);

    TrafficTaskDO findByIdAndAccountNo(Long id, Long accountNo);

    int deleteByIdAndAccountNo(Long id, Long accountNo);

}
