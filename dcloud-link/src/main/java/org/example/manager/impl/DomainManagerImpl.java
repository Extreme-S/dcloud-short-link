package org.example.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.DomainTypeEnum;
import org.example.manager.DomainManager;
import org.example.mapper.DomainMapper;
import org.example.model.DomainDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class DomainManagerImpl implements DomainManager {

    @Autowired
    private DomainMapper domainMapper;

    @Override
    public DomainDO findById(Long id, Long accountNO) {
        return domainMapper.selectOne(new QueryWrapper<DomainDO>()
            .eq("id", id)
            .eq("account_no", accountNO));
    }

    @Override
    public DomainDO findByDomainTypeAndID(Long id, DomainTypeEnum domainTypeEnum) {
        return domainMapper.selectOne(new QueryWrapper<DomainDO>()
            .eq("id", id)
            .eq("domain_type", domainTypeEnum.name()));
    }

    @Override
    public int addDomain(DomainDO domainDO) {
        return domainMapper.insert(domainDO);
    }

    @Override
    public List<DomainDO> listOfficialDomain() {
        return domainMapper.selectList(new QueryWrapper<DomainDO>()
            .eq("domain_type", DomainTypeEnum.OFFICIAL.name()));
    }

    @Override
    public List<DomainDO> listCustomDomain(Long accountNo) {
        return domainMapper.selectList(new QueryWrapper<DomainDO>()
            .eq("domain_type", DomainTypeEnum.CUSTOM.name())
            .eq("account_no", accountNo));
    }
}
