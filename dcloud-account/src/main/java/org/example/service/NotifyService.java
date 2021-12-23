package org.example.service;

import org.example.enums.SendCodeEnum;
import org.example.util.JsonData;

public interface NotifyService {

    /**
     * 发送短信验证码
     *
     * @param userRegister
     * @param to
     * @return
     */
    JsonData sendCode(SendCodeEnum userRegister, String to);

    /**
     * 校验验证码
     *
     * @param sendCodeEnum
     * @param to
     * @param code
     * @return
     */
    boolean checkCode(SendCodeEnum sendCodeEnum, String to, String code);

}

