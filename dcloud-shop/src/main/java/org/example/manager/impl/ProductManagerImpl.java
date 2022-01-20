package org.example.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import groovy.util.logging.Slf4j;
import java.util.List;
import org.example.manager.ProductManager;
import org.example.mapper.ProductMapper;
import org.example.model.ProductDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class ProductManagerImpl implements ProductManager {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<ProductDO> list() {
        return productMapper.selectList(null);
    }

    @Override
    public ProductDO findDetailById(long productId) {
        return productMapper.selectOne(new QueryWrapper<ProductDO>()
            .eq("id", productId));
    }
}
