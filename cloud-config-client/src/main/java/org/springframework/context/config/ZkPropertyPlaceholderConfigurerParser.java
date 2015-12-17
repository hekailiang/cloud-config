package org.springframework.context.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.StringUtils;
import org.squirrelframework.cloud.conf.ZkPropertyPlaceholderConfigurer;
import org.squirrelframework.cloud.utils.CloudConfigCommon;
import org.w3c.dom.Element;

/**
 * Created by kailianghe on 11/9/15.
 */
public class ZkPropertyPlaceholderConfigurerParser extends PropertyPlaceholderBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return ZkPropertyPlaceholderConfigurer.class;
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        super.doParse(element, builder);

        builder.addPropertyValue("path", element.getAttribute("path"));

        Integer maxDepth = CloudConfigCommon.getSafeInteger(element.getAttribute("max-depth"));
        if(maxDepth!=null) {
            builder.addPropertyValue("maxDepth", maxDepth);
        }

        builder.addPropertyReference("client", CloudConfigCommon.ZK_CLIENT_BEAN_NAME);

        String jsonConverterRef = element.getAttribute("json-flatten-converter-ref");
        if(StringUtils.hasLength(jsonConverterRef)) {
            builder.addPropertyReference("jsonFlattenConverter", jsonConverterRef);
        }

    }
}
