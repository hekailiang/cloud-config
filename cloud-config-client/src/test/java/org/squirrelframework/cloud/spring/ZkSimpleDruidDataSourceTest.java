package org.squirrelframework.cloud.spring;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.squirrelframework.cloud.BaseTestClass;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by kailianghe on 11/10/15.
 */
public class ZkSimpleDruidDataSourceTest extends BaseTestClass {
    ApplicationContext applicationContext;

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
        prepareZk(normalConfig);
        DataSource dataSource = applicationContext.getBean(DataSource.class);
        assertThat(dataSource, notNullValue());

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> result = jdbcTemplate.queryForList(SELECT_1);
        assertThat(result.size(), is(1));
        assertThat((Integer) result.get(0).get("1"), is(1));
    }
}
