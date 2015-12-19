package org.squirrelframework.cloud.spring;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.squirrelframework.cloud.resource.codec.CipherCodecFactoryBean;
import org.squirrelframework.cloud.utils.BeanIdGenerator;
import org.w3c.dom.Element;

import static org.squirrelframework.cloud.utils.CloudConfigCommon.getSafeBoolean;

/**
 * Created by kailianghe on 15/12/19.
 */
public class ZkResourceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class getBeanClass(Element element) {
        String resourceType = element.getAttribute("resource-type");
        if ("Cipher".equals(resourceType)) {
            return CipherCodecFactoryBean.class;
        }
        throw new UnsupportedOperationException("Unsupported resource type "+resourceType);
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        String path = element.getAttribute("path");
        String beanAlias = null;
        String resourceType = element.getAttribute("resource-type");
        if ("Cipher".equals(resourceType)) {
            beanAlias = BeanIdGenerator.getCipherCodecBeanId(path);

        }
        if(beanAlias!=null) {
            element.setAttribute(AbstractBeanDefinitionParser.NAME_ATTRIBUTE, beanAlias);
        }

        builder.addPropertyValue("configPath", path);
        Boolean autoReload = getSafeBoolean(element.getAttribute("auto-reload"));
        String validatorBeanName = element.getAttribute("validator-ref");
        if(StringUtils.hasLength(validatorBeanName) && parserContext.getRegistry().containsBeanDefinition(validatorBeanName)) {
            builder.addPropertyReference("validator", validatorBeanName);
        }
        builder.addPropertyValue("autoReload", autoReload);
    }
}