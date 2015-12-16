package org.squirrelframework.cloud.spring;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.squirrelframework.cloud.resource.sequence.SequenceGeneratorFactoryBean;
import org.squirrelframework.cloud.utils.CloudConfigCommon;
import org.w3c.dom.Element;

/**
 * Created by kailianghe on 15/12/15.
 */
public class ZkSequenceGeneratorBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return SequenceGeneratorFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        String path = element.getAttribute("path");
        builder.addPropertyValue("path", path);

        String resolverBeanName = element.getAttribute("resolver-ref");
        if(parserContext.getRegistry().containsBeanDefinition(resolverBeanName)) {
            builder.addPropertyReference("resolver", resolverBeanName);
        }
        builder.addPropertyReference("client", CloudConfigCommon.ZK_CLIENT_BEAN_NAME);

        String formatterBeanName = element.getAttribute("formatter-ref");
        if(parserContext.getRegistry().containsBeanDefinition(formatterBeanName)) {
            builder.addPropertyReference("sequenceFormatter", formatterBeanName);
        } else {
            throw new IllegalArgumentException("Undefined formatter bean: "+formatterBeanName);
        }
    }
}
