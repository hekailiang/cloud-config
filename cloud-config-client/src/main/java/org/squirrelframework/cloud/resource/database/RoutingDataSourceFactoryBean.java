package org.squirrelframework.cloud.resource.database;

import org.squirrelframework.cloud.resource.AbstractRoutingResourceFactoryBean;
import org.squirrelframework.cloud.resource.RoutingSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.squirrelframework.cloud.utils.BeanIdGenerator;

import javax.sql.DataSource;
import java.util.Collections;

/**
 * Created by kailianghe on 9/7/15.
 */
public class RoutingDataSourceFactoryBean extends AbstractRoutingResourceFactoryBean<DataSource> {

    private static final Logger myLogger = LoggerFactory.getLogger(RoutingDataSourceFactoryBean.class);

    @Override
    public Class<?> getObjectType() {
        return DataSource.class;
    }

    @Override
    protected DataSource createInstance() throws Exception {
        createChildResourceBeanDefinition();
        return new RoutingDataSource();
    }

    @Override
    protected String getResourceBeanIdFromPath(String resPath) {
        return BeanIdGenerator.getDataSourceBeanId(resPath);
    }

    @Override
    protected void removeResourceBeanDefinition(String resPath, String beanId) throws Exception {
        super.removeResourceBeanDefinition(resPath, beanId);
        String confBeanId = BeanIdGenerator.getResourceConfigBeanId(resPath, JdbcDataSourceConfig.class);
        if(getBeanFactory().containsBeanDefinition(confBeanId)) {
            getBeanFactory().removeBeanDefinition(confBeanId);
        }
    }

    public void setFallbackDataSourcePath(String fallbackDataSourcePath) {
        this.fallbackResourcePath = fallbackDataSourcePath;
    }

    public void setFallbackDataSource(DataSource fallbackDataSource) {
        this.fallbackResource = fallbackDataSource;
    }

    public class RoutingDataSource extends AbstractRoutingDataSource implements RoutingSupport<DataSource> {
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
            return getLocalResource(routingKey);
        }
    }
}
