package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LogRecord {

    /**
     * 客户端ip
     */
    private String ip;

    /**
     * 产生时间戳
     */
    private Long ts;

    /**
     * 日志事件类型
     */
    private String event;

    /**
     * udid(UniqueDeviceIdentifier)，是设备的唯一标识，
     */
    private String udid;

    /**
     * 业务id
     */
    private String bizId;

    /**
     * 日志内容
     */
    private Object data;

}
