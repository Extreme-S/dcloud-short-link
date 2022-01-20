package org.example.service.impl;

import groovy.util.logging.Slf4j;
import java.util.List;
import java.util.stream.Collectors;
import org.example.manager.ProductManager;
import org.example.model.ProductDO;
import org.example.service.ProductService;
import org.example.vo.ProductVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductManager productManager;

    @Override
    public List<ProductVO> list() {
        List<ProductDO> list = productManager.list();
        return list.stream().map(this::beanProcess).collect(Collectors.toList());
    }

    @Override
    public ProductVO findDetailById(long productId) {
        ProductDO productDO = productManager.findDetailById(productId);
        return beanProcess(productDO);
    }

    private ProductVO beanProcess(ProductDO productDO) {
        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(productDO, productVO);
        return productVO;
    }
}
