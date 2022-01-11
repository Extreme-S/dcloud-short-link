package org.example.controller;


import org.example.controller.request.ShortLinkAddRequest;
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

    @PostMapping("add")
    public JsonData createShortLink(@RequestBody ShortLinkAddRequest request) {
        JsonData jsonData = shortLinkService.createShortLink(request);
        return jsonData;
    }


}

