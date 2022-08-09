package org.example.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.example.config.RabbitMQConfig;
import org.example.constant.RedisKey;
import org.example.controller.request.TrafficPageRequest;
import org.example.controller.request.UseTrafficRequest;
import org.example.enums.BizCodeEnum;
import org.example.enums.EventMessageType;
import org.example.enums.TaskStateEnum;
import org.example.exception.BizException;
import org.example.feign.ProductFeignService;
import org.example.feign.ShortLinkFeignService;
import org.example.interceptor.LoginInterceptor;
import org.example.manager.TrafficManager;
import org.example.manager.TrafficTaskManager;
import org.example.model.EventMessage;
import org.example.model.LoginUser;
import org.example.model.TrafficDO;
import org.example.model.TrafficTaskDO;
import org.example.service.TrafficService;
import org.example.util.JsonData;
import org.example.util.JsonUtil;
import org.example.util.TimeUtil;
import org.example.vo.ProductVO;
import org.example.vo.TrafficVO;
import org.example.vo.UseTrafficVO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@Slf4j
public class TrafficServiceImpl implements TrafficService {

    @Autowired
    private TrafficManager trafficManager;

    @Autowired
    private TrafficTaskManager trafficTaskManager;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ShortLinkFeignService shortLinkFeignService;


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
            //新增流量包，删除Redis中该用户流量包的 天剩余使用次数
            redisTemplate.delete(String.format(RedisKey.DAY_TOTAL_TRAFFIC, accountNo));
        } else if (EventMessageType.TRAFFIC_FREE_INIT.name().equalsIgnoreCase(messageType)) {
            //发放免费流量包
            JsonData jsonData = productFeignService.detail(Long.parseLong(eventMessage.getBizId()));
            ProductVO productVO = jsonData.getData(new TypeReference<>() {
            });
            //构建流量包对象
            TrafficDO trafficDO = TrafficDO.builder().accountNo(accountNo).dayLimit(productVO.getDayTimes()).dayUsed(0)
                    .totalLimit(productVO.getTotalTimes()).pluginType(productVO.getPluginType()).level(productVO.getLevel())
                    .productId(productVO.getId()).outTradeNo("free_init").expiredDate(new Date()).build();
            trafficManager.add(trafficDO);
        } else if (EventMessageType.TRAFFIC_USED.name().equalsIgnoreCase(messageType)) {
            //流量包使用，检查是否成功使用
            //检查task是否存在
            //检查短链是否成功
            //如果不成功，则恢复流量包
            //删除task (也可以更新task状态，定时删除就行)
            Long trafficTaskId = Long.valueOf(eventMessage.getBizId());
            TrafficTaskDO trafficTaskDO = trafficTaskManager.findByIdAndAccountNo(trafficTaskId, accountNo);
            if (trafficTaskDO != null && trafficTaskDO.getLockState().equalsIgnoreCase(TaskStateEnum.LOCK.name())) {
                JsonData jsonData = shortLinkFeignService.check(trafficTaskDO.getBizId());
                if (jsonData.getCode() != 0) {
                    log.error("创建短链失败，流量包回滚");
                    String useDateStr = TimeUtil.format(trafficTaskDO.getGmtCreate(), "yyyy-MM-dd");
                    trafficManager.releaseUsedTimes(accountNo, trafficTaskDO.getTrafficId(), 1, useDateStr);
                    //恢复流量包，应该删除这个key（也可以让这个key递增）
                    String totalTrafficTimesKey = String.format(RedisKey.DAY_TOTAL_TRAFFIC, accountNo);
                    redisTemplate.delete(totalTrafficTimesKey);
                }
                //多种方式处理task：不立刻删除，可以更新状态，然后定时删除也行
                trafficTaskManager.deleteByIdAndAccountNo(trafficTaskId, accountNo);
            }
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

    @Override
    public boolean deleteExpireTraffic() {
        return trafficManager.deleteExpireTraffic();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public JsonData reduce(UseTrafficRequest trafficRequest) {
        Long accountNo = trafficRequest.getAccountNo();
        UseTrafficVO useTrafficVO = processTrafficList(accountNo); //处理流量包，筛选出未更新流量包，当前使用的流量包
        log.info("今天可用总次数:{},当前使用流量包:{}", useTrafficVO.getDayTotalLeftTimes(), useTrafficVO.getCurrentTrafficDO());
        log.info("待更新流量包列表:{}", useTrafficVO.getUnUpdatedTrafficIds());
        if (useTrafficVO.getCurrentTrafficDO() == null)
            return JsonData.buildResult(BizCodeEnum.TRAFFIC_REDUCE_FAIL);
        if (!CollectionUtils.isEmpty(useTrafficVO.getUnUpdatedTrafficIds())) {//更新今日待更新的流量包
            trafficManager.batchUpdateUsedTimes(accountNo, useTrafficVO.getUnUpdatedTrafficIds());
        }
        //扣减当前流量包的当天剩余使用次数
        int rows = trafficManager.addDayUsedTimes(accountNo, useTrafficVO.getCurrentTrafficDO().getId(), 1);
        if (rows != 1) throw new BizException(BizCodeEnum.TRAFFIC_REDUCE_FAIL); //扣减流量包失败，抛出异常

        TrafficTaskDO trafficTaskDO = TrafficTaskDO.builder()
                .accountNo(accountNo).bizId(trafficRequest.getBizId()).useTimes(1).lockState(TaskStateEnum.LOCK.name())
                .trafficId(useTrafficVO.getCurrentTrafficDO().getId()).build();
        trafficTaskManager.add(trafficTaskDO);
        //向redis中设置总流量包次数，短链服务那边递减即可；如果有新增流量包，则删除这个key
        long leftSeconds = TimeUtil.getRemainSecondsOneDay(new Date());
        String totalTrafficTimesKey = String.format(RedisKey.DAY_TOTAL_TRAFFIC, accountNo);
        redisTemplate.opsForValue().set(
                totalTrafficTimesKey, useTrafficVO.getDayTotalLeftTimes() - 1, leftSeconds, TimeUnit.SECONDS);
        //构造延迟消息(用于异常回滚)并发送给MQ
        EventMessage trafficUseEventMessage = EventMessage.builder().accountNo(accountNo)
                .bizId(trafficTaskDO.getId().toString()).eventMessageType(EventMessageType.TRAFFIC_USED.name()).build();
        rabbitTemplate.convertAndSend(rabbitMQConfig.getTrafficEventExchange(),
                rabbitMQConfig.getTrafficReleaseDelayRoutingKey(), trafficUseEventMessage);
        return JsonData.buildSuccess();
    }

    private UseTrafficVO processTrafficList(Long accountNo) {
        List<TrafficDO> list = trafficManager.selectAvailableTraffics(accountNo);        //全部流量包
        if (CollectionUtils.isEmpty(list)) throw new BizException(BizCodeEnum.TRAFFIC_EXCEPTION);
        int dayTotalLeftTimes = 0;//当天流量包剩余可用总次数
        TrafficDO currentTrafficDO = null;//当次使用的流量包
        List<Long> unUpdatedTrafficIds = new ArrayList<>();//没过期，但是今天没更新的流量包id列表
        String todayStr = TimeUtil.format(new Date(), "yyyy-MM-dd");//今天日期
        for (TrafficDO trafficDO : list) {
            String trafficUpdateDate = TimeUtil.format(trafficDO.getGmtModified(), "yyyy-MM-dd");
            if (todayStr.equalsIgnoreCase(trafficUpdateDate)) {
                //如果当前流量包 已更新
                int curTrafficDOLeftTimes = trafficDO.getDayLimit() - trafficDO.getDayUsed(); //当前流量包当天剩余使用次数
                dayTotalLeftTimes += curTrafficDOLeftTimes;
                if (curTrafficDOLeftTimes > 0 && currentTrafficDO == null) currentTrafficDO = trafficDO;//选取当次使用流量包
            } else {
                //如果当前流量包 未更新
                dayTotalLeftTimes += trafficDO.getDayLimit();
                unUpdatedTrafficIds.add(trafficDO.getId());//记录未更新的流量包
                if (currentTrafficDO == null) currentTrafficDO = trafficDO;//选取当次使用流量包
            }
        }
        return new UseTrafficVO(dayTotalLeftTimes, currentTrafficDO, unUpdatedTrafficIds);
    }

    private TrafficVO beanProcess(TrafficDO trafficDO) {
        TrafficVO trafficVO = new TrafficVO();
        BeanUtils.copyProperties(trafficDO, trafficVO);
        return trafficVO;
    }


}
