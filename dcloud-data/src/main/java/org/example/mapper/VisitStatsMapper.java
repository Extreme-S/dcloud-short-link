package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.model.VisitStatsDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VisitStatsMapper extends BaseMapper<VisitStatsDO> {


    /**
     * 分页查询
     */
    List<VisitStatsDO> pageVisitRecord(@Param("code") String code,
                                       @Param("accountNo") Long accountNo,
                                       @Param("from") int from,
                                       @Param("size") int size);

    /**
     * 计算总条数
     */
    int countTotal(@Param("code") String code,
                   @Param("accountNo") Long accountNo);

    /**
     * 根据时间范围查询地区访问分布
     */
    List<VisitStatsDO> queryRegionVisitStatsWithDay(@Param("code") String code,
                                                    @Param("accountNo") Long accountNo,
                                                    @Param("startTime") String startTime,
                                                    @Param("endTime") String endTime);


    /**
     * 查询时间范围内的访问趋势图 天级别
     */
    List<VisitStatsDO> queryVisitTrendWithMultiDay(@Param("code") String code,
                                                   @Param("accountNo") Long accountNo,
                                                   @Param("startTime") String startTime,
                                                   @Param("endTime") String endTime);

    /**
     * 查询时间范围内的访问趋势图 小时级别
     */
    List<VisitStatsDO> queryVisitTrendWithHour(@Param("code") String code,
                                               @Param("accountNo") Long accountNo,
                                               @Param("startTime") String startTime);

    /**
     * 查询时间范围内的访问趋势图 分钟级别
     */
    List<VisitStatsDO> queryVisitTrendWithMinute(@Param("code") String code,
                                                 @Param("accountNo") Long accountNo,
                                                 @Param("startTime") String startTime,
                                                 @Param("endTime") String endTime);

    /**
     * 查询高频访问来源
     */
    List<VisitStatsDO> queryFrequentSource(@Param("code") String code,
                                           @Param("accountNo") Long accountNo,
                                           @Param("startTime") String startTime,
                                           @Param("endTime") String endTime,
                                           @Param("size") int size);

    /**
     * 查询设备类型
     */
    List<VisitStatsDO> queryDeviceInfo(@Param("code") String code,
                                       @Param("accountNo") Long accountNo,
                                       @Param("startTime") String startTime,
                                       @Param("endTime") String endTime,
                                       @Param("field") String field);
}
