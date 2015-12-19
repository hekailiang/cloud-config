package org.squirrelframework.cloud.resource.database;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.base.Preconditions;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.squirrelframework.cloud.resource.CloudResourceConfig;
import org.squirrelframework.cloud.resource.ReloadCallback;

import javax.sql.DataSource;

/**
 * Created by kailianghe on 11/10/15.
 */
public class DruidDataSourceFactoryBean extends AbstractDataSourceFactoryBean<DruidDataSourceConfig> {

    @Override
    protected DataSource createInstance() throws Exception {
        Preconditions.checkNotNull(config, "config cannot be empty");
        final DruidDataSource targetDataSource = createNewDataSource();
        final DelegatingDataSource proxyDataSource = createProxyDataSource(targetDataSource);
        config.setReloadCallback(new ReloadCallback() {
            @Override
            public void reload() throws Exception {
                // switch data source
                DruidDataSource oldTargetDataSource = (DruidDataSource)proxyDataSource.getTargetDataSource();
                DruidDataSource newTargetDataSource = createNewDataSource();
                newTargetDataSource.getConnection().close(); // initialize a connection (+ throw it away) to force the datasource to initialize the pool

                proxyDataSource.setTargetDataSource(newTargetDataSource);
                oldTargetDataSource.close();
            }
        });
        return proxyDataSource;
    }

    private DruidDataSource createNewDataSource() throws Exception {
        DruidDataSource druidDataSource = new DruidDataSource();

        druidDataSource.setDriverClassName(config.getDriverClassName()); //loads the jdbc driver
        druidDataSource.setUrl(config.getJdbcUrl());
        druidDataSource.setUsername(config.getUserName());
        druidDataSource.setPassword(config.getPassword());

        druidDataSource.setInitialSize(config.getInitialSize());
        druidDataSource.setMaxActive(config.getMaxActive());
        druidDataSource.setMaxIdle(config.getMaxIdle());
        druidDataSource.setMaxWait(config.getMaxWait());
        druidDataSource.setFilters(config.getFilters());
        druidDataSource.setTimeBetweenEvictionRunsMillis(config.getTimeBetweenEvictionRunsMillis());
        druidDataSource.setMinEvictableIdleTimeMillis(config.getMinEvictableIdleTimeMillis());
        druidDataSource.setValidationQuery(config.getValidationQuery());
        druidDataSource.setTestWhileIdle(config.isTestWhileIdle());
        druidDataSource.setTestOnBorrow(config.isTestOnBorrow());
        druidDataSource.setTestOnReturn(config.isTestOnReturn());
        druidDataSource.setPoolPreparedStatements(config.isPoolPreparedStatements());
        druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(config.getMaxPoolPreparedStatementPerConnectionSize());
        return druidDataSource;
    }

    @Override
    protected void destroyInstance(DataSource instance) throws Exception {
        ((DruidDataSource)((DelegatingDataSource)instance).getTargetDataSource()).close();
    }

    @Override
    protected Class<? extends CloudResourceConfig> getConfigType() {
        return DruidDataSourceConfig.class;
    }
}
