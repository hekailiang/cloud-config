package org.squirrelframework.cloud.resource.codec;

import com.google.common.base.Preconditions;
import org.squirrelframework.cloud.resource.AbstractRoutingResourceFactoryBean;
import org.squirrelframework.cloud.resource.RoutingSupport;
import org.squirrelframework.cloud.utils.BeanIdGenerator;

/**
 * Created by kailianghe on 15/12/20.
 */
public class RoutingCipherDecoderFactoryBean extends AbstractRoutingResourceFactoryBean<Decoder> {

    @Override
    protected String getResourceBeanIdFromPath(String resPath) {
        return BeanIdGenerator.getCipherDecoderBeanId(resPath);
    }

    @Override
    public Class<?> getObjectType() {
        return Decoder.class;
    }

    @Override
    protected Decoder createInstance() throws Exception {
        createChildResourceBeanDefinition();
        return new RoutingCipherDecoder();
    }

    public class RoutingCipherDecoder implements Decoder, RoutingSupport<Decoder> {
        @Override
        public Decoder get(String routingKey) {
            return getLocalResource(routingKey);
        }

        @Override
        public String decode(String value) throws Exception {
            String routingKey = resolver.get().orNull();
            Preconditions.checkNotNull(routingKey, "routing key is not defined");
            return get(routingKey).decode(value);
        }
    }
}
