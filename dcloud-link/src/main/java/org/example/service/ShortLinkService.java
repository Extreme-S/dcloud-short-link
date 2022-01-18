package org.example.service;

import java.util.Map;
import org.example.controller.request.ShortLinkAddRequest;
import org.example.controller.request.ShortLinkDelRequest;
import org.example.controller.request.ShortLinkPageRequest;
import org.example.controller.request.ShortLinkUpdateRequest;
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

    /**
     * 分页查找短链
     */
    Map<String, Object> pageByGroupId(ShortLinkPageRequest request);

    /**
     * 删除短链
     */
    JsonData del(ShortLinkDelRequest request);

    /**
     * 更新
     */
    JsonData update(ShortLinkUpdateRequest request);
}
