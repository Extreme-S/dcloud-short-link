package org.example.controller;

import com.google.code.kaptcha.Producer;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.controller.request.SendCodeRequest;
import org.example.enums.BizCodeEnum;
import org.example.enums.SendCodeEnum;
import org.example.service.NotifyService;
import org.example.util.CommonUtil;
import org.example.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notify/v1")
@Slf4j
public class NotifyController {


    @Autowired
    private NotifyService notifyService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private StringRedisTemplate redisTemplate;

    //redis中Kaptcha验证码过期时间
    private static final long KAPTCHA_CODE_EXPIRED = 1000 * 10 * 60;

    /**
     * 获取kaptcha验证码图片
     *
     * @param request
     * @param response
     */
    @GetMapping("kaptcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {

        String kaptchaText = kaptchaProducer.createText();
        log.info("验证码内容:{}", kaptchaText);

        //存储redis,配置过期时间
        redisTemplate.opsForValue()
            .set(getKaptchaKey(request), kaptchaText, KAPTCHA_CODE_EXPIRED, TimeUnit.MILLISECONDS);
        BufferedImage bufferedImage = kaptchaProducer.createImage(kaptchaText);
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            ImageIO.write(bufferedImage, "jpg", outputStream);
            outputStream.flush();
        } catch (IOException e) {
            log.error("获取流出错:{}", e.getMessage());
        }
    }

    /**
     * 发送短信验证码
     *
     * @return
     */
    @PostMapping("send_code")
    public JsonData sendCode(@RequestBody SendCodeRequest sendCodeRequest, HttpServletRequest request) {
        //根据浏览器指纹生成key，并获取redis中的kaptcha验证码 和 request中的验证码
        String key = getKaptchaKey(request);
        String cacheKaptcha = redisTemplate.opsForValue().get(key);
        String kaptcha = sendCodeRequest.getKaptcha();

        //kaptcha图形验证码匹配成功，发送短信/邮箱验证码
        if (kaptcha != null && cacheKaptcha != null && cacheKaptcha.equalsIgnoreCase(kaptcha)) {
            redisTemplate.delete(key);
            return notifyService.sendCode(SendCodeEnum.USER_REGISTER, sendCodeRequest.getTo());
        } else {
            return JsonData.buildResult(BizCodeEnum.CODE_CAPTCHA_ERROR);
        }
    }

    private String getKaptchaKey(HttpServletRequest request) {
        String ip = CommonUtil.getIpAddr(request);
        String userAgent = request.getHeader("User-Agent");
        String key = "account-service:captcha:" + CommonUtil.MD5(ip + userAgent);
        log.info("验证码key:{}", key);
        return key;
    }
}
