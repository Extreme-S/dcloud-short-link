package org.example.service;

import javax.servlet.http.HttpServletRequest;

public interface LogService {

    /**
     * 记录日志
     */
    void recordShortLinkLog(HttpServletRequest request, String shortLinkCode, Long accountNo);

}
