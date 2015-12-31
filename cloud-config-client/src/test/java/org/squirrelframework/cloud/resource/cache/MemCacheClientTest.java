package org.squirrelframework.cloud.resource.cache;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.squirrelframework.cloud.BaseTestClass;
import org.squirrelframework.cloud.CustomRoutingKeyResolver;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by kailianghe on 15/12/31.
 */
@Ignore
public class MemCacheClientTest extends BaseTestClass {
    ApplicationContext applicationContext;
    MockUserService userService;
    CustomRoutingKeyResolver resolver;

    protected void prepare() throws Exception {
        String cacheConfig = "{\n" +
                "    \"address\" : \"localhost:11211\",\n" +
                "    \"serializationType\" : \"JSON\"\n" +
                "}";
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/cache/mycahce", cacheConfig.getBytes());

        applicationContext = new ClassPathXmlApplicationContext("classpath:memcahce-context.xml");
        Thread.sleep(500);
        userService = applicationContext.getBean("userService", MockUserService.class);
        resolver = applicationContext.getBean("tenantResolver", CustomRoutingKeyResolver.class);
        userService.clearAllCached();

        resolver.key = "t1";
        userService.clearAllCached2();

        resolver.key = "t2";
        userService.clearAllCached2();
    }

    @Test
    public void testUserCache() throws Exception {
        MockUser user1 = new MockUser("1", "hhe1", 31);
        MockUser user11 = userService.findUserById("1");
        Thread.sleep(100);
        MockUser user12 = userService.findUserById("1");
        Thread.sleep(100);
        MockUser user13 = userService.findUserById("1");

        assertThat(user11, equalTo(user1));
        assertThat(user12, equalTo(user1));
        assertThat(user13, equalTo(user1));
        assertThat(userService.getFindInvokeCount(), is(1));
    }

    @Test
    public void testRoutingUserCache() throws Exception {
        MockUser user1 = new MockUser("1", "hhe1", 31);
        CustomRoutingKeyResolver resolver = applicationContext.getBean("tenantResolver", CustomRoutingKeyResolver.class);

        resolver.key = "t1";
        MockUser user11 = userService.findUserById2("1");
        Thread.sleep(100);
        MockUser user12 = userService.findUserById2("1");
        Thread.sleep(100);
        assertThat(user11, equalTo(user1));
        assertThat(user12, equalTo(user1));
        assertThat(userService.getFindInvokeCount(), is(1));

        resolver.key = "t2";
        MockUser user21 = userService.findUserById2("1");
        Thread.sleep(100);
        MockUser user22 = userService.findUserById2("1");
        Thread.sleep(100);
        assertThat(user21, equalTo(user1));
        assertThat(user22, equalTo(user1));
        assertThat(userService.getFindInvokeCount(), is(2));
    }
}
