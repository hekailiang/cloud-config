package org.squirrelframework.cloud.resource.database;

import org.squirrelframework.cloud.annotation.Secret;
import org.squirrelframework.cloud.resource.CloudResourceConfig;

import javax.validation.constraints.NotNull;

/**
 * Created by kailianghe on 9/10/15.
 */
public class JdbcDataSourceConfig extends CloudResourceConfig {

    @NotNull
    private String jdbcUrl;

    @NotNull
    private String userName;

    @NotNull
    private String password;

    @NotNull
    private String driverClassName;

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Secret
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }
}
