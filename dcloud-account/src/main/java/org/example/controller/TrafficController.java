package org.example.controller;

import org.example.controller.request.TrafficPageRequest;
import org.example.controller.request.UseTrafficRequest;
import org.example.service.TrafficService;
import org.example.util.JsonData;
import org.example.vo.TrafficVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@RestController
@RequestMapping("/api/traffic/v1")
public class TrafficController {

    @Autowired
    private TrafficService trafficService;


    /**
     * 使用流量包API
     */
    @PostMapping("reduce")
    public JsonData useTraffic(@RequestBody UseTrafficRequest useTrafficRequest, HttpServletRequest request) {
        //具体使用流量包逻辑  TODO
        return JsonData.buildSuccess();
    }


    /**
     * 分页查询流量包列表，查看可用的流量包
     */
    @RequestMapping("page")
    public JsonData pageAvailable(@RequestBody TrafficPageRequest request) {
        Map<String, Object> pageMap = trafficService.pageAvailable(request);
        return JsonData.buildSuccess(pageMap);
    }


    /**
     * 查找某个流量包详情
     */
    @GetMapping("/detail/{trafficId}")
    public JsonData detail(@PathVariable("trafficId") long trafficId) {
        TrafficVO trafficVO = trafficService.detail(trafficId);
        return JsonData.buildSuccess(trafficVO);
    }


}
