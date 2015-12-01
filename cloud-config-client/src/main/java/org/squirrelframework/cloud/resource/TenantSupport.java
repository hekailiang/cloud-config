package org.squirrelframework.cloud.resource;

/**
 * Created by kailianghe on 9/22/15.
 */
public interface TenantSupport<T> {
    T get(String tenantId);
}
