package org.example.controller.request;

import lombok.Data;


@Data
public class QueryDeviceRequest {

    private String code;

    private String startTime;

    private String endTime;

}
