package org.example.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.manager.ShortLinkManager;
import org.example.mapper.ShortLinkMapper;
import org.example.model.ShortLinkDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class ShortLinkManagerImpl implements ShortLinkManager {

    @Autowired
    private ShortLinkMapper shortLinkMapper;

    @Override
    public int addShortLink(ShortLinkDO shortLinkDO) {
        return shortLinkMapper.insert(shortLinkDO);
    }

    @Override
    public ShortLinkDO findByShortLinCode(String shortLinkCode) {
        ShortLinkDO shortLinkDO = shortLinkMapper.selectOne(
            new QueryWrapper<ShortLinkDO>().eq("code", shortLinkCode));
        return shortLinkDO;
    }

    @Override
    public int del(String shortLinkCode, Long accountNo) {
        ShortLinkDO shortLinkDO = new ShortLinkDO();
        shortLinkDO.setDel(1);
        int rows = shortLinkMapper.update(shortLinkDO,
            new QueryWrapper<ShortLinkDO>().eq("code", shortLinkCode).eq("account_no", accountNo));
        return rows;
    }
}
