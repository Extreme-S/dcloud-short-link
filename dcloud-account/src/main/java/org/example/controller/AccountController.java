package org.example.controller;


import org.example.controller.request.AccountLoginRequest;
import org.example.controller.request.AccountRegisterRequest;
import org.example.enums.BizCodeEnum;
import org.example.service.AccountService;
import org.example.service.FileService;
import org.example.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/account/v1")
public class AccountController {

    @Autowired
    private FileService fileService;

    @Autowired
    private AccountService accountService;

    /**
     * 文件上传 最大默认1M
     * 文件格式、拓展名等判断
     */
    @PostMapping("upload")
    public JsonData uploadUserImg(@RequestPart("file") MultipartFile file) {
        String result = fileService.uploadUserImg(file);
        return result != null
            ? JsonData.buildSuccess(result)
            : JsonData.buildResult(BizCodeEnum.FILE_UPLOAD_USER_IMG_FAIL);
    }

    /**
     * 用户注册
     */
    @PostMapping("register")
    public JsonData register(@RequestBody AccountRegisterRequest registerRequest) {
        return accountService.register(registerRequest);
    }

    /**
     * 用户登录
     */
    @PostMapping("login")
    public JsonData login(@RequestBody AccountLoginRequest request) {
        return accountService.login(request);
    }


}

