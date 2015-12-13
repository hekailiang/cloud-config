package org.squirrelframework.cloud.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.context.config.ZkPropertyPlaceholderConfigurerParser;

/**
 * Created by kailianghe on 11/9/15.
 */
public class CloudConfigNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("zk-client", new ZkClientBeanDefinitionParser());
        registerBeanDefinitionParser("zk-declarative-routing", new ZkDeclarativeRoutingParser());
        registerBeanDefinitionParser("zk-property-placeholder", new ZkPropertyPlaceholderConfigurerParser());
        registerBeanDefinitionParser("zk-jdbc-datasource", new ZkJdbcDataSourceBeanDefinitionParser());
        registerBeanDefinitionParser("zk-resource-config", new ZkResourceConfigBeanDefinitionParser());
    }
}
