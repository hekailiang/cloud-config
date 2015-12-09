package org.squirrelframework.cloud.spring;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.squirrelframework.cloud.BaseTestClass;
import org.squirrelframework.cloud.resource.database.BoneCPDataSourceConfig;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by kailianghe on 11/30/15.
 */
public class ResourceConfigPropertyPlaceholderTest  extends BaseTestClass {

    ApplicationContext applicationContext;

    @Override
    protected void prepare() throws Exception {
        String mailConfig = "{" +
                "\"h2.driver.name\": \"org.h2.Driver\", " +
                "\"h2.url\": \"jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1\", " +
                "\"idle.max.age\": 20" +
                "}";
        zkPropsClient.create().creatingParentsIfNeeded().forPath("/variables", mailConfig.getBytes());

        String config = "{\n" +
                "    \"driverClassName\" : \"${h2.driver.name}\",\n" +
                "    \"userName\" : \"sa\",\n" +
                "    \"password\" : \"sa\", \n"+
                "    \"jdbcUrl\" : \"${h2.url}\", \n"+
                "    \"idleMaxAgeInMinutes\" : ${idle.max.age}"+
                "}";
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb", config.getBytes());
        applicationContext = new ClassPathXmlApplicationContext("classpath:rc-propertyplaceholder-context.xml");
    }

    @Test
    public void testResourceConfigPlaceholderReplace() {
        BoneCPDataSourceConfig config = applicationContext.getBean(BoneCPDataSourceConfig.class);
        assertThat(config, notNullValue());
        assertThat(config.getDriverClassName(), is("org.h2.Driver"));
        assertThat(config.getJdbcUrl(), is("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1"));
        assertThat(config.getIdleMaxAgeInMinutes(), is(20));
    }
}
