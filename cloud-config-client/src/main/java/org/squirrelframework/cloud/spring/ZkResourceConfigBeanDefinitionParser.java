package org.squirrelframework.cloud.spring;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.squirrelframework.cloud.resource.SimpleResourceConfigFactoryBean;
import org.squirrelframework.cloud.resource.TenantAwareResourceConfigFactoryBean;
import org.squirrelframework.cloud.utils.CloudConfigCommon;
import org.w3c.dom.Element;
import static org.squirrelframework.cloud.utils.CloudConfigCommon.getSafeBoolean;

/**
 * Created by kailianghe on 11/10/15.
 */
public class ZkResourceConfigBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    protected Class<?> getBeanClass(Element element) {
        Boolean routingSupport = getSafeBoolean(element.getAttribute("routing-support"));
        return routingSupport ? TenantAwareResourceConfigFactoryBean.class : SimpleResourceConfigFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        String path = element.getAttribute("path");
        String resourceType = element.getAttribute("resource-type");
        String validatorBeanName = element.getAttribute("validator-ref");
        if(!parserContext.getRegistry().containsBeanDefinition(validatorBeanName)) {
            validatorBeanName = null;
        }

        builder.addPropertyValue("path", path);
        builder.addPropertyValue("resourceType", resourceType);
        if(StringUtils.hasLength(validatorBeanName)) {
            builder.addPropertyReference("validator", validatorBeanName);
        }
        builder.addPropertyReference("client", CloudConfigCommon.ZK_CLIENT_BEAN_NAME);
    }
}
