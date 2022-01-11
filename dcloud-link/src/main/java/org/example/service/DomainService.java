package org.example.service;

import java.util.List;
import org.example.vo.DomainVO;

public interface DomainService {

    /**
     * 列举全部可用域名
     */
    List<DomainVO> listAll();

}
