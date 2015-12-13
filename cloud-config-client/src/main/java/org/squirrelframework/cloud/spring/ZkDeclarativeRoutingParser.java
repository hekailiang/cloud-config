package org.squirrelframework.cloud.spring;

import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.squirrelframework.cloud.routing.DeclarativeRoutingKeyAspect;
import org.w3c.dom.Element;

/**
 * Created by kailianghe on 15/12/13.
 */
public class ZkDeclarativeRoutingParser implements BeanDefinitionParser {

    public static final String ROUTING_KEY_ASPECT_BEAN_NAME = "__declarative_routing_key_aspect";

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        AopNamespaceUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(parserContext, element);
        if (!parserContext.getRegistry().containsBeanDefinition(ROUTING_KEY_ASPECT_BEAN_NAME)) {
            RootBeanDefinition def = new RootBeanDefinition(DeclarativeRoutingKeyAspect.class);
            parserContext.registerBeanComponent(new BeanComponentDefinition(def, ROUTING_KEY_ASPECT_BEAN_NAME));
        }
        return null;
    }
}
