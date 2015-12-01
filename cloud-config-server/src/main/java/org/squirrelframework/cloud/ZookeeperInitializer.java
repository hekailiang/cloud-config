package org.squirrelframework.cloud;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import static org.squirrelframework.cloud.utils.CloudConfigCommon.*;

/**
 * Created by kailianghe on 12/1/15.
 */
@Component
public class ZookeeperInitializer implements CommandLineRunner {

    @Autowired
    private CuratorFramework zkClient;

    @Override
    public void run(String... args) throws Exception {
        // check minimum requirement for zookeeper environment
        safeCreateZkNodeIfNotExists(zkClient, PROPERTY_ROOT);
        safeCreateZkNodeIfNotExists(zkClient, CONFIG_ROOT);
    }
}
