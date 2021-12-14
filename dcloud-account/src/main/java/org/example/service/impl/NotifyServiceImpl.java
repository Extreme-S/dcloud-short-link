package org.example.service.impl;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.example.component.SmsComponent;
import org.example.config.SmsConfig;
import org.example.enums.SendCodeEnum;
import org.example.service.NotifyService;
import org.example.util.CheckUtil;
import org.example.util.CommonUtil;
import org.example.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@Slf4j
public class NotifyServiceImpl implements NotifyService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SmsComponent smsComponent;

    @Autowired
    private SmsConfig smsConfig;

    @Override
    public JsonData sendCode(SendCodeEnum userRegister, String to) {
        String code = CommonUtil.getRandomCode(6);
        if (CheckUtil.isEmail(to)) {
            //发送邮箱验证码  TODO
        } else if (CheckUtil.isPhone(to)) {
            //发送手机验证码
            smsComponent.send(to, smsConfig.getTemplateId(), code);
        }
        return JsonData.buildSuccess();
    }
}
