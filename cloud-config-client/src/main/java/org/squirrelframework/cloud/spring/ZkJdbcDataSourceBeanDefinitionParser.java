package org.squirrelframework.cloud.spring;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.squirrelframework.cloud.resource.database.BoneCPDataSourceFactoryBean;
import org.squirrelframework.cloud.resource.database.C3P0DataSourceFactoryBean;
import org.squirrelframework.cloud.resource.database.DruidDataSourceFactoryBean;
import org.squirrelframework.cloud.resource.database.RoutingDataSourceFactoryBean;
import static org.squirrelframework.cloud.utils.CloudConfigCommon.getSafeBoolean;

import org.squirrelframework.cloud.utils.CloudConfigCommon;
import org.w3c.dom.Element;

/**
 * Created by kailianghe on 11/10/15.
 */
public class ZkJdbcDataSourceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class getBeanClass(Element element) {
        String resourceType = element.getAttribute("resource-type");
        Boolean multiTenancyAware = getSafeBoolean(element.getAttribute("multi-tenancy-aware"));
        if (multiTenancyAware) {
            return RoutingDataSourceFactoryBean.class;
        } else if ("BoneCP".equals(resourceType)) {
            return BoneCPDataSourceFactoryBean.class;
        } else if ("C3P0".equals(resourceType)) {
            return C3P0DataSourceFactoryBean.class;
        } else if ("Druid".equals(resourceType)) {
            return DruidDataSourceFactoryBean.class;
        }
        throw new UnsupportedOperationException("Unsupported resource type "+resourceType);
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        String path = element.getAttribute("path");
        String resourceType = element.getAttribute("resource-type");
        Boolean multiTenancyAware = getSafeBoolean(element.getAttribute("multi-tenancy-aware"));
        String validatorBeanName = element.getAttribute("validator-ref");
        String fallbackDsPath = element.getAttribute("fallback");
        if(!parserContext.getRegistry().containsBeanDefinition(validatorBeanName)) {
            validatorBeanName = null;
        }
        if (multiTenancyAware) {
            if ("BoneCP".equals(resourceType)) {
                builder.addPropertyValue("dataSourceFactoryBeanClass", BoneCPDataSourceFactoryBean.class.getName());
            } else if ("C3P0".equals(resourceType)) {
                builder.addPropertyValue("dataSourceFactoryBeanClass", C3P0DataSourceFactoryBean.class.getName());
            } else if ("C3P0".equals(resourceType)) {
                builder.addPropertyValue("dataSourceFactoryBeanClass", DruidDataSourceFactoryBean.class.getName());
            } else {
                throw new UnsupportedOperationException("Unsupported resource type "+resourceType);
            }
            String resolverBeanName = element.getAttribute("resolver-ref");
            if(!parserContext.getRegistry().containsBeanDefinition(resolverBeanName)) {
                throw new IllegalArgumentException("Undefined routing key resolver");
            }
            builder.addPropertyReference("resolver", resolverBeanName);

            builder.addPropertyValue("path", path);
            builder.addPropertyReference("client", CloudConfigCommon.ZK_CLIENT_BEAN_NAME);

            if(StringUtils.hasLength(fallbackDsPath)) {
                builder.addPropertyValue("fallbackDataSourcePath", fallbackDsPath);
            }
        } else {
            builder.addPropertyValue("configPath", path);
        }
        if(StringUtils.hasLength(validatorBeanName)) {
            builder.addPropertyReference("validator", validatorBeanName);
        }
    }
}
