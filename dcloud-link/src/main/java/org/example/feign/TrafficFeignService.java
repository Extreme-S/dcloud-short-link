package org.example.feign;

import org.example.controller.request.UseTrafficRequest;
import org.example.util.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "dcloud-account-service")
public interface TrafficFeignService {

    /**
     * 使用流量包
     */
    @PostMapping(value = "/api/traffic/v1/reduce", headers = {"rpc-token=${rpc.token}"})
    JsonData useTraffic(@RequestBody UseTrafficRequest request);

}
