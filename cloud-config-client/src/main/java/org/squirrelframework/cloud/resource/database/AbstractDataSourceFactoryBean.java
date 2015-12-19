package org.squirrelframework.cloud.resource.database;

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

    @Override
    protected Class<? extends CloudResourceConfig> getConfigType() {
        return JdbcDataSourceConfig.class;
    }
}
