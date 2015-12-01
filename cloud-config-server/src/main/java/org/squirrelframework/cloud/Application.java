package org.squirrelframework.cloud;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.squirrelframework.cloud.utils.CloudConfigCommon;

import static org.squirrelframework.cloud.utils.CloudConfigCommon.ZK_CONNECT_STRING_KEY;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private Environment environment;

    @Bean
    public CuratorFramework zkClient() {
        String connectionString = resolveConnectionString();
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(connectionString)
                .retryPolicy(retryPolicy)
                .canBeReadOnly(false)
                .build();
        client.start();
        return client;
    }

    private String resolveConnectionString() {
        if(environment!=null && environment.containsProperty(ZK_CONNECT_STRING_KEY)) {
            return environment.getProperty(ZK_CONNECT_STRING_KEY);
        }
        return "127.0.0.1:2181";
    }
}
