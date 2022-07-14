package org.example.controller;

import java.util.List;
import org.example.service.ProductService;
import org.example.util.JsonData;
import org.example.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product/v1")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 查看商品列表接口
     */
    @GetMapping("list")
    public JsonData list(){
        List<ProductVO> list = productService.list();
        return JsonData.buildSuccess(list);
    }

    /**
     * 查看商品详情
     */
    @GetMapping("detail/{product_id}")
    public JsonData detail(@PathVariable("product_id") long productId){
        ProductVO productVO = productService.findDetailById(productId);
        return JsonData.buildSuccess(productVO);
    }


}
