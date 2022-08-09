package org.example.service;

import org.example.controller.request.TrafficPageRequest;
import org.example.controller.request.UseTrafficRequest;
import org.example.model.EventMessage;
import org.example.util.JsonData;
import org.example.vo.TrafficVO;

import java.util.Map;

public interface TrafficService {

    /**
     * TRAFFIC_USED
     * - 流量包使用，检查是否成功使用
     *             //检查task是否存在
     *             //检查短链是否成功
     *             //如果不成功，则恢复流量包
     *             //删除task (也可以更新task状态，定时删除就行)
     */
    void handleTrafficMessage(EventMessage eventMessage);

    Map<String, Object> pageAvailable(TrafficPageRequest request);

    TrafficVO detail(long trafficId);

    /**
     * 删除过期流量包
     */
    boolean deleteExpireTraffic();

    /**
     * 扣减流量包
     * - 查询用户全部可用流量包
     * - 遍历流量包，根据日期判断是否需要更新
     * - - 当日未更新的流量包后加入【待更新集合】中  ->更新流量包
     * - - 当日已更新的判断是否超过 day_limit      ->增加day_used
     * - 更新今日用户【待更新集合】中流量包相关数据
     * - 扣减使用的某个流量包使用次数
     */
    JsonData reduce(UseTrafficRequest useTrafficRequest);
}
