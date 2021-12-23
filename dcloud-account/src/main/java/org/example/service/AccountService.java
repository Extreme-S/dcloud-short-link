package org.example.service;

import org.example.controller.request.AccountLoginRequest;
import org.example.controller.request.AccountRegisterRequest;
import org.example.util.JsonData;

public interface AccountService {

    /**
     * 用户注册
     *
     * @param registerRequest
     * @return
     */
    JsonData register(AccountRegisterRequest registerRequest);

    /**
     * 用户登录
     *
     * @param request
     * @return
     */
    JsonData login(AccountLoginRequest request);
}
