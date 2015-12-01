package org.squirrelframework.cloud.spring;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.squirrelframework.cloud.conf.ZkClientFactoryBean;
import org.squirrelframework.cloud.utils.CloudConfigCommon;
import org.w3c.dom.Element;

import static org.squirrelframework.cloud.utils.CloudConfigCommon.getSafeBoolean;
import static org.squirrelframework.cloud.utils.CloudConfigCommon.getSafeInteger;

/**
 * Created by kailianghe on 11/9/15.
 */
public class ZkClientBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class getBeanClass(Element element) {
        return ZkClientFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        String connectionString = element.getAttribute("connection-string");
        if(StringUtils.hasLength(connectionString)) {
            builder.addPropertyValue("connectionString", connectionString);
        }

        Integer maxRetries = getSafeInteger(element.getAttribute("max-retries"));
        if(maxRetries!=null) {
            builder.addPropertyValue("maxRetries", maxRetries);
        }

        Integer baseSleepTime = getSafeInteger(element.getAttribute("base-sleep-time"));
        if(baseSleepTime!=null) {
            builder.addPropertyValue("baseSleepTime", baseSleepTime);
        }

        Boolean readOnly = getSafeBoolean(element.getAttribute("read-only"));
        if(readOnly!=null) {
            builder.addPropertyValue("canReadOnly", readOnly);
        }
    }

    @Override
    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
        return CloudConfigCommon.ZK_CLIENT_BEAN_NAME;
    }
}
