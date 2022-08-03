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

    /**
     * 给某个流量包增加天使用次数
     */
    @Override
    public int addDayUsedTimes(long currentTrafficId, Long accountNo, int dayUsedTimes) {
        return trafficMapper.update(null, new UpdateWrapper<TrafficDO>()
                .eq("account_no", accountNo)
                .eq("id", currentTrafficId)
                .set("day_used", dayUsedTimes));
    }

    @Override
    public boolean deleteExpireTraffic() {
        int rows = trafficMapper.delete(new QueryWrapper<TrafficDO>().le("expired_date", new Date()));
        log.info("删除过期流量包行数：rows={}", rows);
        return true;
    }
}
