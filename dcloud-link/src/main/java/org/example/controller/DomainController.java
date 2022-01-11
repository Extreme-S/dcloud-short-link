package org.example.controller;


import java.util.List;
import org.example.service.DomainService;
import org.example.util.JsonData;
import org.example.vo.DomainVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/domain/v1")
public class DomainController {

    @Autowired
    private DomainService domainService;

    /**
     * 列举全部可用域名列表
     */
    @GetMapping("list")
    public JsonData listAll() {
        List<DomainVO> list = domainService.listAll();
        return JsonData.buildSuccess(list);
    }
}

