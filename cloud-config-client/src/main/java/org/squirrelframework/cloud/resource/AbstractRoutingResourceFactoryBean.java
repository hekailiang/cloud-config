package org.squirrelframework.cloud.resource;

import org.springframework.validation.Validator;
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

/**
 * Created by kailianghe on 9/14/15.
 */
public abstract class AbstractRoutingResourceFactoryBean<T> extends AbstractFactoryBean<T> implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRoutingResourceFactoryBean.class);

    protected String path;

    protected CuratorFramework client;

    protected RoutingKeyResolver resolver;

    protected ApplicationContext applicationContext;

    protected PathChildrenCache childNodeCache;

    protected T fallbackResource;

    protected String fallbackResourcePath;

    protected Validator validator;

    @Override
    public void afterPropertiesSet() throws Exception {
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
        if(fallbackResourcePath!=null && fallbackResource==null) {
            String dsBeanName = getResourceBeanIdFromPath(fallbackResourcePath.replace('/', '_'));
            if( !applicationContext.containsBeanDefinition(dsBeanName) ) {
                buildResourceBeanDefinition(fallbackResourcePath, dsBeanName);
            }
            fallbackResource = (T) applicationContext.getBean(dsBeanName);
        }
        super.afterPropertiesSet();
    }

    abstract protected String getResourceBeanIdFromPath(String resPath);

    abstract protected void buildResourceBeanDefinition(String resPath, String beanId) throws Exception;

    abstract protected void removeResourceBeanDefinition(String resPath, String beanId) throws Exception;

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
}
