package org.example.controller;

import org.example.service.NotifyService;
import org.example.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account/v1")
public class NotifyController {


    @Autowired
    private NotifyService notifyService;

    /**
     * 测试发送验证码接口-主要是用于对比优化前后区别
     *
     * @return
     */
    @GetMapping("send_code")
    public JsonData sendCode() {
        notifyService.testSend();
        return JsonData.buildSuccess("自定义线程池测试");
    }
}
