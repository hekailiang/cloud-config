package org.squirrelframework.cloud.resource.database;

import org.squirrelframework.cloud.resource.ReloadCallback;
import com.google.common.base.Preconditions;
import com.jolbox.bonecp.BoneCPDataSource;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;

/**
 * Created by kailianghe on 9/6/15.
 */
public class BoneCPDataSourceFactoryBean extends AbstractDataSourceFactoryBean<BoneCPDataSourceConfig> {

    @Override
    protected DataSource createInstance() throws Exception {
        Preconditions.checkNotNull(config, "config cannot be empty");
        final BoneCPDataSource targetDataSource = createNewDataSource();
        final DelegatingDataSource proxyDataSource = createProxyDataSource(targetDataSource);
        config.setReloadCallback(new ReloadCallback() {
            @Override
            public void reload() throws Exception {
                // switch data source
                BoneCPDataSource oldTargetDataSource = (BoneCPDataSource)proxyDataSource.getTargetDataSource();
                BoneCPDataSource newTargetDataSource = createNewDataSource();
                newTargetDataSource.getConnection().close(); // initialize a connection (+ throw it away) to force the datasource to initialize the pool

                proxyDataSource.setTargetDataSource(newTargetDataSource);
                oldTargetDataSource.close();
            }
        });
        return proxyDataSource;
    }

    private BoneCPDataSource createNewDataSource() {
        BoneCPDataSource target = new BoneCPDataSource();
        target.setDriverClass(config.getDriverClassName());
        target.setJdbcUrl(config.getJdbcUrl());
        target.setUsername(config.getUserName());
        target.setPassword(config.getPassword());
        target.setIdleConnectionTestPeriodInMinutes(config.getIdleConnectionTestPeriodInMinutes());
        target.setIdleMaxAgeInMinutes(config.getIdleMaxAgeInMinutes());
        target.setMaxConnectionsPerPartition(config.getMaxConnectionsPerPartition());
        target.setMinConnectionsPerPartition(config.getMinConnectionsPerPartition());
        target.setPartitionCount(config.getPartitionCount());
        target.setAcquireIncrement(config.getAcquireIncrement());
        target.setStatementsCacheSize(config.getStatementsCacheSize());
        target.setDisableJMX(true);
        return target;
    }

    @Override
    protected void destroyInstance(DataSource instance) throws Exception {
        ((BoneCPDataSource)((DelegatingDataSource)instance).getTargetDataSource()).close();
    }

    @Override
    protected String baseDataSourceConfigType() {
        return BoneCPDataSourceConfig.class.getName();
    }
}
