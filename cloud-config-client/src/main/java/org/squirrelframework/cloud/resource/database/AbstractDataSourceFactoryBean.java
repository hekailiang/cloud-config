package org.squirrelframework.cloud.resource.database;

import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.squirrelframework.cloud.resource.AbstractResourceFactoryBean;
import org.squirrelframework.cloud.resource.CloudResourceConfig;
import javax.sql.DataSource;

/**
 * Created by kailianghe on 9/11/15.
 */
public abstract class AbstractDataSourceFactoryBean<T extends CloudResourceConfig>
        extends AbstractResourceFactoryBean<DataSource, T> {

    @Override
    public Class<?> getObjectType() {
        return DataSource.class;
    }

    protected DelegatingDataSource createProxyDataSource(DataSource targetDataSource) {
        return new DelegatingDataSource(targetDataSource) {
            @Override
            public String toString() {
                if(getTargetDataSource()!=null) {
                    return getTargetDataSource().toString();
                } else {
                    return "_NULL_DATA_SOURCE_";
                }
            }
        };
    }
}
