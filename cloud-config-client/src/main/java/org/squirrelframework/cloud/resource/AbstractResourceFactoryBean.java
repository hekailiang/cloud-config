package org.squirrelframework.cloud.resource;

import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.util.StringValueResolver;
import org.squirrelframework.cloud.utils.CloudConfigCommon;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.squirrelframework.cloud.utils.InetAddressHelper;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

/**
 * Created by kailianghe on 9/10/15.
 */
public abstract class AbstractResourceFactoryBean<T extends CloudResourceConfig> extends AbstractFactoryBean<T>
        implements EmbeddedValueResolverAware {

    protected Logger logger = LoggerFactory.getLogger(AbstractResourceFactoryBean.class);

    protected static final ObjectMapper mapper = new ObjectMapper()
            /*.setPropertyNamingStrategy(new com.fasterxml.jackson.databind.PropertyNamingStrategy.PropertyNamingStrategyBase() {
                @Override
                public String translate(String input) {
                    if (input == null) return input; // garbage in, garbage out
                    int length = input.length();
                    StringBuilder result = new StringBuilder(length * 2);
                    int resultLength = 0;
                    boolean wasPrevTranslated = false;
                    for (int i = 0; i < length; i++) {
                        char c = input.charAt(i);
                        if (i > 0 || c != '_') { // skip first starting underscore
                            if (Character.isUpperCase(c)) {
                                if (!wasPrevTranslated && resultLength > 0 && result.charAt(resultLength - 1) != '_') {
                                    result.append('.');
                                    resultLength++;
                                }
                                c = Character.toLowerCase(c);
                                wasPrevTranslated = true;
                            } else {
                                wasPrevTranslated = false;
                            }
                            result.append(c);
                            resultLength++;
                        }
                    }
                    return resultLength > 0 ? result.toString() : input;
                }
            })*/
            ;

    protected String path;

    protected CuratorFramework client;

    protected Class<? extends CloudResourceConfig> resourceType;

    protected Validator validator;

    protected String[] configProfiles;

    private PathChildrenCache childNodeCache;

    private StringValueResolver stringValueResolver;

    @Override
    public void afterPropertiesSet() throws Exception {
        configProfiles = CloudConfigCommon.getProfiles();

        // attach listeners
        childNodeCache = new PathChildrenCache(client, path, true);
        childNodeCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        childNodeCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_ADDED: {
                        handleChildAdded(client, event);
                        break;
                    }

                    case CHILD_UPDATED: {
                        handleChildUpdated(client, event);
                        break;
                    }

                    case CHILD_REMOVED: {
                        handleChildRemoved(client, event);
                        break;
                    }
                    default:
                        break;
                }
            }
        });

        // create instances
        super.afterPropertiesSet();

        // validate created instance
        T config = getObject();
        if(validator!=null && config!=null) {
            Set<ConstraintViolation<T>> result = validator.validate(config);
            if(result.size()>0) {
                StringBuilder errMsgBuilder = new StringBuilder("Bean '"+config.toString()+"' validation failed. Details:\n");
                for(ConstraintViolation<T> item: result) {
                    errMsgBuilder.append("property: ").append(item.getPropertyPath()).append("\t==>\t").append(item.getMessage()).append('\n');
                }
                throw new IllegalArgumentException(errMsgBuilder.toString());
            }
        }
    }

    protected void handleChildAdded(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
        logger.info("ChildNode added: " + safeGetNodeNameFromEvent(event));
    }

    protected void handleChildUpdated(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
        logger.info("ChildNode changed: " + safeGetNodeNameFromEvent(event));
    }

    protected void handleChildRemoved(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
        logger.info("ChildNode removed: " + safeGetNodeNameFromEvent(event));
    }

    protected String safeGetNodeNameFromEvent(PathChildrenCacheEvent event) {
        return event.getData() != null ?
                ZKPaths.getNodeFromPath(event.getData().getPath()) : "";
    }

    @Override
    protected void destroyInstance(T instance) throws Exception {
        if(childNodeCache!=null) {
            childNodeCache.close();
        }
    }

    protected <M extends CloudResourceConfig> M createConfig(byte[] value) throws Exception {
        return createConfig(value, null);
    }

    protected <M extends CloudResourceConfig> M createConfig(byte[] value, M baseConfig) throws Exception {
        M config;
        if(value!=null && value.length>0) {
            byte[] bValue = stringValueResolver==null ?  value :
                    stringValueResolver.resolveStringValue(CloudConfigCommon.bytes2String(value)).getBytes();
            if(baseConfig!=null) {
                Object overrideConfig = resourceType.newInstance();
                BeanUtils.copyProperties(baseConfig, overrideConfig);
                config = mapper.readerForUpdating(overrideConfig).readValue(bValue);
            } else {
                config = (M) mapper.readValue(bValue, resourceType);
            }
        } else {
            config = baseConfig;
        }
        return config;
    }

    @Required
    public void setPath(String path) {
        this.path = path;
    }

    @Required
    public void setClient(CuratorFramework client) {
        this.client = client.usingNamespace(CloudConfigCommon.CONFIG_ROOT);
    }

    @Required
    public void setResourceType(Class<? extends CloudResourceConfig> resourceType) {
        this.resourceType = resourceType;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    public void setEmbeddedValueResolver(StringValueResolver stringValueResolver) {
        this.stringValueResolver = stringValueResolver;
    }

    protected boolean canApplyForLocalMachine(String nodeName) {
        return nodeName.charAt(0)=='&' &&
                InetAddressHelper.isLocalMachineIpAddressInRange(nodeName.substring(1));
    }
}
