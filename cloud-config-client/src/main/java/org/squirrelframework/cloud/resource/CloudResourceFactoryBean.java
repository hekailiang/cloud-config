package org.squirrelframework.cloud.resource;

import org.springframework.beans.factory.FactoryBean;

/**
 * Created by kailianghe on 9/6/15.
 */
public interface CloudResourceFactoryBean<T, C extends CloudResourceConfig> extends FactoryBean<T> {
    void setConfig(C config);
}
