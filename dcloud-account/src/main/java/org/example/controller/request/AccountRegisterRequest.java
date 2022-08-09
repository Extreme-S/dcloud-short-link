package org.example.controller.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;


@Data
public class AccountRegisterRequest {

    /**
     * 头像
     */
    private String headImg;

    /**
     * 手机号(不能为空)
     * TODO 可以自定义手机号注解
     * TODO json返回数据格式
     */
    @NotBlank(message = "手机号不能为空")
    private String phone;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String pwd;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    private String mail;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 短信验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String code;

}
