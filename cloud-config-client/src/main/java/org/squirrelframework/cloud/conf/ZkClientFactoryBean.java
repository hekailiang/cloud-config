package org.squirrelframework.cloud.conf;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import static org.squirrelframework.cloud.utils.CloudConfigCommon.ZK_CONNECT_STRING_KEY;
import static org.squirrelframework.cloud.utils.CloudConfigCommon.ZK_CREDENTIAL_STRING_KEY;

/**
 * Created by kailianghe on 8/29/15.
 */
public class ZkClientFactoryBean extends AbstractFactoryBean<CuratorFramework> implements EnvironmentAware {

    public static final int MAX_RETRIES = 3;

    public static final int BASE_SLEEP_TIME = 3000;

    public static final String SCHEME_DIGEST = "digest";

    private String connectionString;

    private String credentialString;

    private int maxRetries = MAX_RETRIES;

    private int baseSleepTime = BASE_SLEEP_TIME;

    private boolean canReadOnly;

    private Environment environment;

    @Override
    public Class<?> getObjectType() {
        return CuratorFramework.class;
    }

    @Override
    protected CuratorFramework createInstance() throws Exception {
        String connectionString = resolveConnectionString();
        if(connectionString==null) {
            throw new IllegalArgumentException("Cannot resolve zookeeper connection string");
        }

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(baseSleepTime, maxRetries);
        Builder curatorFrameworkBuilder = CuratorFrameworkFactory.builder()
            .connectString(connectionString)
            .retryPolicy(retryPolicy)
            .canBeReadOnly(canReadOnly);        
        
        String credentialString = resolveCredentialString();
        if(credentialString!=null) {
            curatorFrameworkBuilder.authorization(SCHEME_DIGEST, credentialString.getBytes());
        }
        
        CuratorFramework client = curatorFrameworkBuilder.build();
        client.start();
        
        return client;
    }

    protected void destroyInstance(CuratorFramework instance) throws Exception {
        if(instance!=null) {
            instance.close();
        }
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public void setCredentialString(String credentialString) {
        this.credentialString = credentialString;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void setBaseSleepTime(int baseSleepTime) {
        this.baseSleepTime = baseSleepTime;
    }

    public void setCanReadOnly(boolean canReadOnly) {
        this.canReadOnly = canReadOnly;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private String resolveConnectionString() {
        if(environment.containsProperty(ZK_CONNECT_STRING_KEY)) {
            return environment.getProperty(ZK_CONNECT_STRING_KEY);
        }
        return connectionString;
    }

    private String resolveCredentialString() {
        if(environment.containsProperty(ZK_CREDENTIAL_STRING_KEY)) {
            return environment.getProperty(ZK_CREDENTIAL_STRING_KEY);
        }
        return credentialString;
    }
}
