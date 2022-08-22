package org.example.service;

import org.example.controller.request.*;
import org.example.vo.VisitStatsVO;

import java.util.List;
import java.util.Map;

public interface VisitStatsService {

    Map<String,Object> pageVisitRecord(VisitRecordPageRequest request);

    List<VisitStatsVO> queryRegionWithDay(RegionQueryRequest request);

    List<VisitStatsVO> queryVisitTrend(VisitTrendQueryRequest request);

    List<VisitStatsVO> queryFrequentSource(FrequentSourceRequest request);

    Map<String,List<VisitStatsVO>> queryDeviceInfo(QueryDeviceRequest request);
}
