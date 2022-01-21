package org.example.biz;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.example.ShopApplication;
import org.example.manager.ProductOrderManager;
import org.example.model.ProductDO;
import org.example.model.ProductOrderDO;
import org.example.util.CommonUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = ShopApplication.class)

@Slf4j
public class ProductOrderTest {

    @Autowired
    private ProductOrderManager productOrderManager;

    @Test
    public void testAdd() {
        for (long i = 0L; i < 5; i++) {
            ProductOrderDO productOrderDO = ProductOrderDO.builder()
                .outTradeNo(CommonUtil.generateUUID())
                .payAmount(new BigDecimal(11))
                .state("NEW")
                .nickname("小滴课堂-老王 i" + i)
                .accountNo(100L)
                .del(0)
                .productId(2L)
                .build();
            productOrderManager.add(productOrderDO);
        }
    }

    @Test
    public void test() {
        List<ProductDO> list = new ArrayList<>();
        ProductDO product1 = new ProductDO(1L, "测试1");
        ProductDO product2 = new ProductDO(8L, "测试2");
        ProductDO product3 = new ProductDO(5L, "测试3");
        ProductDO product4 = new ProductDO(7L, "测试4");
        ProductDO product5 = new ProductDO(3L, "测试5");
        list.add(product1);
        list.add(product2);
        list.add(product3);
        list.add(product4);
        list.add(product5);
        List<ProductDO> sortedList = list.stream().sorted(Comparator.comparing(ProductDO::getId))
            .collect(Collectors.toList());
        log.info(sortedList.toString());
    }


    @Test
    public void testPage() {
        Map<String, Object> page = productOrderManager.page(1, 2, 10L, null);
        log.info(page.toString());
    }

}

