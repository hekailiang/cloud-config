package org.squirrelframework.cloud.resource.sequence;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squirrelframework.cloud.annotation.RoutingKey;
import org.squirrelframework.cloud.annotation.RoutingVariable;

/**
 * Created by kailianghe on 15/12/25.
 */
@Service
public class ProductService2 {
    @Autowired
    private ProductDao productDao;

    @Transactional
    @RoutingKey( "#{ #product.customerId }" )
    public String saveProduct(@RoutingVariable("product") Product product) throws Exception {
        if(product.getId() == null) {
            product.setId(RandomStringUtils.randomAlphabetic(16));
            productDao.save(product);
        } else {
            productDao.update(product);
        }
        return product.getId();
    }

    @RoutingKey( "#{ #customerId }" )
    public Product findProductById(String prodId, @RoutingVariable("customerId") long customerId) {
        return productDao.findProductById(prodId);
    }
}
