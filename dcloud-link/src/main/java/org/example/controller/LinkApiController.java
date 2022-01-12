package org.example.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.ShortLinkStateEnum;
import org.example.service.ShortLinkService;
import org.example.util.CommonUtil;
import org.example.vo.ShortLinkVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@Controller
@Slf4j
public class LinkApiController {


    @Autowired
    private ShortLinkService shortLinkService;


    /**
     * 解析 301还是302，这边是返回http code是302
     * 知识点一，为什么要用 301 跳转而不是 302 呐？
     * 301 是永久重定向，302 是临时重定向。
     * 短地址一经生成就不会变化，所以用 301 是同时对服务器压力也会有一定减少
     * 但是如果使用了 301，无法统计到短地址被点击的次数。
     * 所以选择302虽然会增加服务器压力，但是有很多数据可以获取进行分析
     */
    @GetMapping(path = "/{shortLinkCode}")
    public void dispatch(@PathVariable(name = "shortLinkCode") String shortLinkCode, HttpServletRequest request,
        HttpServletResponse response) {
        try {
            log.info("短链码:{}", shortLinkCode);
            //判断短链码是否合规
            if (isLetterDigit(shortLinkCode)) {
                ShortLinkVO shortLinkVO = shortLinkService.parseShortLinkCode(shortLinkCode);
                //判断是否过期和可用
                if (isVisitable(shortLinkVO)) {
                    // 移除url前缀，HTTP 302跳转
                    String originalUrl = CommonUtil.removeUrlPrefix(shortLinkVO.getOriginalUrl());
                    response.setHeader("Location", originalUrl);
                    response.setStatus(HttpStatus.FOUND.value());
                } else {
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    /**
     * 判断短链是否可用
     *
     * @param shortLinkVO
     * @return
     */
    private static boolean isVisitable(ShortLinkVO shortLinkVO) {
        if ((shortLinkVO != null && shortLinkVO.getExpired().getTime() > CommonUtil.getCurrentTimestamp())) {
            if (ShortLinkStateEnum.ACTIVE.name().equalsIgnoreCase(shortLinkVO.getState())) {
                return true;
            }
        } else if ((shortLinkVO != null && shortLinkVO.getExpired().getTime() == -1)) {
            if (ShortLinkStateEnum.ACTIVE.name().equalsIgnoreCase(shortLinkVO.getState())) {
                return true;
            }
        }
        return false;
    }


    /**
     * 判断短链是否仅包括数字和字母
     */
    private static boolean isLetterDigit(String str) {
        String regex = "^[a-z0-9A-Z]+$";
        return str.matches(regex);
    }


}
