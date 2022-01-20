package org.example.service;

import java.util.List;
import org.example.vo.ProductVO;

public interface ProductService {

    List<ProductVO> list();

    ProductVO findDetailById(long productId);
}
