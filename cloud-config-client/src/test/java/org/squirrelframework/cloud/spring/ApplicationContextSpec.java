package org.squirrelframework.cloud.spring;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

/**
 * Created by kailianghe on 11/9/15.
 */
@ContextConfiguration(locations = "classpath:zkclient-context.xml")
public class ApplicationContextSpec extends SpringBaseTestClass {

    @Test
    public void testZkClientInjection() {
        CuratorFramework curatorClient = applicationContext.getBean(CuratorFramework.class);
        assertThat(curatorClient, notNullValue());
        assertThat(curatorClient.getZookeeperClient().getCurrentConnectionString(), equalTo(server.getConnectString()));
    }
}
