package org.squirrelframework.cloud.resource.codec;

import com.google.common.base.Preconditions;
import org.squirrelframework.cloud.resource.AbstractRoutingResourceFactoryBean;
import org.squirrelframework.cloud.resource.RoutingSupport;
import org.squirrelframework.cloud.utils.BeanIdGenerator;

/**
 * Created by kailianghe on 15/12/20.
 */
public class RoutingCipherCodecFactoryBean extends AbstractRoutingResourceFactoryBean<Codec> {

    @Override
    protected String getResourceBeanIdFromPath(String resPath) {
        return BeanIdGenerator.getCipherCodecBeanId(resPath);
    }

    @Override
    public Class<?> getObjectType() {
        return Codec.class;
    }

    @Override
    protected Codec createInstance() throws Exception {
        createChildResourceBeanDefinition();
        return new RoutingCipherCode();
    }

    public class RoutingCipherCode implements Codec, RoutingSupport<Codec> {
        @Override
        public Codec get(String routingKey) {
            return getLocalResource(routingKey);
        }

        @Override
        public String decode(String value) throws Exception {
            String routingKey = resolver.get().orNull();
            Preconditions.checkNotNull(routingKey, "routing key is not defined");
            return get(routingKey).decode(value);
        }

        @Override
        public String encode(String value) throws Exception {
            String routingKey = resolver.get().orNull();
            Preconditions.checkNotNull(routingKey, "routing key is not defined");
            return get(routingKey).encode(value);
        }
    }
}
