package org.squirrelframework.cloud.resource.sequence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by kailianghe on 15/12/17.
 */
@Entity
@Table(name="PRODUCT")
public class Product {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "customer_id", nullable = false)
    private long customerId;

    @Column(name = "name")
    private String name;

    public Product(long customerId, String name) {
        this.customerId = customerId;
        this.name = name;
    }

    public Product() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;

        Product product = (Product) o;

        if (customerId != product.customerId) return false;
        if (id != null ? !id.equals(product.id) : product.id != null) return false;
        return !(name != null ? !name.equals(product.name) : product.name != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (customerId ^ (customerId >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
