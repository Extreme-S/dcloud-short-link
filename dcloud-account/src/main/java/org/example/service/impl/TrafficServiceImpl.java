package org.example.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.example.controller.request.TrafficPageRequest;
import org.example.enums.EventMessageType;
import org.example.feign.ProductFeignService;
import org.example.interceptor.LoginInterceptor;
import org.example.manager.TrafficManager;
import org.example.model.EventMessage;
import org.example.model.LoginUser;
import org.example.model.TrafficDO;
import org.example.service.TrafficService;
import org.example.util.JsonData;
import org.example.util.JsonUtil;
import org.example.vo.ProductVO;
import org.example.vo.TrafficVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
public class TrafficServiceImpl implements TrafficService {

    @Autowired
    private TrafficManager trafficManager;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void handleTrafficMessage(EventMessage eventMessage) {
        Long accountNo = eventMessage.getAccountNo();
        String messageType = eventMessage.getEventMessageType();
        if (EventMessageType.PRODUCT_ORDER_PAY.name().equalsIgnoreCase(messageType)) {
            //订单已经支付，新增流量包
            String content = eventMessage.getContent();
            Map<String, Object> orderInfoMap = JsonUtil.json2Obj(content, Map.class);
            //还原订单商品信息
            String outTradeNo = (String) orderInfoMap.get("outTradeNo");
            Integer buyNum = (Integer) orderInfoMap.get("buyNum");
            String productStr = (String) orderInfoMap.get("product");
            ProductVO productVO = JsonUtil.json2Obj(productStr, ProductVO.class);
            log.info("商品信息:{}", productVO);

            LocalDateTime expiredDateTime = LocalDateTime.now().plusDays(productVO.getValidDay());//流量包有效期
            Date date = Date.from(expiredDateTime.atZone(ZoneId.systemDefault()).toInstant());
            //构建流量包对象
            TrafficDO trafficDO = TrafficDO.builder().accountNo(accountNo).dayLimit(productVO.getDayTimes() * buyNum)
                    .dayUsed(0).totalLimit(productVO.getTotalTimes()).pluginType(productVO.getPluginType())
                    .level(productVO.getLevel()).productId(productVO.getId()).outTradeNo(outTradeNo).expiredDate(date)
                    .build();
            int rows = trafficManager.add(trafficDO);
            log.info("消费消息新增流量包:rows={},trafficDO={}", rows, trafficDO);
        } else if (EventMessageType.TRAFFIC_FREE_INIT.name().equalsIgnoreCase(messageType)) {
            //发放免费流量包
            long productId = Long.parseLong(eventMessage.getBizId());
            JsonData jsonData = productFeignService.detail(productId);
            ProductVO productVO = jsonData.getData(new TypeReference<ProductVO>() {});
            //构建流量包对象
            TrafficDO trafficDO = TrafficDO.builder()
                    .accountNo(accountNo)
                    .dayLimit(productVO.getDayTimes())
                    .dayUsed(0)
                    .totalLimit(productVO.getTotalTimes())
                    .pluginType(productVO.getPluginType())
                    .level(productVO.getLevel())
                    .productId(productVO.getId())
                    .outTradeNo("free_init")
                    .expiredDate(new Date())
                    .build();
            trafficManager.add(trafficDO);
        }
    }

    @Override
    public Map<String, Object> pageAvailable(TrafficPageRequest pageReq) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        IPage<TrafficDO> trafficDOIPage = trafficManager.pageAvailable(pageReq.getPage(), pageReq.getSize(), loginUser.getAccountNo());
        List<TrafficDO> trafficDOS = trafficDOIPage.getRecords();        //获取流量包列表
        List<TrafficVO> trafficVOS = trafficDOS.stream().map(this::beanProcess).collect(Collectors.toList());
        Map<String, Object> pageMap = new HashMap<>(3);
        pageMap.put("total_record", trafficDOIPage.getTotal());
        pageMap.put("total_page", trafficDOIPage.getPages());
        pageMap.put("current_data", trafficVOS);
        return pageMap;
    }

    @Override
    public TrafficVO detail(long trafficId) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        TrafficDO trafficDO = trafficManager.findByIdAndAccountNo(trafficId, loginUser.getAccountNo());
        return beanProcess(trafficDO);
    }

    private TrafficVO beanProcess(TrafficDO trafficDO) {
        TrafficVO trafficVO = new TrafficVO();
        BeanUtils.copyProperties(trafficDO, trafficVO);
        return trafficVO;
    }


}
