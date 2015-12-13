package org.squirrelframework.cloud.resource.database;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.squirrelframework.cloud.resource.AbstractRoutingResourceFactoryBean;
import org.squirrelframework.cloud.resource.TenantSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.squirrelframework.cloud.routing.NestedRoutingKeyResolver;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

/**
 * Created by kailianghe on 9/7/15.
 */
public class RoutingDataSourceFactoryBean extends AbstractRoutingResourceFactoryBean<DataSource> {

    private static final Logger myLogger = LoggerFactory.getLogger(RoutingDataSourceFactoryBean.class);

    private Class<?> dataSourceFactoryBeanClass;

    @Override
    public Class<?> getObjectType() {
        return DataSource.class;
    }

    @Override
    protected DataSource createInstance() throws Exception {
        List<String> children = client.getChildren().forPath(path);
        for(String child : children) {
            String resPath = path + "/" + child;
            buildResourceBeanDefinition(resPath, getResourceBeanIdFromPath(resPath));
        }
        return new RoutingDataSource();
    }

    @Override
    protected String getResourceBeanIdFromPath(String resPath) {
        return "_"+resPath.replace('/','_')+"DS";
    }

    @Override
    protected void buildResourceBeanDefinition(String dsPath, String dsBeanId) throws Exception {
        if(getBeanFactory().containsBeanDefinition(dsBeanId)) {
            return;
        }

        // build datasource bean based on config bean
        final BeanDefinitionBuilder dsBuilder;
        if( isNestedRoutingNeeded(dsPath) ) {
            dsBuilder = BeanDefinitionBuilder.rootBeanDefinition(RoutingDataSourceFactoryBean.class);
            dsBuilder.addPropertyValue("path", dsPath);
            dsBuilder.addPropertyValue("client", this.client);
            dsBuilder.addPropertyValue("fallbackResource", this.fallbackResource);
            dsBuilder.addPropertyValue("fallbackResourcePath", this.fallbackResourcePath);
            dsBuilder.addPropertyValue("dataSourceFactoryBeanClass", this.dataSourceFactoryBeanClass);
            dsBuilder.addPropertyValue("resolver", ((NestedRoutingKeyResolver)this.resolver).next());
        } else {
            dsBuilder = BeanDefinitionBuilder.rootBeanDefinition(dataSourceFactoryBeanClass);
            dsBuilder.addPropertyValue("configPath", dsPath);
        }
        dsBuilder.addPropertyValue("autoReload", isAutoReload());
        dsBuilder.addPropertyValue("validator", validator);
        dsBuilder.setLazyInit(true);
        getBeanFactory().registerBeanDefinition(dsBeanId, dsBuilder.getBeanDefinition());
        myLogger.info("Bean definition of resource '{}' is created as '{}'.", dsPath, dsBeanId);
    }

    private boolean isNestedRoutingNeeded(String dsPath) throws Exception {
        return resolver instanceof NestedRoutingKeyResolver &&
                ((NestedRoutingKeyResolver)resolver).hasNext() &&
                client.getChildren().forPath(dsPath).size() > 0;
    }

    @Override
    protected void removeResourceBeanDefinition(String resPath, String beanId) throws Exception {
        if(getBeanFactory().containsBeanDefinition(beanId)) {
            getBeanFactory().removeBeanDefinition(beanId);
        }
        String confBeanId = AbstractDataSourceFactoryBean.getResourceConfigBeanIdFromPath(resPath);
        if(getBeanFactory().containsBeanDefinition(confBeanId)) {
            getBeanFactory().removeBeanDefinition(confBeanId);
        }

        // TODO-hhe: clean local cached data sources as we cannot get tenant id here
        if( getObject()!=null ) {
            ((RoutingDataSource)getObject()).localDataSourceStore.invalidateAll();
        }
        myLogger.info("Bean definition of resource '{}' is removed as '{}'.", resPath, beanId);
    }

    @Required
    public void setDataSourceFactoryBeanClass(Class<?> dataSourceFactoryBeanClass) {
        this.dataSourceFactoryBeanClass = dataSourceFactoryBeanClass;
    }

    public void setFallbackDataSourcePath(String fallbackDataSourcePath) {
        this.fallbackResourcePath = fallbackDataSourcePath;
    }

    public void setFallbackDataSource(DataSource fallbackDataSource) {
        this.fallbackResource = fallbackDataSource;
    }

    private CacheLoader<String, DataSource> dataSourceCacheLoader = new CacheLoader<String, DataSource>() {
        @Override
        public DataSource load(String routingKey) throws Exception {
            String expectedBeanId = getResourceBeanIdFromPath(path+"/"+ routingKey);
            DataSource dataSource = applicationContext.getBean(expectedBeanId, DataSource.class);
            myLogger.info("DataSource for '{}' is resolved as '{}'.", routingKey, dataSource.toString());
            return dataSource;
        }
    };

    public class RoutingDataSource extends AbstractRoutingDataSource implements TenantSupport<DataSource> {
        private LoadingCache<String, DataSource> localDataSourceStore = CacheBuilder.newBuilder().build(dataSourceCacheLoader);

        public RoutingDataSource() {
            setTargetDataSources(Collections.emptyMap());
        }

        @Override
        protected Object determineCurrentLookupKey() {
            Object lookupKey = resolver.get().orNull();
            if( lookupKey!=null ) {
                myLogger.debug("Routing data source lookup key is '{}'", lookupKey.toString());
            } else {
                myLogger.warn("Routing data source lookup key cannot be found in current context!");
                lookupKey = "__absent_tenant__";
            }
            return lookupKey;
        }

        @Override
        protected DataSource determineTargetDataSource() {
            String lookupKey = determineCurrentLookupKey().toString();
            return get(lookupKey);
        }

        @Override
        public DataSource get(String routingKey) {
            try {
                return localDataSourceStore.getUnchecked(routingKey);
            } catch (UncheckedExecutionException e) {
                Throwable cause = e.getCause();
                if(cause instanceof NoSuchBeanDefinitionException && fallbackResource!=null) {
                    myLogger.warn("Cannot find proper data source for '{}'. Use fallback data source instead.", routingKey);
                    return fallbackResource;
                }
                throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + routingKey + "]", cause);
            }
        }
    }
}
