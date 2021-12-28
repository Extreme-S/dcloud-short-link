package org.example.manager;

import org.example.model.LinkGroupDO;

public interface LinkGroupManager {

    int add(LinkGroupDO linkGroupDO);

    int del(Long groupId, Long accountNo);
}
