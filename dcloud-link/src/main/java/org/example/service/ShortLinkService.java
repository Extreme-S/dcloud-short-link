package org.example.service;

import org.example.controller.request.ShortLinkAddRequest;
import org.example.model.EventMessage;
import org.example.util.JsonData;
import org.example.vo.ShortLinkVO;


public interface ShortLinkService {

    /**
     * 解析短链
     */
    ShortLinkVO parseShortLinkCode(String shortLinkCode);

    /**
     * 创建短链
     */
    JsonData createShortLink(ShortLinkAddRequest request);

    /**
     * 处理新增短链消息
     */
    boolean handlerAddShortLink(EventMessage eventMessage);

}
