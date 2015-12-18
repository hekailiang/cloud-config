package org.squirrelframework.cloud.resource.sequence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squirrelframework.cloud.annotation.RoutingKey;
import org.squirrelframework.cloud.annotation.RoutingVariable;

/**
 * Created by kailianghe on 15/12/17.
 */
@Service
public class ProductService {

    @Autowired
    @Qualifier("sequence")
    private SequenceGenerator sequenceGenerator;

    @Autowired
    private ProductDao productDao;

    @Transactional
    @RoutingKey( "#{ ${sequence.product.sharding.rule} }" )
    public String saveProduct(@RoutingVariable("product") Product product) throws Exception {
        if(product.getId() == null) {
            String productId = sequenceGenerator.next("product");
            product.setId(productId);
            productDao.save(product);
        } else {
            productDao.update(product);
        }
        return product.getId();
    }

    @RoutingKey( "#{ ${sequence.product.id.sharding.rule} }" )
    public Product findProductById(@RoutingVariable("id") String id) {
        return productDao.findProductById(id);
    }

}
