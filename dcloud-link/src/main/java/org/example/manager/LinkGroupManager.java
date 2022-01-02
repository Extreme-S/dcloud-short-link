package org.example.manager;

import java.util.List;
import org.example.model.LinkGroupDO;

public interface LinkGroupManager {

    int add(LinkGroupDO linkGroupDO);

    int del(Long groupId, Long accountNo);

    LinkGroupDO detail(Long groupId, Long accountNo);

    List<LinkGroupDO> listAllGroup(Long accountNo);

    int updateById(LinkGroupDO linkGroupDO);
}
