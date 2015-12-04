package org.squirrelframework.cloud;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.squirrelframework.cloud.conf.JsonFlattenConverter;

import static org.squirrelframework.cloud.utils.CloudConfigCommon.ZK_CONNECT_STRING_KEY;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        processArguments(args);
        SpringApplication.run(Application.class, args);
    }

    private static void processArguments(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--")) {
                String optionText = arg.substring(2, arg.length());
                String optionName;
                String optionValue = null;
                if (optionText.contains("=")) {
                    optionName = optionText.substring(0, optionText.indexOf("="));
                    optionValue = optionText.substring(optionText.indexOf("=")+1, optionText.length());
                } else {
                    optionName = optionText;
                }
                if (optionName.isEmpty() || (optionValue != null && optionValue.isEmpty())) {
                    throw new IllegalArgumentException("Invalid argument syntax: " + arg);
                }
                System.setProperty(optionName, optionValue);
            }
        }
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

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Bean
    public JsonFlattenConverter jsonFlattenConverter() {
        JsonFlattenConverter jsonFlattenConverter = new JsonFlattenConverter();
        jsonFlattenConverter.setAllowOverride(false);
        return jsonFlattenConverter;
    }

    private String resolveConnectionString() {
        if(environment!=null && environment.containsProperty(ZK_CONNECT_STRING_KEY)) {
            return environment.getProperty(ZK_CONNECT_STRING_KEY);
        }
        return "127.0.0.1:2181";
    }
}
