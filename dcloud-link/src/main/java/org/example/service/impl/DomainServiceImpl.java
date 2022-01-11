package org.example.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.example.interceptor.LoginInterceptor;
import org.example.manager.DomainManager;
import org.example.model.DomainDO;
import org.example.service.DomainService;
import org.example.vo.DomainVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class DomainServiceImpl implements DomainService {

    @Autowired
    private DomainManager domainManager;

    @Override
    public List<DomainVO> listAll() {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        List<DomainDO> customDomainList = domainManager.listCustomDomain(accountNo);
        List<DomainDO> officialDomainList = domainManager.listOfficialDomain();
        customDomainList.addAll(officialDomainList);
        return customDomainList.stream().map(this::beanProcess).collect(Collectors.toList());
    }

    private DomainVO beanProcess(DomainDO domainDO) {
        DomainVO domainVO = new DomainVO();
        BeanUtils.copyProperties(domainDO, domainVO);
        return domainVO;
    }

}
