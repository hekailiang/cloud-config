package org.squirrelframework.cloud.resource.sequence;

/**
 * Created by kailianghe on 15/12/17.
 */
public class Product {

    private String id;

    private final long customerId;

    public Product(long customerId) {
        this.customerId = customerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCustomerId() {
        return customerId;
    }
}
