package org.example.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.component.ShortLinkComponent;
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
    public ShortLinkDO findByShortLinkCode(String shortLinkCode) {
        return shortLinkMapper.selectOne(new QueryWrapper<ShortLinkDO>()
            .eq("code", shortLinkCode)
            .eq("del", 0));
    }

    @Override
    public int del(ShortLinkDO shortLinkDO) {
        return shortLinkMapper.update(null, new UpdateWrapper<ShortLinkDO>()
            .eq("code", shortLinkDO.getCode())
            .eq("account_no", shortLinkDO.getAccountNo())
            .set("del", 1));
    }

    @Override
    public int update(ShortLinkDO shortLinkDO) {
        int rows = shortLinkMapper.update(null, new UpdateWrapper<ShortLinkDO>()
            .eq("code", shortLinkDO.getCode())
            .eq("del", 0)
            .eq("account_no", shortLinkDO.getAccountNo())
            .set("title", shortLinkDO.getTitle())
            .set("domain", shortLinkDO.getDomain()));
        return rows;
    }
}
