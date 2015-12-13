package org.squirrelframework.cloud.resource;

/**
 * Created by kailianghe on 9/22/15.
 */
public interface RoutingSupport<T> {
    T get(String routingKey);
}
