package org.squirrelframework.cloud.conf;

import com.google.common.collect.Lists;
import org.springframework.util.StringUtils;
import org.squirrelframework.cloud.utils.CloudConfigCommon;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Created by kailianghe on 8/28/15.
 */
public class ZkPropertyPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer {

    public static final Logger logger = LoggerFactory.getLogger(ZkPropertyPlaceholderConfigurer.class);

    public static final String REMOTE_PROPERTIES_PROPERTY_SOURCE_NAME = "remoteProperties:";

    public static final int MAX_DEPTH = Integer.MAX_VALUE;

    private CuratorFramework client;

    private MutablePropertySources propertySources;

    private JsonFlattenConverter jsonFlattenConverter = new JsonFlattenConverter();

    private String[] pathArray;

    private int maxDepth = MAX_DEPTH;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // setup property sources
        if (this.propertySources == null) {
            this.propertySources = new MutablePropertySources();

            // --- hack super class private field environment
            Environment environment;
            try {
                Field field = getClass().getSuperclass().getDeclaredField("environment");
                if(!field.isAccessible()) {
                    field.setAccessible(true);
                }
                environment = (Environment) field.get(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // --- hack super class private field environment

            if (environment != null) {
                this.propertySources.addLast(
                        new PropertySource<Environment>(ENVIRONMENT_PROPERTIES_PROPERTY_SOURCE_NAME, environment) {
                            @Override
                            public String getProperty(String key) {
                                return this.source.getProperty(key);
                            }
                        }
                );
            }

            try {
                PropertySource<?> localPropertySource =
                        new PropertiesPropertySource(LOCAL_PROPERTIES_PROPERTY_SOURCE_NAME, mergeProperties());
                if (this.localOverride) {
                    this.propertySources.addFirst(localPropertySource);
                }
                else {
                    this.propertySources.addLast(localPropertySource);
                }
            } catch (IOException ex) {
                throw new BeanInitializationException("Could not load properties", ex);
            }

            // add zk properties
            try {
                if(client!=null) {
                    List<PropertySource<?>> remotePropertySources = Lists.newArrayList();
                    for(String path : pathArray) {
                        PropertySource<?> remotePropertySource = new PropertiesPropertySource(
                                REMOTE_PROPERTIES_PROPERTY_SOURCE_NAME+path,
                                fetchRemoteProperties(path, CloudConfigCommon.getProfiles())
                        );
                        remotePropertySources.add(remotePropertySource);
                    }
                    if(!localOverride) {
                        Collections.reverse(remotePropertySources);
                    }
                    for(PropertySource<?> remotePropertySource : remotePropertySources) {
                        if(localOverride) {
                            this.propertySources.addLast(remotePropertySource);
                        } else {
                            this.propertySources.addFirst(remotePropertySource);
                        }
                    }
                }
            } catch (Exception e) {
                throw new BeanInitializationException("Could not load remote properties", e);
            }
        }

        setPropertySources(propertySources);
        super.postProcessBeanFactory(beanFactory);
    }

    private Properties fetchRemoteProperties(String path, String[] profiles) throws Exception {
        Properties properties = new Properties();
        ZkPath[] zkPaths = new ZkPath[profiles.length+1];
        zkPaths[0] = ZkPath.create(path);
        for(int i=0; i<profiles.length; ++i) {
            zkPaths[i+1] = zkPaths[0].appendChild(profiles[i]);
        }
        fetchPropertiesRecursive(client, zkPaths, properties);
        properties.list(System.out);
        return properties;
    }

    public void fetchPropertiesRecursive(CuratorFramework zkClient, ZkPath[] paths, Properties properties) throws Exception {
        if(paths!=null && paths.length>0) {
            for(int i=0; i<paths.length; ++i) {
                fetchPropertiesRecursive(zkClient, paths[i], maxDepth, properties);
            }
        }
    }

    private void fetchPropertiesRecursive(CuratorFramework zkClient, ZkPath path, int depth, Properties properties) throws Exception {
        Object checkExist = zkClient.checkExists().forPath(path.toString());
        if(checkExist==null) {
            logger.warn("Undefined zk node \"{}\"", path.toString());
            return;
        }
        String value = CloudConfigCommon.bytes2String( zkClient.getData().forPath(path.toString()) );
        if(!value.isEmpty()) {
            jsonFlattenConverter.flatten(value, properties);
        }
        if (depth > 0) {
            List<String> children = zkClient.getChildren().forPath(path.toString());
            for (String child : children) {
                fetchPropertiesRecursive(zkClient, path.appendChild(child), depth-1, properties);
            }
        }
    }

    public void setClient(CuratorFramework client) {
        this.client = client.usingNamespace(CloudConfigCommon.PROPERTY_ROOT);
    }

    public void setPath(String path) {
        this.pathArray = StringUtils.tokenizeToStringArray(path, ",;|+");
    }

    public void setJsonFlattenConverter(JsonFlattenConverter jsonFlattenConverter) {
        this.jsonFlattenConverter = jsonFlattenConverter;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }
}
