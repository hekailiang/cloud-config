package org.squirrelframework.cloud.resource.sequence;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by kailianghe on 15/12/17.
 */
@Repository
public class ProductDao {

    @PersistenceContext
    private EntityManager em;

    public void save(Product product) {
        em.persist(product);
    }

    public void update(Product product) {
        em.merge(product);
    }

    public Product findProductById(String id) {
        return em.find(Product.class, id);
    }
}
