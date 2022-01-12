package org.example.manager;

import java.util.Map;
import org.example.enums.ShortLinkStateEnum;
import org.example.model.GroupCodeMappingDO;

public interface GroupCodeMappingManager {

    /**
     * 查找详情
     */
    GroupCodeMappingDO findByGroupIdAndMappingId(Long mappingId, Long accountNo, Long groupId);

    /**
     * 新增
     */
    int add(GroupCodeMappingDO groupCodeMappingDO);

    /**
     * 根据短链码删除
     */
    int del(String shortLinkCode, Long accountNo, Long groupId);

    /**
     * 分页查找
     */
    Map<String, Object> pageShortLinkByGroupId(Integer page, Integer size, Long accountNo, Long groupId);

    /**
     * 更新短链码状态
     */
    int updateGroupCodeMappingState(Long accountNo, Long groupId, String shortLinkCode,
        ShortLinkStateEnum shortLinkStateEnum);

    /**
     * 查找是否存在GroupCodeMappingDO对象
     */
    GroupCodeMappingDO findByCodeAndGroupId(String shortLinkCode, Long groupId, Long accountNo);
}
