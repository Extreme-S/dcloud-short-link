package org.example.controller.request;

import lombok.Data;


@Data
public class UseTrafficRequest {

    /**
     * 账号
     */
    private Long accountNo;

    /**
     * 业务id, 短链码
     */
    private String bizId;


}
