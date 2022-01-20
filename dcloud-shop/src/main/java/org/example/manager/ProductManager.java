package org.example.manager;

import java.util.List;
import org.example.model.ProductDO;

public interface ProductManager {

    List<ProductDO> list();

    ProductDO findDetailById(long productId);
}
