package org.squirrelframework.cloud.resource.database;

import javax.validation.constraints.Min;

/**
 * Created by kailianghe on 9/6/15.
 */
public class BoneCPDataSourceConfig extends JdbcDataSourceConfig {

    private int idleMaxAgeInMinutes = 120;

    private int idleConnectionTestPeriodInMinutes = 30;

    private int maxConnectionsPerPartition = 5;

    @Min(1)
    private int minConnectionsPerPartition = 1;

    @Min(1)
    private int partitionCount = 2;

    @Min(1)
    private int acquireIncrement = 1;

    private int statementsCacheSize = 50;

    public int getIdleMaxAgeInMinutes() {
        return idleMaxAgeInMinutes;
    }

    public void setIdleMaxAgeInMinutes(int idleMaxAgeInMinutes) {
        this.idleMaxAgeInMinutes = idleMaxAgeInMinutes;
    }

    public int getIdleConnectionTestPeriodInMinutes() {
        return idleConnectionTestPeriodInMinutes;
    }

    public void setIdleConnectionTestPeriodInMinutes(int idleConnectionTestPeriodInMinutes) {
        this.idleConnectionTestPeriodInMinutes = idleConnectionTestPeriodInMinutes;
    }

    public int getMaxConnectionsPerPartition() {
        return maxConnectionsPerPartition;
    }

    public void setMaxConnectionsPerPartition(int maxConnectionsPerPartition) {
        this.maxConnectionsPerPartition = maxConnectionsPerPartition;
    }

    public int getMinConnectionsPerPartition() {
        return minConnectionsPerPartition;
    }

    public void setMinConnectionsPerPartition(int minConnectionsPerPartition) {
        this.minConnectionsPerPartition = minConnectionsPerPartition;
    }

    public int getPartitionCount() {
        return partitionCount;
    }

    public void setPartitionCount(int partitionCount) {
        this.partitionCount = partitionCount;
    }

    public int getAcquireIncrement() {
        return acquireIncrement;
    }

    public void setAcquireIncrement(int acquireIncrement) {
        this.acquireIncrement = acquireIncrement;
    }

    public int getStatementsCacheSize() {
        return statementsCacheSize;
    }

    public void setStatementsCacheSize(int statementsCacheSize) {
        this.statementsCacheSize = statementsCacheSize;
    }

}
