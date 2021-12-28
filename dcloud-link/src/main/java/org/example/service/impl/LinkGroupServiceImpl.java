package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.controller.request.LinkGroupAddRequest;
import org.example.interceptor.LoginInterceptor;
import org.example.manager.LinkGroupManager;
import org.example.model.LinkGroupDO;
import org.example.service.LinkGroupService;
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
}
