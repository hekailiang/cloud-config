package org.squirrelframework.cloud.resource.sequence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.squirrelframework.cloud.annotation.RoutingKey;

/**
 * Created by kailianghe on 15/12/18.
 */
@Service
public class ContainerService {

    @Autowired
    private ProductService productService;

    // @Transactional
    // @Transaction需要在Routing key的最内层声明, 因为在@Transactional在创建Transaction时需要指定DataSource.
    // 如果在RoutingKey外层将因为缺少routing key信息, 无法定位正确地DataSource
    @RoutingKey("a")
    public String saveProduct(Product product) throws Exception {
        return productService.saveProduct(product);
    }

    @RoutingKey("a")
    public Product findProductById(String id) {
        return productService.findProductById(id);
    }
}
