package org.squirrelframework.cloud.resource.database;

import javax.validation.constraints.Min;

/**
 * Created by kailianghe on 11/10/15.
 */
public class C3P0DataSourceConfig extends JdbcDataSourceConfig {

    @Min(1)
    private int acquireIncrement = 1;

    private int idleTestPeriod = 1800; // seconds

    @Min(1)
    private int maxPoolSize = 10;

    @Min(1)
    private int minPoolSize = 1;

    @Min(0)
    private int maxStatements = 0;

    private int maxIdleTime = 120; // seconds

    public int getAcquireIncrement() {
        return acquireIncrement;
    }

    public void setAcquireIncrement(int acquireIncrement) {
        this.acquireIncrement = acquireIncrement;
    }

    public int getIdleTestPeriod() {
        return idleTestPeriod;
    }

    public void setIdleTestPeriod(int idleTestPeriod) {
        this.idleTestPeriod = idleTestPeriod;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public int getMaxStatements() {
        return maxStatements;
    }

    public void setMaxStatements(int maxStatements) {
        this.maxStatements = maxStatements;
    }

    public int getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(int maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }
}
