package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.enums.LogTypeEnum;
import org.example.model.LogRecord;
import org.example.service.LogService;
import org.example.util.CommonUtil;
import org.example.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


@Service
@Slf4j
public class LogServiceImpl implements LogService {


    private static final String TOPIC_NAME = "ods_link_visit_topic";

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * ====================用于测试====================
     */
    private static List<String> ipList = new ArrayList<>();

    static {
        ipList.add("14.197.9.110");         //深圳
        ipList.add("113.68.152.139");       //广州
    }

    private static List<String> refererList = new ArrayList<>();

    static {
        refererList.add("https://taobao.com");
        refererList.add("https://douyin.com");
    }

    private Random random = new Random();


    @Override
    public void recordShortLinkLog(HttpServletRequest request, String shortLinkCode, Long accountNo) {

//        String ip = CommonUtil.getIpAddr(request);//ip、浏览器信息
        String ip = ipList.get(random.nextInt(ipList.size()));

        Map<String, String> headerMap = CommonUtil.getAllRequestHeader(request);//全部请求头
        Map<String, String> availableMap = new HashMap<>();
        availableMap.put("user-agent", headerMap.get("user-agent"));

//        availableMap.put("referer", headerMap.get("referer"));
        availableMap.put("referer",refererList.get(random.nextInt(refererList.size())));

        availableMap.put("accountNo", accountNo.toString());

        LogRecord logRecord = LogRecord.builder()
                .event(LogTypeEnum.SHORT_LINK_TYPE.name())  //日志类型
                .data(availableMap)                         //日志内容
                .ip(ip)                                     //客户端ip
                .ts(CommonUtil.getCurrentTimestamp())       //产生时间
                .bizId(shortLinkCode).build();              //业务唯一标识

        String jsonLog = JsonUtil.obj2Json(logRecord);
        log.info(jsonLog);
        kafkaTemplate.send(TOPIC_NAME, jsonLog);
    }
}
