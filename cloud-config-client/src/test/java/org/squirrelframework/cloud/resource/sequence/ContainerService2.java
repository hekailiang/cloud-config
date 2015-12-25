package org.squirrelframework.cloud.resource.sequence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.squirrelframework.cloud.annotation.RoutingKey;

/**
 * Created by kailianghe on 15/12/25.
 */
@Service
public class ContainerService2 {

    @Autowired
    private ProductService2 productService;

    @RoutingKey(value = "a", recordRoutingKeys = true)
    public String saveProduct(Product product) throws Exception {
        return productService.saveProduct(product);
    }

    @RoutingKey(value = "a", recordRoutingKeys = true)
    public Product findProductById(String id, long customerId) {
        return productService.findProductById(id, customerId);
    }
}
