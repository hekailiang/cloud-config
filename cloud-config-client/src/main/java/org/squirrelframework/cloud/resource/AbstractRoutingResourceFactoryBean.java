package org.squirrelframework.cloud.resource;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.validation.Validator;
import org.squirrelframework.cloud.routing.NestedRoutingKeyResolver;
import org.squirrelframework.cloud.routing.RoutingKeyResolver;
import org.squirrelframework.cloud.utils.CloudConfigCommon;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

/**
 * Created by kailianghe on 9/14/15.
 */
public abstract class AbstractRoutingResourceFactoryBean<T> extends AbstractFactoryBean<T> implements ApplicationContextAware, BeanFactoryAware {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRoutingResourceFactoryBean.class);

    protected String path;

    protected CuratorFramework client;

    protected RoutingKeyResolver resolver;

    protected ApplicationContext applicationContext;

    protected PathChildrenCache childNodeCache;

    protected T fallbackResource;

    protected String fallbackResourcePath;

    protected Validator validator;

    private DefaultListableBeanFactory beanFactory;

    private boolean autoReload = false;

    protected Class<?> resourceFactoryBeanClass;

    protected CacheLoader<String, T> resourceCacheLoader = new CacheLoader<String, T>() {
        @Override
        public T load(String routingKey) throws Exception {
            String expectedBeanId = getResourceBeanIdFromPath(path+"/"+ routingKey);
            T resource = (T)applicationContext.getBean(expectedBeanId);
            logger.info("DataSource for '{}' is resolved as '{}'.", routingKey, resource.toString());
            return resource;
        }
    };

    protected LoadingCache<String, T> localResourceStore = CacheBuilder.newBuilder().build(resourceCacheLoader);

    protected T getLocalResource(String routingKey) {
        try {
            return localResourceStore.getUnchecked(routingKey);
        } catch (UncheckedExecutionException e) {
            Throwable cause = e.getCause();
            if(cause instanceof NoSuchBeanDefinitionException && fallbackResource!=null) {
                logger.warn("Cannot find proper data source for '{}'. Use fallback data source instead.", routingKey);
                return fallbackResource;
            }
            throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + routingKey + "]", cause);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(isAutoReload()) {
            childNodeCache = new PathChildrenCache(client, path, true);
            childNodeCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
            childNodeCache.getListenable().addListener(new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    String nodeName = event.getData()!=null ?
                            ZKPaths.getNodeFromPath(event.getData().getPath()) : "";
                    String resPath = path+"/"+nodeName;
                    switch (event.getType()) {
                        case CHILD_ADDED: {
                            logger.info("ChildNode added: {}", nodeName);
                            buildResourceBeanDefinition(resPath, getResourceBeanIdFromPath(resPath));
                            break;
                        }

                        case CHILD_UPDATED: {
                            logger.info("ChildNode changed: {}", nodeName);
                            break;
                        }

                        case CHILD_REMOVED: {
                            logger.info("ChildNode removed: {}", nodeName);
                            removeResourceBeanDefinition(resPath, getResourceBeanIdFromPath(resPath));
                            break;
                        }
                        default:
                            break;
                    }
                }
            });
        }
        if(fallbackResourcePath!=null && fallbackResource==null) {
            String dsBeanName = getResourceBeanIdFromPath(fallbackResourcePath);
            if( !applicationContext.containsBeanDefinition(dsBeanName) ) {
                buildResourceBeanDefinition(fallbackResourcePath, dsBeanName);
            }
            fallbackResource = (T) applicationContext.getBean(dsBeanName);
        }
        super.afterPropertiesSet();
    }

    protected void createChildResourceBeanDefinition() throws Exception {
        List<String> children = client.getChildren().forPath(path);
        for(String child : children) {
            String resPath = path + "/" + child;
            buildResourceBeanDefinition(resPath, getResourceBeanIdFromPath(resPath));
        }
    }

    protected void buildResourceBeanDefinition(String dsPath, String dsBeanId) throws Exception {
        if(getBeanFactory().containsBeanDefinition(dsBeanId)) {
            return;
        }

        // build datasource bean based on config bean
        final BeanDefinitionBuilder dsBuilder;
        if( isNestedRoutingNeeded(dsPath) ) {
            dsBuilder = BeanDefinitionBuilder.rootBeanDefinition(getClass());
            dsBuilder.addPropertyValue("path", dsPath);
            dsBuilder.addPropertyValue("client", this.client);
            dsBuilder.addPropertyValue("fallbackResource", this.fallbackResource);
            dsBuilder.addPropertyValue("fallbackResourcePath", this.fallbackResourcePath);
            dsBuilder.addPropertyValue("resourceFactoryBeanClass", this.resourceFactoryBeanClass);
            dsBuilder.addPropertyValue("resolver", ((NestedRoutingKeyResolver)this.resolver).next());
        } else {
            dsBuilder = BeanDefinitionBuilder.rootBeanDefinition(resourceFactoryBeanClass);
            dsBuilder.addPropertyValue("configPath", dsPath);
        }
        dsBuilder.addPropertyValue("autoReload", isAutoReload());
        dsBuilder.addPropertyValue("validator", validator);
        dsBuilder.setLazyInit(true);
        getBeanFactory().registerBeanDefinition(dsBeanId, dsBuilder.getBeanDefinition());
        logger.info("Bean definition of resource '{}' is created as '{}'.", dsPath, dsBeanId);
    }

    abstract protected String getResourceBeanIdFromPath(String resPath);

    protected void removeResourceBeanDefinition(String resPath, String beanId) throws Exception {
        if(getBeanFactory().containsBeanDefinition(beanId)) {
            getBeanFactory().removeBeanDefinition(beanId);
        }
        // TODO-hhe: clean local cached data sources as we cannot get tenant id here
        if( getObject()!=null ) {
            localResourceStore.invalidateAll();
        }
        logger.info("Bean definition of resource '{}' is removed as '{}'.", resPath, beanId);
    }

    protected boolean isNestedRoutingNeeded(String dsPath) throws Exception {
        return resolver instanceof NestedRoutingKeyResolver &&
                ((NestedRoutingKeyResolver)resolver).hasNext() &&
                client.getChildren().forPath(dsPath).size() > 0;
    }

    @Override
    protected void destroyInstance(T instance) throws Exception {
        childNodeCache.close();
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
    public void setResolver(RoutingKeyResolver resolver) {
        this.resolver = resolver;
    }

    public void setFallbackResource(T fallbackResource) {
        this.fallbackResource = fallbackResource;
    }

    public void setFallbackResourcePath(String fallbackResourcePath) {
        this.fallbackResourcePath = fallbackResourcePath;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    protected DefaultListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    public void setAutoReload(boolean autoReload) {
        this.autoReload = autoReload;
    }

    public boolean isAutoReload() {
        return autoReload;
    }

    @Required
    public void setResourceFactoryBeanClass(Class<?> resourceFactoryBeanClass) {
        this.resourceFactoryBeanClass = resourceFactoryBeanClass;
    }
}
