package org.example.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.manager.TrafficManager;
import org.example.mapper.TrafficMapper;
import org.example.model.TrafficDO;
import org.example.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;


@Component
@Slf4j
public class TrafficManagerImpl implements TrafficManager {


    @Autowired
    private TrafficMapper trafficMapper;

    @Override
    public int add(TrafficDO trafficDO) {
        return trafficMapper.insert(trafficDO);
    }


    @Override
    public IPage<TrafficDO> pageAvailable(int page, int size, Long accountNo) {
        Page<TrafficDO> pageInfo = new Page<>(page, size);
        String today = TimeUtil.format(new Date(), "yyyy-MM-dd");
        Page<TrafficDO> trafficDOPage = trafficMapper.selectPage(pageInfo, new QueryWrapper<TrafficDO>()
                .eq("account_no", accountNo)
                .ge("expired_date", today)
                .orderByDesc("gmt_create"));
        return trafficDOPage;
    }

    @Override
    public TrafficDO findByIdAndAccountNo(Long trafficId, Long accountNo) {
        TrafficDO trafficDO = trafficMapper.selectOne(new QueryWrapper<TrafficDO>()
                .eq("account_no", accountNo)
                .eq("id", trafficId));
        return trafficDO;
    }

    @Override
    public boolean deleteExpireTraffic() {
        int rows = trafficMapper.delete(new QueryWrapper<TrafficDO>().le("expired_date", new Date()));
        log.info("删除过期流量包行数：rows={}", rows);
        return true;
    }

    /**
     * 查找未过期流量列表（不一定可用，可能超过次数）
     * select * from traffic where account_no =111 and (expired_date >= ? OR out_trade_no=free_init )
     */
    @Override
    public List<TrafficDO> selectAvailableTraffics(Long accountNo) {
        String today = TimeUtil.format(new Date(), "yyyy-MM-dd");
        return trafficMapper.selectList(new QueryWrapper<TrafficDO>()
                .eq("account_no", accountNo)
                .and(wrapper -> wrapper.ge("expired_date", today).or().eq("out_trade_no", "free_init")));
    }

    /**
     * 增加流量包使用次数
     */
    @Override
    public int addDayUsedTimes(Long accountNo, Long trafficId, Integer usedTimes) {
        return trafficMapper.addDayUsedTimes(accountNo, trafficId, usedTimes);
    }

    /**
     * 恢复某个流量包使用次数，回滚流量包
     */
    @Override
    public int releaseUsedTimes(Long accountNo, Long trafficId, Integer useTimes, String useDateStr) {
        return trafficMapper.releaseUsedTimes(accountNo, trafficId, useTimes, useDateStr);
    }

    @Override
    public int batchUpdateUsedTimes(Long accountNo, List<Long> unUpdatedTrafficIds) {
        int rows = trafficMapper.update(null, new UpdateWrapper<TrafficDO>()
                .eq("account_no", accountNo)
                .in("id", unUpdatedTrafficIds)
                .set("day_used", 0));
        return rows;
    }
}
