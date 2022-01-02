package org.example.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import org.example.manager.LinkGroupManager;
import org.example.mapper.LinkGroupMapper;
import org.example.model.LinkGroupDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class LinkGroupManagerImpl implements LinkGroupManager {

    @Autowired
    private LinkGroupMapper linkGroupMapper;

    @Override
    public int add(LinkGroupDO linkGroupDO) {
        return linkGroupMapper.insert(linkGroupDO);
    }

    @Override
    public int del(Long groupId, Long accountNo) {
        return linkGroupMapper.delete(new QueryWrapper<LinkGroupDO>()
            .eq("id", groupId).eq("account_no", accountNo));
    }

    @Override
    public LinkGroupDO detail(Long groupId, Long accountNo) {
        return linkGroupMapper.selectOne(new QueryWrapper<LinkGroupDO>().eq("id", groupId).eq("account_no", accountNo));
    }

    @Override
    public List<LinkGroupDO> listAllGroup(Long accountNo) {
        return linkGroupMapper.selectList(new QueryWrapper<LinkGroupDO>().eq("account_no", accountNo));
    }

    @Override
    public int updateById(LinkGroupDO linkGroupDO) {
        return linkGroupMapper.update(linkGroupDO,
            new QueryWrapper<LinkGroupDO>().eq("id", linkGroupDO.getId()).eq("account_no", linkGroupDO.getAccountNo()));
    }
}
