package org.squirrelframework.cloud.spring;

import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.squirrelframework.cloud.BaseTestClass;

import javax.sql.DataSource;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

/**
 * Created by kailianghe on 11/10/15.
 */
public class ZkSimpleBoneCPDataSourceTest extends BaseTestClass {
    ApplicationContext applicationContext;

    String normalConfig = "{\n" +
            "    \"driverClassName\" : \"org.h2.Driver\",\n" +
            "    \"userName\" : \"sa\",\n" +
            "    \"password\" : \"sa\", \n"+
            "    \"jdbcUrl\" : \"jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1\"\n"+
            "}";

    String errorConfig = "{\n" +
            "    \"minConnectionsPerPartition\" : 0,\n"+  // validation fail here
            "    \"driverClassName\" : \"org.h2.Driver\",\n" +
            "    \"userName\" : \"sa\",\n" +
            "    \"password\" : \"sa\", \n"+
            "    \"jdbcUrl\" : \"jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1\"\n"+
            "}";

    protected void prepareZk(String config) throws Exception {
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb", config.getBytes());
        applicationContext = new ClassPathXmlApplicationContext("classpath:bonecp-simple-ds-context.xml");
    }

    @Test
    public void testSimpleBoneCPDataSource() throws Exception {
        prepareZk(normalConfig);
        DataSource dataSource = applicationContext.getBean(DataSource.class);
        assertThat(dataSource, notNullValue());

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> result = jdbcTemplate.queryForList(SELECT_1);
        assertThat(result.size(), is(1));
        assertThat((Integer) result.get(0).get("1"), is(1));
    }

    @Test
    public void testAddNewProfileThenUpdateThenRemove() throws Exception {
        prepareZk(normalConfig);
        DataSource dataSource = applicationContext.getBean(DataSource.class);
        assertThat(dataSource, notNullValue());

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> result = jdbcTemplate.queryForList(SELECT_DB);
        assertThat((String)result.get(0).get("DATABASE()"), is("TEST"));

        String devConfig = "{\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:preprod;MODE=MySQL;DB_CLOSE_DELAY=-1\"\n"+
                "}";
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/dev", devConfig.getBytes());

        Thread.sleep(1000);
        result = jdbcTemplate.queryForList(SELECT_DB);
        assertThat((String)result.get(0).get("DATABASE()"), is("PREPROD"));

        String newDevConfig = "{\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:prod;MODE=MySQL;DB_CLOSE_DELAY=-1\"\n"+
                "}";
        zkConfigClient.setData().forPath("/database/mydb/dev", newDevConfig.getBytes());

        Thread.sleep(1000);
        result = jdbcTemplate.queryForList(SELECT_DB);
        assertThat((String)result.get(0).get("DATABASE()"), is("PROD"));

        zkConfigClient.delete().forPath("/database/mydb/dev");
        Thread.sleep(1000);
        result = jdbcTemplate.queryForList(SELECT_DB);
        assertThat((String)result.get(0).get("DATABASE()"), is("TEST"));
    }

    @Test(expected = BeanCreationException.class)
    public void testSimpleBoneCPDataSourceWithValidationFail() throws Exception {
        prepareZk(errorConfig);
        applicationContext.getBean(DataSource.class);
    }

}
