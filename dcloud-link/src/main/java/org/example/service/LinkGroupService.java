package org.example.service;

import org.example.controller.request.LinkGroupAddRequest;

public interface LinkGroupService {

    /**
     * 新增分组
     *
     * @param addRequest
     * @return
     */
    int add(LinkGroupAddRequest addRequest);

    /**
     * 删除分组
     *
     * @param groupId
     * @return
     */
    int del(Long groupId);
}
