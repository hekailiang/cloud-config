package org.squirrelframework.cloud.resource.security;

import com.google.common.base.Preconditions;
import org.squirrelframework.cloud.resource.AbstractRoutingResourceFactoryBean;
import org.squirrelframework.cloud.resource.RoutingSupport;
import org.squirrelframework.cloud.utils.BeanIdGenerator;

/**
 * Created by kailianghe on 16/1/6.
 */
public class RoutingSignatureCheckerFactoryBean extends AbstractRoutingResourceFactoryBean<SignatureChecker> {
    @Override
    protected String getResourceBeanIdFromPath(String resPath) {
        return BeanIdGenerator.getSignatureCheckerBeanId(resPath);
    }

    @Override
    public Class<?> getObjectType() {
        return SignatureChecker.class;
    }

    @Override
    protected SignatureChecker createInstance() throws Exception {
        createChildResourceBeanDefinition();
        return new RoutingSignatureChecker();
    }

    class RoutingSignatureChecker implements SignatureChecker, RoutingSupport<SignatureChecker> {
        @Override
        public boolean verify(String data, String charset, String sign) throws Exception {
            String routingKey = resolver.get().orNull();
            Preconditions.checkNotNull(routingKey, "routing key is not defined");
            return get(routingKey).verify(data, charset, sign);
        }

        @Override
        public boolean verify(String data, String sign) throws Exception {
            return verify(data, "UTF-8", sign);
        }

        @Override
        public SignatureChecker get(String routingKey) {
            return getLocalResource(routingKey);
        }
    }
}
