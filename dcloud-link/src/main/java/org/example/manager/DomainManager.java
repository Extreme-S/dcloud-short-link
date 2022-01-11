package org.example.manager;

import java.util.List;
import org.example.enums.DomainTypeEnum;
import org.example.model.DomainDO;

public interface DomainManager {

    /**
     * 查找详情
     */
    DomainDO findById(Long id, Long accountNO);

    /**
     * 查找详情
     */
    DomainDO findByDomainTypeAndID(Long id, DomainTypeEnum domainTypeEnum);

    /**
     * 新增
     */
    int addDomain(DomainDO domainDO);

    /**
     * 列举全部官方域名
     */
    List<DomainDO> listOfficialDomain();

    /**
     * 列举全部自定义域名
     */
    List<DomainDO> listCustomDomain(Long accountNo);

}
