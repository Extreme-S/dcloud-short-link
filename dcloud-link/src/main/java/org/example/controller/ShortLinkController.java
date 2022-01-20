package org.example.controller;


import java.util.Map;
import org.example.controller.request.ShortLinkAddRequest;
import org.example.controller.request.ShortLinkDelRequest;
import org.example.controller.request.ShortLinkPageRequest;
import org.example.controller.request.ShortLinkUpdateRequest;
import org.example.service.ShortLinkService;
import org.example.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/link/v1")
public class ShortLinkController {

    @Autowired
    private ShortLinkService shortLinkService;

    /**
     * 新增短链
     */
    @PostMapping("add")
    public JsonData createShortLink(@RequestBody ShortLinkAddRequest request) {
        JsonData jsonData = shortLinkService.createShortLink(request);
        return jsonData;
    }

    /**
     * 分页查找短链
     */
    @RequestMapping("page")
    public JsonData pageByGroupId(@RequestBody ShortLinkPageRequest request) {
        Map<String, Object> result = shortLinkService.pageByGroupId(request);
        return JsonData.buildSuccess(result);
    }

    /**
     * 删除短链
     */
    @PostMapping("del")
    public JsonData del(@RequestBody ShortLinkDelRequest request) {
        return shortLinkService.del(request);
    }

    /**
     * 更新短链
     */
    @PostMapping("update")
    public JsonData update(@RequestBody ShortLinkUpdateRequest request) {
        return shortLinkService.update(request);
    }


}

