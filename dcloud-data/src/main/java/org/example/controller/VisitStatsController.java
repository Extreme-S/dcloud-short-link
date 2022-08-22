package org.example.controller;

import org.example.controller.request.*;
import org.example.enums.BizCodeEnum;
import org.example.service.VisitStatsService;
import org.example.util.JsonData;
import org.example.vo.VisitStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/visit_stats/v1")
public class VisitStatsController {

    @Autowired
    private VisitStatsService statsService;


    @RequestMapping("page_record")
    public JsonData pageVisitRecord(@RequestBody VisitRecordPageRequest request) {
        int total = request.getSize() * request.getPage();        //条数限制
        if (total > 1000) {
            return JsonData.buildResult(BizCodeEnum.DATA_OUT_OF_LIMIT_SIZE);
        }
        Map<String, Object> pageResult = statsService.pageVisitRecord(request);
        return JsonData.buildSuccess(pageResult);
    }


    /**
     * 查询时间范围内的，地区访问分布
     */
    @RequestMapping("region_day")
    public JsonData queryRegionWithDay(@RequestBody RegionQueryRequest request) {
        List<VisitStatsVO> list = statsService.queryRegionWithDay(request);
        return JsonData.buildSuccess(list);
    }


    /**
     * 访问趋势图
     */
    @RequestMapping("trend")
    public JsonData queryVisitTrend(@RequestBody VisitTrendQueryRequest request) {
        List<VisitStatsVO> list = statsService.queryVisitTrend(request);
        return JsonData.buildSuccess(list);
    }


    /**
     * 高频refer统计
     */
    @RequestMapping("frequent_source")
    public JsonData queryFrequentSource(@RequestBody FrequentSourceRequest request) {
        List<VisitStatsVO> list = statsService.queryFrequentSource(request);
        return JsonData.buildSuccess(list);
    }


    /**
     * 查询设备访问分布情况
     */
    @RequestMapping("device_info")
    public JsonData queryDeviceInfo(@RequestBody QueryDeviceRequest request) {
        Map<String, List<VisitStatsVO>> map = statsService.queryDeviceInfo(request);
        return JsonData.buildSuccess(map);
    }


}
