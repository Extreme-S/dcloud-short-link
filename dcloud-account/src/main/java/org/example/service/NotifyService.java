package org.example.service;

import org.example.enums.SendCodeEnum;
import org.example.util.JsonData;

public interface NotifyService {

    /**
     * 发送短信验证码
     */
    JsonData sendCode(SendCodeEnum userRegister, String to);

    /**
     * 校验验证码
     */
    boolean checkCode(SendCodeEnum sendCodeEnum, String to, String code);

}

