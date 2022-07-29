package org.example.manager;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.model.TrafficDO;


public interface TrafficManager {

    /**
     * 新增流量包
     */
    int add(TrafficDO trafficDO);


    /**
     * 分页查询可用的流量包
     */
    IPage<TrafficDO> pageAvailable(int page, int size, Long accountNo);


    /**
     * 查找详情
     */
    TrafficDO findByIdAndAccountNo(Long trafficId,Long accountNo);


    /**
     * 增加某个流量包天使用次数
     */
    int addDayUsedTimes(long currentTrafficId, Long accountNo, int dayUsedTimes);


}
