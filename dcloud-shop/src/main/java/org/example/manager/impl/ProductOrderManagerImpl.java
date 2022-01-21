package org.example.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.example.manager.ProductOrderManager;
import org.example.mapper.ProductOrderMapper;
import org.example.model.ProductOrderDO;
import org.example.vo.ProductOrderVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ProductOrderManagerImpl implements ProductOrderManager {

    @Autowired
    private ProductOrderMapper productOrderMapper;

    @Override
    public int add(ProductOrderDO productOrderDO) {
        return productOrderMapper.insert(productOrderDO);
    }

    @Override
    public ProductOrderDO findByOutTradeNoAndAccountNo(String outTradeNo, Long accountNo) {
        return productOrderMapper.selectOne(new QueryWrapper<ProductOrderDO>()
            .eq("out_trade_no", outTradeNo)
            .eq("account_no", accountNo)
            .eq("del", 0));
    }

    @Override
    public int updateOrderPayState(String outTradeNo, Long accountNo, String newState, String oldState) {
        return productOrderMapper.update(null, new UpdateWrapper<ProductOrderDO>()
            .eq("out_trade_no", outTradeNo)
            .eq("account_no", accountNo)
            .eq("state", oldState)
            .set("state", newState));
    }


    @Override
    public Map<String, Object> page(int page, int size, Long accountNo, String state) {
        Page<ProductOrderDO> pageInfo = new Page<>(page, size);
        IPage<ProductOrderDO> orderDOIPage;
        if (StringUtils.isBlank(state)) {
            orderDOIPage = productOrderMapper.selectPage(pageInfo, new QueryWrapper<ProductOrderDO>()
                .eq("account_no", accountNo)
                .eq("del", 0));
        } else {
            orderDOIPage = productOrderMapper.selectPage(pageInfo, new QueryWrapper<ProductOrderDO>()
                .eq("account_no", accountNo)
                .eq("state", state)
                .eq("del", 0));
        }
        List<ProductOrderDO> orderDOIPageRecords = orderDOIPage.getRecords();
        List<ProductOrderVO> productOrderVOList = orderDOIPageRecords.stream().map(obj -> {
            ProductOrderVO productOrderVO = new ProductOrderVO();
            BeanUtils.copyProperties(obj, productOrderVO);
            return productOrderVO;
        }).collect(Collectors.toList());

        Map<String, Object> pageMap = new HashMap<>(3);
        pageMap.put("total_record", orderDOIPage.getTotal());
        pageMap.put("total_page", orderDOIPage.getPages());
        pageMap.put("current_data", productOrderVOList);
        return pageMap;
    }

    @Override
    public int del(Long productOrderId, Long accountNo) {
        return productOrderMapper.update(null, new UpdateWrapper<ProductOrderDO>()
            .eq("id", productOrderId)
            .eq("account_no", accountNo)
            .set("del", 1));
    }
}
