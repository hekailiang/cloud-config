package org.squirrelframework.cloud.spring;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.squirrelframework.cloud.resource.codec.*;
import org.squirrelframework.cloud.utils.BeanIdGenerator;
import org.squirrelframework.cloud.utils.CloudConfigCommon;
import org.w3c.dom.Element;

import static org.squirrelframework.cloud.utils.CloudConfigCommon.getSafeBoolean;

/**
 * Created by kailianghe on 15/12/19.
 */
public class ZkResourceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class getBeanClass(Element element) {
        String resourceType = element.getAttribute("resource-type");
        boolean routingSupport = getSafeBoolean(element.getAttribute("routing-support"));
        return getFactoryBeanClassByType(resourceType, routingSupport);
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        String path = element.getAttribute("path");
        String resourceType = element.getAttribute("resource-type");
        element.setAttribute(AbstractBeanDefinitionParser.NAME_ATTRIBUTE, getBeanAliasByType(resourceType, path));

        boolean autoReload = getSafeBoolean(element.getAttribute("auto-reload"));
        boolean routingSupport = getSafeBoolean(element.getAttribute("routing-support"));
        String validatorBeanName = element.getAttribute("validator-ref");
        if(routingSupport) {
            String fallbackRsPath = element.getAttribute("fallback");
            String resolverBeanName = element.getAttribute("resolver-ref");
            if(!parserContext.getRegistry().containsBeanDefinition(resolverBeanName)) {
                throw new IllegalArgumentException("Undefined routing key resolver");
            }
            builder.addPropertyValue("path", path);
            builder.addPropertyReference("client", CloudConfigCommon.ZK_CLIENT_BEAN_NAME);
            builder.addPropertyReference("resolver", resolverBeanName);
            builder.addPropertyValue("resourceFactoryBeanClass", getFactoryBeanClassByType(resourceType, false));
            if(StringUtils.hasLength(fallbackRsPath)) {
                builder.addPropertyValue("fallbackResourcePath", fallbackRsPath);
            }
        } else {
            builder.addPropertyValue("configPath", path);
        }
        if(StringUtils.hasLength(validatorBeanName) && parserContext.getRegistry().containsBeanDefinition(validatorBeanName)) {
            builder.addPropertyReference("validator", validatorBeanName);
        }
        builder.addPropertyValue("autoReload", autoReload);
    }

    private Class<?> getFactoryBeanClassByType(String resourceType, boolean routingSupport) {
        if ("CipherEncoder".equals(resourceType)) {
            return routingSupport ? RoutingCipherEncoderFactoryBean.class : CipherEncoderFactoryBean.class;
        } else if ("CipherDecoder".equals(resourceType)) {
            return routingSupport ? RoutingCipherDecoderFactoryBean.class : CipherDecoderFactoryBean.class;
        }
        throw new UnsupportedOperationException("Unsupported resource type "+resourceType);
    }

    private String getBeanAliasByType(String resourceType, String path) {
        if ("CipherEncoder".equals(resourceType)) {
            return BeanIdGenerator.getCipherEncoderBeanId(path);
        } else if ("CipherDecoder".equals(resourceType)) {
            return BeanIdGenerator.getCipherDecoderBeanId(path);
        }
        throw new UnsupportedOperationException("Unsupported resource type "+resourceType);
    }
}