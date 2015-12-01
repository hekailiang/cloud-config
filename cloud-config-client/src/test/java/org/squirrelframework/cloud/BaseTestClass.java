package org.squirrelframework.cloud;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.squirrelframework.cloud.utils.CloudConfigCommon;

/**
 * Created by kailianghe on 9/24/15.
 */
public class BaseTestClass {

    protected TestingServer server = newTestingServer(1234);

    protected CuratorFramework zkRootClient;

    protected CuratorFramework zkConfigClient;

    protected CuratorFramework zkPropsClient;

    public static String SELECT_DB = "SELECT DATABASE();";

    public static String SELECT_1 = "SELECT 1;";

    @Before
    public void init() throws Throwable {
        zkRootClient = CuratorFrameworkFactory.builder()
                .connectString(server.getConnectString())
                .retryPolicy(new BoundedExponentialBackoffRetry(10, 100, 7))
                .build();
        zkRootClient.start();

        ZooKeeper zk = zkRootClient.getZookeeperClient().getZooKeeper();
        ZKPaths.mkdirs(zk, "/"+ CloudConfigCommon.CONFIG_ROOT);
        ZKPaths.mkdirs(zk, "/"+CloudConfigCommon.PROPERTY_ROOT);

        zkConfigClient = zkRootClient.usingNamespace(CloudConfigCommon.CONFIG_ROOT);
        zkPropsClient  = zkRootClient.usingNamespace(CloudConfigCommon.PROPERTY_ROOT);

        prepare();
    }

    protected void prepare() throws Exception {
    }

    @After
    public void close() throws Throwable {
        finish();
        zkRootClient.close();
        CloseableUtils.closeQuietly(server);
    }

    protected void finish() throws Exception {
        System.getProperties().remove(CloudConfigCommon.CONFIG_PROFILE_KEY);
    }

    private static TestingServer newTestingServer(int port) {
        try {
            return new TestingServer(port);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
