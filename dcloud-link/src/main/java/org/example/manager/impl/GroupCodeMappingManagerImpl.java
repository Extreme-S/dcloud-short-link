package org.example.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.ShortLinkStateEnum;
import org.example.manager.GroupCodeMappingManager;
import org.example.mapper.GroupCodeMappingMapper;
import org.example.model.GroupCodeMappingDO;
import org.example.vo.GroupCodeMappingVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class GroupCodeMappingManagerImpl implements GroupCodeMappingManager {

    @Autowired
    private GroupCodeMappingMapper groupCodeMappingMapper;

    @Override
    public GroupCodeMappingDO findByGroupIdAndMappingId(Long mappingId, Long accountNo, Long groupId) {
        GroupCodeMappingDO groupCodeMappingDO = groupCodeMappingMapper.selectOne(new QueryWrapper<GroupCodeMappingDO>()
            .eq("id", mappingId)
            .eq("account_no", accountNo)
            .eq("group_id", groupId));
        return groupCodeMappingDO;
    }

    @Override
    public int add(GroupCodeMappingDO groupCodeMappingDO) {
        return groupCodeMappingMapper.insert(groupCodeMappingDO);
    }

    @Override
    public int del(GroupCodeMappingDO groupCodeMappingDO) {
        return groupCodeMappingMapper.update(null, new UpdateWrapper<GroupCodeMappingDO>()
            .eq("id", groupCodeMappingDO.getId())
            .eq("account_no", groupCodeMappingDO.getAccountNo())
            .eq("group_id", groupCodeMappingDO.getGroupId())
            .set("del", 1)
        );
    }

    @Override
    public Map<String, Object> pageShortLinkByGroupId(Integer page, Integer size, Long accountNo, Long groupId) {
        Page<GroupCodeMappingDO> pageInfo = new Page<>(page, size);
        Page<GroupCodeMappingDO> groupCodeMappingDOPage = groupCodeMappingMapper.selectPage(pageInfo,
            new QueryWrapper<GroupCodeMappingDO>()
                .eq("account_no", accountNo)
                .eq("group_id", groupId)
                .eq("del", 0));

        Map<String, Object> pageMap = new HashMap<>(3);
        pageMap.put("total_record", groupCodeMappingDOPage.getTotal());
        pageMap.put("total_page", groupCodeMappingDOPage.getPages());
        pageMap.put("current_data",
            groupCodeMappingDOPage.getRecords().stream().map(this::beanProcess).collect(Collectors.toList()));

        return pageMap;
    }

    @Override
    public int updateGroupCodeMappingState(Long accountNo, Long groupId, String shortLinkCode,
        ShortLinkStateEnum shortLinkStateEnum) {
        int rows = groupCodeMappingMapper.update(null, new UpdateWrapper<GroupCodeMappingDO>()
            .eq("code", shortLinkCode)
            .eq("account_no", accountNo)
            .eq("group_id", groupId)
            .eq("del", 0)
            .set("state", shortLinkStateEnum.name()));
        return rows;
    }

    @Override
    public GroupCodeMappingDO findByCodeAndGroupId(String shortLinkCode, Long groupId, Long accountNo) {
        GroupCodeMappingDO groupCodeMappingDO = groupCodeMappingMapper.selectOne(new QueryWrapper<GroupCodeMappingDO>()
            .eq("code", shortLinkCode)
            .eq("account_no", accountNo)
            .eq("group_id", groupId)
            .eq("del", 0));
        return groupCodeMappingDO;
    }

    @Override
    public int update(GroupCodeMappingDO groupCodeMappingDO) {
        int rows = groupCodeMappingMapper.update(null, new UpdateWrapper<GroupCodeMappingDO>()
            .eq("id", groupCodeMappingDO.getId())
            .eq("account_no", groupCodeMappingDO.getAccountNo())
            .eq("group_id", groupCodeMappingDO.getGroupId())
            .eq("del", 0)
            .set("title", groupCodeMappingDO.getTitle())
            .set("domain", groupCodeMappingDO.getDomain())
        );
        return rows;
    }


    private GroupCodeMappingVO beanProcess(GroupCodeMappingDO groupCodeMappingDO) {
        GroupCodeMappingVO groupCodeMappingVO = new GroupCodeMappingVO();
        BeanUtils.copyProperties(groupCodeMappingDO, groupCodeMappingVO);
        return groupCodeMappingVO;
    }

}
