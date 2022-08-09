package org.example.service.impl;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.StringUtils;
import org.example.config.RabbitMQConfig;
import org.example.controller.request.AccountLoginRequest;
import org.example.controller.request.AccountRegisterRequest;
import org.example.enums.AuthTypeEnum;
import org.example.enums.BizCodeEnum;
import org.example.enums.EventMessageType;
import org.example.enums.SendCodeEnum;
import org.example.manager.AccountManager;
import org.example.model.AccountDO;
import org.example.model.EventMessage;
import org.example.model.LoginUser;
import org.example.service.AccountService;
import org.example.service.NotifyService;
import org.example.util.CommonUtil;
import org.example.util.IDUtil;
import org.example.util.JWTUtil;
import org.example.util.JsonData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    /**
     * 免费流量包商品id
     */
    private static final Long FREE_TRAFFIC_PRODUCT_ID = 1L;


    /**
     * 手机验证码验证
     * 密码加密
     * 账号唯一性检查(TODO)
     * 插入数据库
     * 新注册用户福利发放
     */
    @Override
    public JsonData register(AccountRegisterRequest registerRequest) {
        boolean checkFlag = notifyService.checkCode(    //判断手机验证码是否正确
                SendCodeEnum.USER_REGISTER, registerRequest.getPhone(), registerRequest.getCode());
        if (!checkFlag) return JsonData.buildResult(BizCodeEnum.CODE_ERROR);
        //加密处理密码，生成accountDO对象插入数据库
        AccountDO accountDO = new AccountDO();
        BeanUtils.copyProperties(registerRequest, accountDO);
        accountDO.setAccountNo(Long.valueOf(IDUtil.geneSnowFlakeID().toString()));                                      //账号唯一编号
        accountDO.setAuth(AuthTypeEnum.DEFAULT.name());                                                                 //用户认证级别
        accountDO.setSecret("$1$" + CommonUtil.getStringNumRandom(8));                                           //密钥
        accountDO.setPwd(Md5Crypt.md5Crypt(registerRequest.getPwd().getBytes(), accountDO.getSecret()));                //pwd，加密加盐处理
        int rows = accountManager.insert(accountDO);
        log.info("rows:{},注册成功:{}", rows, accountDO);

        userRegisterInitTask(accountDO);//用户注册成功，发放福利
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
     */
    private void userRegisterInitTask(AccountDO accountDO) {
        EventMessage eventMessage = EventMessage.builder()
                .messageId(IDUtil.geneSnowFlakeID().toString())
                .accountNo(accountDO.getAccountNo())
                .eventMessageType(EventMessageType.TRAFFIC_FREE_INIT.name())
                .bizId(FREE_TRAFFIC_PRODUCT_ID.toString())
                .build();
        //发送发放流量包消息
        rabbitTemplate.convertAndSend(
                rabbitMQConfig.getTrafficEventExchange(), rabbitMQConfig.getTrafficFreeInitRoutingKey(), eventMessage);
    }
}










