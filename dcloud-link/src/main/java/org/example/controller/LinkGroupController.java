package org.example.controller;


import org.example.controller.request.LinkGroupAddRequest;
import org.example.enums.BizCodeEnum;
import org.example.service.LinkGroupService;
import org.example.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/group/v1")
public class LinkGroupController {

    @Autowired
    private LinkGroupService linkGroupService;

    /**
     * 创建分组
     */
    @PostMapping("/add")
    public JsonData add(@RequestBody LinkGroupAddRequest addRequest) {
        int rows = linkGroupService.add(addRequest);
        return rows == 1 ? JsonData.buildSuccess() : JsonData.buildResult(BizCodeEnum.GROUP_ADD_FAIL);
    }


    /**
     * 根据id删除分组
     */
    @DeleteMapping("/del/{group_id}")
    public JsonData del(@PathVariable("group_id") Long groupId) {
        int rows = linkGroupService.del(groupId);
        return rows == 1 ? JsonData.buildSuccess() : JsonData.buildResult(BizCodeEnum.GROUP_NOT_EXIST);
    }

}

