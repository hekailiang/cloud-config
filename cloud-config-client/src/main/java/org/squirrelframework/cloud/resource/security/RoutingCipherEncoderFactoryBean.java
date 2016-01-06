package org.squirrelframework.cloud.resource.security;

import com.google.common.base.Preconditions;
import org.squirrelframework.cloud.resource.AbstractRoutingResourceFactoryBean;
import org.squirrelframework.cloud.resource.RoutingSupport;
import org.squirrelframework.cloud.utils.BeanIdGenerator;

/**
 * Created by kailianghe on 15/12/20.
 */
public class RoutingCipherEncoderFactoryBean extends AbstractRoutingResourceFactoryBean<Encoder> {

    @Override
    protected String getResourceBeanIdFromPath(String resPath) {
        return BeanIdGenerator.getCipherEncoderBeanId(resPath);
    }

    @Override
    public Class<?> getObjectType() {
        return Encoder.class;
    }

    @Override
    protected Encoder createInstance() throws Exception {
        createChildResourceBeanDefinition();
        return new RoutingCipherEncoder();
    }

    public class RoutingCipherEncoder implements Encoder, RoutingSupport<Encoder> {
        @Override
        public Encoder get(String routingKey) {
            return getLocalResource(routingKey);
        }

        @Override
        public String encode(String value, String charset) throws Exception {
            String routingKey = resolver.get().orNull();
            Preconditions.checkNotNull(routingKey, "routing key is not defined");
            return get(routingKey).encode(value, charset);
        }

        @Override
        public String encode(String value) throws Exception {
            return encode(value, "UTF-8");
        }
    }
}
