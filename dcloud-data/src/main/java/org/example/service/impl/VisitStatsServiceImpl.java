package org.example.service.impl;

import org.example.controller.request.*;
import org.example.enums.DateTimeFieldEnum;
import org.example.enums.QueryDeviceEnum;
import org.example.interceptor.LoginInterceptor;
import org.example.controller.request.*;
import org.example.mapper.VisitStatsMapper;
import org.example.model.VisitStatsDO;
import org.example.service.VisitStatsService;
import org.example.vo.VisitStatsVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class VisitStatsServiceImpl implements VisitStatsService {

    @Autowired
    private VisitStatsMapper visitStatsMapper;


    @Override
    public Map<String, Object> pageVisitRecord(VisitRecordPageRequest request) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        Map<String, Object> data = new HashMap<>();
        String code = request.getCode();

        int page = request.getPage();
        int size = request.getSize();
        int count = visitStatsMapper.countTotal(code, accountNo);
        int from = (page - 1) * size;

        List<VisitStatsDO> list = visitStatsMapper.pageVisitRecord(code, accountNo, from, size);

        List<VisitStatsVO> visitStatsVOS = list.stream().map(this::beanProcess).collect(Collectors.toList());

        data.put("total", count);
        data.put("current_page", page);

        //计算总页数
        int totalPage = 0;
        if (count % size == 0) {
            totalPage = count / size;
        } else {
            totalPage = count / size + 1;
        }
        data.put("total_page", totalPage);

        data.put("data", visitStatsVOS);
        return data;
    }


    @Override
    public List<VisitStatsVO> queryRegionWithDay(RegionQueryRequest request) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        List<VisitStatsDO> list = visitStatsMapper.queryRegionVisitStatsWithDay(
                request.getCode(), accountNo, request.getStartTime(), request.getEndTime());
        return list.stream().map(this::beanProcess).collect(Collectors.toList());
    }

    @Override
    public List<VisitStatsVO> queryVisitTrend(VisitTrendQueryRequest request) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        String code = request.getCode();
        String type = request.getType();
        String startTime = request.getStartTime();
        String endTime = request.getEndTime();
        List<VisitStatsDO> list = new ArrayList<>();
        if (DateTimeFieldEnum.DAY.name().equalsIgnoreCase(type)) {
            list = visitStatsMapper.queryVisitTrendWithMultiDay(code, accountNo, startTime, endTime);
        } else if (DateTimeFieldEnum.HOUR.name().equalsIgnoreCase(type)) {
            list = visitStatsMapper.queryVisitTrendWithHour(code, accountNo, startTime);
        } else if (DateTimeFieldEnum.MINUTE.name().equalsIgnoreCase(type)) {
            list = visitStatsMapper.queryVisitTrendWithMinute(code, accountNo, startTime, endTime);
        }
        return list.stream().map(this::beanProcess).collect(Collectors.toList());
    }

    @Override
    public List<VisitStatsVO> queryFrequentSource(FrequentSourceRequest request) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        String code = request.getCode();
        String startTime = request.getStartTime();
        String endTime = request.getEndTime();
        List<VisitStatsDO> list = visitStatsMapper.queryFrequentSource(code, accountNo, startTime, endTime, 10);
        return list.stream().map(this::beanProcess).collect(Collectors.toList());
    }


    @Override
    public Map<String, List<VisitStatsVO>> queryDeviceInfo(QueryDeviceRequest request) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        String code = request.getCode();
        String startTime = request.getStartTime();
        String endTime = request.getEndTime();
        String os = QueryDeviceEnum.OS.name().toLowerCase();
        String browser = QueryDeviceEnum.BROWSER.name().toLowerCase();
        String device = QueryDeviceEnum.DEVICE.name().toLowerCase();
        List<VisitStatsDO> osList = visitStatsMapper.queryDeviceInfo(code, accountNo, startTime, endTime, os);
        List<VisitStatsDO> browserList = visitStatsMapper.queryDeviceInfo(code, accountNo, startTime, endTime, browser);
        List<VisitStatsDO> deviceList = visitStatsMapper.queryDeviceInfo(code, accountNo, startTime, endTime, device);
        List<VisitStatsVO> osVisitStatsVOS = osList.stream().map(this::beanProcess).collect(Collectors.toList());
        List<VisitStatsVO> browserVisitStatsVOS = browserList.stream().map(this::beanProcess).collect(Collectors.toList());
        List<VisitStatsVO> deviceVisitStatsVOS = deviceList.stream().map(this::beanProcess).collect(Collectors.toList());
        Map<String, List<VisitStatsVO>> map = new HashMap<>(3);
        map.put("os", osVisitStatsVOS);
        map.put("browser", browserVisitStatsVOS);
        map.put("device", deviceVisitStatsVOS);
        return map;
    }


    /**
     * map-struct
     */
    private VisitStatsVO beanProcess(VisitStatsDO visitStatsDO) {
        VisitStatsVO visitStatsVO = new VisitStatsVO();
        BeanUtils.copyProperties(visitStatsDO, visitStatsVO);
        return visitStatsVO;
    }

}
