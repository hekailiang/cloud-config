package org.squirrelframework.cloud.resource.cache;

import com.google.code.ssm.Cache;
import com.google.code.ssm.CacheFactory;
import com.google.code.ssm.config.DefaultAddressProvider;
import com.google.code.ssm.providers.xmemcached.RoutingMemcacheClientFactoryImpl;
import com.google.code.ssm.providers.xmemcached.XMemcachedConfiguration;
import com.google.common.base.Function;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;
import org.squirrelframework.cloud.resource.AbstractResourceFactoryBean;
import org.squirrelframework.cloud.resource.CloudResourceConfig;
import org.squirrelframework.cloud.routing.RoutingKeyResolver;

import java.util.Arrays;

import static com.google.common.collect.Iterables.*;

/**
 * Created by kailianghe on 15/12/27.
 */
public class MemcachedFactoryBean extends AbstractResourceFactoryBean<Cache, MemcachedResourceConfig> implements DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(CacheFactory.class);

    private RoutingKeyResolver resolver;

    private CacheFactory cacheFactory;

    private String cacheName;

    @Override
    protected Class<? extends CloudResourceConfig> getConfigType() {
        return MemcachedResourceConfig.class;
    }

    @Override
    public Class<?> getObjectType() {
        return Cache.class;
    }

    @Override
    protected Cache createInstance() throws Exception {
        if(cacheFactory == null) {
            cacheFactory = new CacheFactory();
            cacheFactory.setCacheName(cacheName);
            cacheFactory.setCacheClientFactory(new RoutingMemcacheClientFactoryImpl(resolver));
            cacheFactory.setAddressProvider(new DefaultAddressProvider(config.getAddress()));
            cacheFactory.setDefaultSerializationType(config.getSerializationType());

            XMemcachedConfiguration cacheConfiguration = new XMemcachedConfiguration();
            cacheConfiguration.setConsistentHashing(config.isConsistentHashing());
            cacheConfiguration.setKeyPrefixSeparator(config.getKeyPrefixSeparator());
            cacheConfiguration.setOperationTimeout(config.getOperationTimeout());
            cacheConfiguration.setUseBinaryProtocol(config.isUseBinaryProtocol());
            cacheConfiguration.setUseNameAsKeyPrefix(config.isUseNameAsKeyPrefix());
            try {
                String weights = config.getWeights();
                if(weights!=null) {
                    String[] weightsStr = StringUtils.tokenizeToStringArray(weights, ",:");
                    Integer[] weightVal = toArray(transform( Arrays.asList(weightsStr), new Function<String, Integer>() {
                        @Override
                        public Integer apply(String input) {
                            return Integer.valueOf(input.trim());
                        }
                    }), Integer.class);
                    cacheConfiguration.setWeights(ArrayUtils.toPrimitive(weightVal));
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid weights settings '"+config.getWeights()+"'", e.getCause());
            }
            cacheFactory.setConfiguration(cacheConfiguration);
            cacheFactory.afterPropertiesSet();
        }
        return cacheFactory.getObject();
    }

    @Override
    protected void destroyInstance(Cache instance) throws Exception {
        if(instance!=null) {
            logger.info("Shutdowning cache {}", cacheFactory.getCacheName());
            instance.shutdown();
            cacheFactory = null;
        }
    }

    public void setResolver(RoutingKeyResolver resolver) {
        this.resolver = resolver;
    }

    @Required
    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public void setPath(String path) {
        this.setConfigPath(path);
    }
}
