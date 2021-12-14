package org.example.service.impl;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.component.SmsComponent;
import org.example.config.SmsConfig;
import org.example.constant.RedisKey;
import org.example.enums.BizCodeEnum;
import org.example.enums.SendCodeEnum;
import org.example.service.NotifyService;
import org.example.util.CheckUtil;
import org.example.util.CommonUtil;
import org.example.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
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

    @Autowired
    private StringRedisTemplate redisTemplate;

    //短信/邮箱验证码的有效时间
    private static final int CODE_EXPIRED = 60 * 1000 * 10;


    @Override
    public JsonData sendCode(SendCodeEnum sendCodeEnum, String to) {
        //获取用户注册时的 短信/邮箱验证码
        String cacheKey = String.format(RedisKey.CHECK_CODE_KEY, sendCodeEnum.name(), to);
        String cacheValue = redisTemplate.opsForValue().get(cacheKey);

        //如果短信/邮箱验证码不为空，再判断是否是60秒内重复发送 0122_232131321314132
        if (StringUtils.isNotBlank(cacheValue)) {
            long ttl = Long.parseLong(cacheKey.split("_")[1]);
            //当前时间戳-验证码发送时的时间戳，如果小于60秒，则不给重复发送
            long leftTime = CommonUtil.getCurrentTimestamp() - ttl;
            if (leftTime < (1000 * 60)) {
                log.info("重复发送短信验证码，时间间隔:{}秒", leftTime);
                return JsonData.buildResult(BizCodeEnum.CODE_LIMITED);
            }
        }

        //生成 短信/邮箱验证码 并拼接好时间戳，存储redis
        String code = CommonUtil.getRandomCode(6);
        String value = code + "_" + CommonUtil.getCurrentTimestamp();
        redisTemplate.opsForValue().set(cacheKey, value, CODE_EXPIRED, TimeUnit.MILLISECONDS);

        if (CheckUtil.isEmail(to)) {
            //发送邮箱验证码  TODO

        } else if (CheckUtil.isPhone(to)) {

            //发送手机验证码
            smsComponent.send(to, smsConfig.getTemplateId(), code);
        }
        return JsonData.buildSuccess();
    }
}
