package org.squirrelframework.cloud.resource.database;

import com.google.common.base.Preconditions;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.squirrelframework.cloud.resource.CloudResourceConfig;
import org.squirrelframework.cloud.resource.ReloadCallback;

import javax.sql.DataSource;

/**
 * Created by kailianghe on 11/10/15.
 */
public class C3P0DataSourceFactoryBean extends AbstractDataSourceFactoryBean<C3P0DataSourceConfig> {

    @Override
    protected DataSource createInstance() throws Exception {
        Preconditions.checkNotNull(config, "config cannot be empty");
        final ComboPooledDataSource targetDataSource = createNewDataSource();
        final DelegatingDataSource proxyDataSource = createProxyDataSource(targetDataSource);
        config.setReloadCallback(new ReloadCallback() {
            @Override
            public void reload() throws Exception {
                // switch data source
                ComboPooledDataSource oldTargetDataSource = (ComboPooledDataSource)proxyDataSource.getTargetDataSource();
                ComboPooledDataSource newTargetDataSource = createNewDataSource();
                newTargetDataSource.getConnection().close(); // initialize a connection (+ throw it away) to force the datasource to initialize the pool

                proxyDataSource.setTargetDataSource(newTargetDataSource);
                oldTargetDataSource.close();
            }
        });
        return proxyDataSource;
    }

    private ComboPooledDataSource createNewDataSource() throws Exception {
        ComboPooledDataSource c3p0DataSource = new ComboPooledDataSource();

        c3p0DataSource.setDriverClass(config.getDriverClassName()); //loads the jdbc driver
        c3p0DataSource.setJdbcUrl(config.getJdbcUrl());
        c3p0DataSource.setUser(config.getUserName());
        c3p0DataSource.setPassword(config.getPassword());

        // the settings below are optional -- c3p0 can work with defaults
        c3p0DataSource.setMinPoolSize(config.getMinPoolSize());
        c3p0DataSource.setMaxPoolSize(config.getMaxPoolSize());
        c3p0DataSource.setAcquireIncrement(config.getAcquireIncrement());
        c3p0DataSource.setMaxStatements(config.getMaxStatements());
        c3p0DataSource.setIdleConnectionTestPeriod(config.getIdleTestPeriod());
        c3p0DataSource.setMaxIdleTime(config.getMaxIdleTime());

        return c3p0DataSource;
    }

    @Override
    protected void destroyInstance(DataSource instance) throws Exception {
        ((ComboPooledDataSource)((DelegatingDataSource)instance).getTargetDataSource()).close();
    }

    @Override
    protected Class<? extends CloudResourceConfig> getConfigType() {
        return C3P0DataSourceConfig.class;
    }
}
