package org.example.controller.request;

import lombok.Data;

@Data
public class VisitTrendQueryRequest {

    private String code;
    /**
     * 跨天、当天24小时、分钟级别
     */
    private String type;

    private String startTime;

    private String endTime;

}