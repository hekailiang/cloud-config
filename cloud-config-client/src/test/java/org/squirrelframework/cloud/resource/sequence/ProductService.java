package org.squirrelframework.cloud.resource.sequence;

import org.springframework.beans.factory.annotation.Autowired;
import org.squirrelframework.cloud.annotation.RoutingKey;
import org.squirrelframework.cloud.annotation.RoutingVariable;

/**
 * Created by kailianghe on 15/12/17.
 */
public class ProductService {

    @Autowired
    private SequenceGenerator sequenceGenerator;

    @RoutingKey("#{ " +
            " #product?.id?.subString(8, 10)" +
            " ?:" +
            " T(java.lang.String).format(\"%02d\", #product.customerId%2+1) " +
            "}")
    public String saveProduct(@RoutingVariable("product") Product product) throws Exception {
        if(product.getId() == null) {
            String productId = sequenceGenerator.next("product");
            product.setId(productId);
        }
        return product.getId();
    }
}
