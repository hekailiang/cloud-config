package org.squirrelframework.cloud;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.squirrelframework.cloud.utils.CloudConfigCommon;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * Created by kailianghe on 11/12/15.
 */
public class CloudConfigSample {

    public static void main(String[] args) throws Exception {
        TestingServer server = new TestingServer(1234);

        try {
            prepare(server);
            Thread.sleep(1000);

            ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring-context.xml");
            DataSource dataSource = applicationContext.getBean(DataSource.class);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            List<Map<String, Object>> result = jdbcTemplate.queryForList("SELECT DATABASE();");
            System.out.println(result);
        } finally {
            CloseableUtils.closeQuietly(server);
        }
    }

    private static void prepare(TestingServer server) throws Exception {
        CuratorFramework zkRootClient = null;
        try {
            zkRootClient = CuratorFrameworkFactory.builder()
                    .connectString(server.getConnectString())
                    .retryPolicy(new BoundedExponentialBackoffRetry(10, 100, 7))
                    .build();
            zkRootClient.start();

            ZooKeeper zk = zkRootClient.getZookeeperClient().getZooKeeper();
            ZKPaths.mkdirs(zk, "/" + CloudConfigCommon.CONFIG_ROOT);
            ZKPaths.mkdirs(zk, "/"+CloudConfigCommon.PROPERTY_ROOT);

            CuratorFramework zkConfigClient = zkRootClient.usingNamespace(CloudConfigCommon.CONFIG_ROOT);
//        CuratorFramework zkPropsClient  = zkRootClient.usingNamespace(CloudConfigCommon.PROPERTY_ROOT);

            String config = "{\n" +
                    "    \"driverClassName\" : \"com.mysql.jdbc.Driver\",\n" +
                    "    \"userName\" : \"root\",\n" +
                    "    \"password\" : \"1111\", \n"+
                    "    \"jdbcUrl\" : \"jdbc:mysql://127.0.0.1:3306/a?characterEncoding=utf8&createDatabaseIfNotExist=true\"\n"+
                    "}";

            zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb", config.getBytes());
        } finally {
            if(zkRootClient!=null) {
                zkRootClient.close();
            }
        }
    }

}
