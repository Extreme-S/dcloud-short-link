package org.example.service.impl;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.StringUtils;
import org.example.controller.request.AccountLoginRequest;
import org.example.controller.request.AccountRegisterRequest;
import org.example.enums.AuthTypeEnum;
import org.example.enums.BizCodeEnum;
import org.example.enums.SendCodeEnum;
import org.example.manager.AccountManager;
import org.example.model.AccountDO;
import org.example.model.LoginUser;
import org.example.service.AccountService;
import org.example.service.NotifyService;
import org.example.util.CommonUtil;
import org.example.util.IDUtil;
import org.example.util.JWTUtil;
import org.example.util.JsonData;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private AccountManager accountManager;


    /**
     * 手机验证码验证
     * 密码加密（TODO）
     * 账号唯一性检查(TODO)
     * 插入数据库
     * 新注册用户福利发放(TODO)
     */
    @Override
    public JsonData register(AccountRegisterRequest registerRequest) {
        boolean checkCode = false;
        //判断验证码
        if (StringUtils.isNotBlank(registerRequest.getPhone())) {
            checkCode = notifyService.checkCode(SendCodeEnum.USER_REGISTER, registerRequest.getPhone(),
                registerRequest.getCode());
        }
        //验证码错误
        if (!checkCode) {
            return JsonData.buildResult(BizCodeEnum.CODE_ERROR);
        }
        //加密处理密码，生成accountDO对象插入数据库
        AccountDO accountDO = new AccountDO();
        BeanUtils.copyProperties(registerRequest, accountDO);
        accountDO.setAccountNo(Long.valueOf(IDUtil.geneSnowFlakeID().toString()));//账号唯一编号
        accountDO.setAuth(AuthTypeEnum.DEFAULT.name());//用户认证级别
        accountDO.setSecret("$1$" + CommonUtil.getStringNumRandom(8));//密钥
        String cryptPwd = Md5Crypt.md5Crypt(registerRequest.getPwd().getBytes(), accountDO.getSecret());//盐
        accountDO.setPwd(cryptPwd);

        int rows = accountManager.insert(accountDO);
        log.info("rows:{},注册成功:{}", rows, accountDO);
        //用户注册成功，发放福利
        userRegisterInitTask(accountDO);
        return JsonData.buildSuccess();

    }

    /**
     * 1、根据手机号去找
     * 2、有的话，则用秘钥+用户传递的明文密码，进行加密，再和数据库的密文进行匹配
     */
    @Override
    public JsonData login(AccountLoginRequest request) {
        List<AccountDO> accountDOList = accountManager.findByPhone(request.getPhone());
        if (accountDOList != null && accountDOList.size() == 1) {
            AccountDO accountDO = accountDOList.get(0);
            String md5Crypt = Md5Crypt.md5Crypt(request.getPwd().getBytes(), accountDO.getSecret());
            if (md5Crypt.equalsIgnoreCase(accountDO.getPwd())) {
                LoginUser loginUser = LoginUser.builder().build();
                BeanUtils.copyProperties(accountDO, loginUser);
                return JsonData.buildSuccess(JWTUtil.geneJsonWebTokne(loginUser));
            } else {
                return JsonData.buildResult(BizCodeEnum.ACCOUNT_PWD_ERROR);
            }
        } else {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNREGISTER);
        }
    }

    /**
     * 用户注册成功，发放福利
     *
     * @param accountDO
     */
    private void userRegisterInitTask(AccountDO accountDO) {

    }
}
