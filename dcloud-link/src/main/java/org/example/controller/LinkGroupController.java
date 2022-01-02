package org.example.controller;


import java.util.List;
import org.example.controller.request.LinkGroupAddRequest;
import org.example.controller.request.LinkGroupUpdateRequest;
import org.example.enums.BizCodeEnum;
import org.example.service.LinkGroupService;
import org.example.util.JsonData;
import org.example.vo.LinkGroupVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    /**
     * 根据id找详情
     */
    @GetMapping("detail/{group_id}")
    public JsonData detail(@PathVariable("group_id") Long groupId) {
        LinkGroupVO linkGroupVO = linkGroupService.detail(groupId);
        return JsonData.buildSuccess(linkGroupVO);
    }

    /**
     * 列出用户全部分组
     */
    @GetMapping("list")
    public JsonData findUserAllLinkGroup() {
        List<LinkGroupVO> list = linkGroupService.listAllGroup();
        return JsonData.buildSuccess(list);
    }

    /**
     * 更新短链分组
     */
    @PutMapping("update")
    public JsonData update(@RequestBody LinkGroupUpdateRequest request) {
        int rows = linkGroupService.updateById(request);
        return rows == 1 ? JsonData.buildSuccess() : JsonData.buildResult(BizCodeEnum.GROUP_OPER_FAIL);
    }

}

