package org.example.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.example.controller.request.LinkGroupAddRequest;
import org.example.controller.request.LinkGroupUpdateRequest;
import org.example.interceptor.LoginInterceptor;
import org.example.manager.LinkGroupManager;
import org.example.model.LinkGroupDO;
import org.example.service.LinkGroupService;
import org.example.vo.LinkGroupVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LinkGroupServiceImpl implements LinkGroupService {

    @Autowired
    private LinkGroupManager linkGroupManager;

    @Override
    public int add(LinkGroupAddRequest addRequest) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        LinkGroupDO linkGroupDO = new LinkGroupDO();
        linkGroupDO.setTitle(addRequest.getTitle());
        linkGroupDO.setAccountNo(accountNo);
        return linkGroupManager.add(linkGroupDO);
    }

    @Override
    public int del(Long groupId) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        return linkGroupManager.del(groupId, accountNo);
    }

    @Override
    public LinkGroupVO detail(Long groupId) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        LinkGroupDO linkGroupDO = linkGroupManager.detail(groupId, accountNo);
        LinkGroupVO linkGroupVO = new LinkGroupVO();
        // mapStruct
        BeanUtils.copyProperties(linkGroupDO, linkGroupVO);
        return linkGroupVO;
    }

    @Override
    public List<LinkGroupVO> listAllGroup() {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        List<LinkGroupDO> linkGroupDOList = linkGroupManager.listAllGroup(accountNo);
        List<LinkGroupVO> groupVOList = linkGroupDOList.stream().map(obj -> {
            LinkGroupVO linkGroupVO = new LinkGroupVO();
            BeanUtils.copyProperties(obj, linkGroupVO);
            return linkGroupVO;
        }).collect(Collectors.toList());
        return groupVOList;
    }



    @Override
    public int updateById(LinkGroupUpdateRequest request) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        LinkGroupDO linkGroupDO = new LinkGroupDO();
        linkGroupDO.setTitle(request.getTitle());
        linkGroupDO.setId(request.getId());
        linkGroupDO.setAccountNo(accountNo);
        return linkGroupManager.updateById(linkGroupDO);
    }
}
