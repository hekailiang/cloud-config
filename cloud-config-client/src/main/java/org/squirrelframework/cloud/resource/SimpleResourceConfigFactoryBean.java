package org.squirrelframework.cloud.resource;

import org.squirrelframework.cloud.conf.ZkPath;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by kailianghe on 9/6/15.
 */
public class SimpleResourceConfigFactoryBean<T extends CloudResourceConfig> extends AbstractResourceFactoryBean<T> {

    private static final Logger logger = LoggerFactory.getLogger(SimpleResourceConfigFactoryBean.class);

    private T baseConfig;

    @Override
    protected T createInstance() throws Exception {
        // get parent common config
        T parentConfig = null;
        ZkPath parentPath = ZkPath.create(path).getParent();
        if(parentPath!=null) {
            parentConfig = createConfig(client.getData().forPath(parentPath.toString()) );
        }

        // get basic config
        baseConfig = createConfig( client.getData().forPath(path), parentConfig );

        // get profile specific current config
        T currentConfig = baseConfig;
        List<String> profileNodes = client.getChildren().forPath(path);
        for(String configProfile : configProfiles) {
            if( profileNodes.contains(configProfile) ) {
                byte[] data = client.getData().forPath(path+"/"+configProfile);
                currentConfig = createConfig( data, currentConfig );
            }
        }

        // apply ip address specific settings
        for(String profileNode : profileNodes) {
            if(canApplyForLocalMachine(profileNode)) {
                byte[] data = client.getData().forPath(path+"/"+profileNode);
                currentConfig = createConfig( data, currentConfig );
            }
        }

        logger.info("Load config object: \"{}\".", currentConfig);
        return currentConfig;
    }

    @Override
    protected void handleChildUpdated(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
        ChildData childData = event.getData();
        String nodeName = safeGetNodeNameFromEvent(event);
        if(Arrays.asList(configProfiles).contains(nodeName) || canApplyForLocalMachine(nodeName)) {
            T newConfig = createConfig(childData.getData(), getObject());
            BeanUtils.copyProperties(newConfig, getObject());
            getObject().reload();
        }
    }

    @Override
    protected void handleChildAdded(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
        handleChildUpdated(client, event);
    }

    @Override
    protected void handleChildRemoved(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
        T newConfig = createInstance();
        BeanUtils.copyProperties(newConfig, getObject());
        getObject().reload();
    }

    @Override
    public Class<?> getObjectType() {
        return resourceType;
    }


}
