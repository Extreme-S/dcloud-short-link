package org.example.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.model.TrafficDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author 不爱吃鱼的猫、
 * @since 2021-11-18
 */
public interface TrafficMapper extends BaseMapper<TrafficDO> {

    /**
     * 给某个流量包增加天使用次数
     */
    int addDayUsedTimes(@Param("accountNo") Long accountNo,
                        @Param("trafficId") Long trafficId,
                        @Param("usedTimes") Integer usedTimes);

    /**
     * 恢复某个流量包使用次数
     */
    int releaseUsedTimes(@Param("accountNo") Long accountNo,
                         @Param("trafficId") Long trafficId,
                         @Param("usedTimes") Integer usedTimes);
}
