package org.squirrelframework.cloud.spring;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by kailianghe on 11/10/15.
 */
public class ZkSimpleDruidDataSourceTest extends AbstractSimpeDataSourceTest {
    String normalConfig = "{\n" +
            "    \"driverClassName\" : \"org.h2.Driver\",\n" +
            "    \"userName\" : \"sa\",\n" +
            "    \"password\" : \"sa\", \n"+
            "    \"jdbcUrl\" : \"jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1;\"\n"+
            "}";

    protected void prepareZk(String config) throws Exception {
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb", config.getBytes());
        applicationContext = new ClassPathXmlApplicationContext("classpath:druid-simple-ds-context.xml");
    }

    @Test
    public void testSimpleDruidDataSource() throws Exception {
        testSimpleDataSource(normalConfig);
    }
}
