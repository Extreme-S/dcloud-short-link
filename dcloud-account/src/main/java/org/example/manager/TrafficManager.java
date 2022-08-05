package org.example.manager;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.model.TrafficDO;

import java.util.List;


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
    TrafficDO findByIdAndAccountNo(Long trafficId, Long accountNo);


    /**
     * 删除过期流量包
     */
    boolean deleteExpireTraffic();


    /**
     * 查找可用的短链流量包(未过期),包括免费流量包
     */
    List<TrafficDO> selectAvailableTraffics(Long accountNo);


    /**
     * 给某个流量包增加使用次数
     */
    int addDayUsedTimes(Long accountNo, Long trafficId, Integer usedTimes);


    /**
     * 恢复流量包使用当天次数
     */
    int releaseUsedTimes(Long accountNo, Long trafficId, Integer useTimes);


    /**
     * 批量更新流量包使用次数为0
     */
    int batchUpdateUsedTimes(Long accountNo, List<Long> unUpdatedTrafficIds);


}
