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
import org.example.service.NotifyService;
import org.example.util.CommonUtil;
import org.example.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account/v1")
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
     * 测试发送验证码接口-主要是用于对比优化前后区别
     *
     * @return
     */
    @GetMapping("send_code")
    public JsonData sendCode() {
        return JsonData.buildSuccess("自定义线程池测试");
    }

    /**
     * 获取kaptcha验证码图片
     *
     * @param request
     * @param response
     */
    @GetMapping("captcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {

        String kaptchaText = kaptchaProducer.createText();
        log.info("验证码内容:{}", kaptchaText);

        //存储redis,配置过期时间 TODO
        redisTemplate.opsForValue()
            .set(getCaptchaKey(request), kaptchaText, KAPTCHA_CODE_EXPIRED, TimeUnit.MILLISECONDS);

        BufferedImage bufferedImage = kaptchaProducer.createImage(kaptchaText);

        try (ServletOutputStream outputStream = response.getOutputStream()) {
            ImageIO.write(bufferedImage, "jpg", outputStream);
            outputStream.flush();
        } catch (IOException e) {
            log.error("获取流出错:{}", e.getMessage());
        }
    }

    private String getCaptchaKey(HttpServletRequest request) {
        String ip = CommonUtil.getIpAddr(request);
        String userAgent = request.getHeader("User-Agent");
        String key = "account-service:captcha:" + CommonUtil.MD5(ip + userAgent);
        log.info("验证码key:{}", key);
        return key;
    }
}
