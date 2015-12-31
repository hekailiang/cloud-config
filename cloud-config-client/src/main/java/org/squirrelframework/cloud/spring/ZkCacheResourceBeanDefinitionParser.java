package org.squirrelframework.cloud.spring;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.squirrelframework.cloud.resource.cache.MemcachedFactoryBean;
import org.squirrelframework.cloud.utils.BeanIdGenerator;
import org.w3c.dom.Element;

/**
 * Created by kailianghe on 15/12/31.
 */
public class ZkCacheResourceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
    @Override
    protected Class getBeanClass(Element element) {
        return MemcachedFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        String path = element.getAttribute("path");
        builder.addPropertyValue("path", path);
        // set bean alias
        element.setAttribute(AbstractBeanDefinitionParser.NAME_ATTRIBUTE, BeanIdGenerator.getMemcachedBeanId(path));

        String cacheName = element.getAttribute("cache-name");
        builder.addPropertyValue("cacheName", cacheName);

        String resolverBeanName = element.getAttribute("resolver-ref");
        if(StringUtils.hasLength(resolverBeanName) && parserContext.getRegistry().containsBeanDefinition(resolverBeanName)) {
            builder.addPropertyReference("resolver", resolverBeanName);
        }

        String validatorBeanName = element.getAttribute("validator-ref");
        if(StringUtils.hasLength(validatorBeanName) && parserContext.getRegistry().containsBeanDefinition(validatorBeanName)) {
            builder.addPropertyReference("validator", validatorBeanName);
        }
    }
}
