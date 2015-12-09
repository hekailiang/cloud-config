package org.squirrelframework.cloud.resource.database;

import org.squirrelframework.cloud.resource.AbstractRoutingResourceFactoryBean;
import org.squirrelframework.cloud.resource.TenantSupport;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.squirrelframework.cloud.routing.NestedRoutingKeyResolver;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

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
            ((RoutingDataSource)getObject()).localDataSources.clear();
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

    public class RoutingDataSource extends AbstractRoutingDataSource implements TenantSupport<DataSource> {

        private ConcurrentMap<String, DataSource> localDataSources = Maps.newConcurrentMap();

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
            DataSource dataSource = localDataSources.get(routingKey);
            if(dataSource==null) {
                try {
                    String expectedBeanId = getResourceBeanIdFromPath(path+"/"+ routingKey);
                    dataSource = applicationContext.getBean(expectedBeanId, DataSource.class);
                    localDataSources.put(routingKey, dataSource);
                    myLogger.info("DataSource for tenant '{}' is resolved as '{}'.", routingKey, dataSource.toString());
                } catch (NoSuchBeanDefinitionException e) {
                    // find fallback datasource - "unknown"
                    if(fallbackResource!=null) {
                        dataSource = fallbackResource;
                        myLogger.warn("Cannot find proper data source for tenant '{}'. Use fallback data source instead.", routingKey);
                    } else {
                        throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + routingKey + "]", e);
                    }
                }
            }
            return dataSource;
        }
    }
}
