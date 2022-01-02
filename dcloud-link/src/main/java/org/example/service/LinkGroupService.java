package org.example.service;

import java.util.List;
import org.example.controller.request.LinkGroupAddRequest;
import org.example.controller.request.LinkGroupUpdateRequest;
import org.example.vo.LinkGroupVO;

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

    /**
     * 详情
     *
     * @param groupId
     * @return
     */
    LinkGroupVO detail(Long groupId);

    /**
     * 列出用户全部分组
     *
     * @return
     */
    List<LinkGroupVO> listAllGroup();

    /**
     * 更新组名
     *
     * @param request
     * @return
     */
    int updateById(LinkGroupUpdateRequest request);

}
