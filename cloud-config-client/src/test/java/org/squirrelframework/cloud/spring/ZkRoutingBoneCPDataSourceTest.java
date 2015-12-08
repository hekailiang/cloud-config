package org.squirrelframework.cloud.spring;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.squirrelframework.cloud.BaseTestClass;

import javax.sql.DataSource;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by kailianghe on 11/10/15.
 */
public class ZkRoutingBoneCPDataSourceTest extends BaseTestClass {
    ApplicationContext applicationContext;

    @Override
    protected void prepare() throws Exception {
        String aConfig = "{\n" +
                "    \"driverClassName\" : \"org.h2.Driver\",\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:a;MODE=MySQL;DB_CLOSE_DELAY=-1\"\n"+
                "}";

        String bConfig = "{\n" +
                "    \"driverClassName\" : \"org.h2.Driver\",\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:b;MODE=MySQL;DB_CLOSE_DELAY=-1\"\n"+
                "}";

        String cConfig = "{\n" +
                "    \"driverClassName\" : \"org.h2.Driver\",\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:c;MODE=MySQL;DB_CLOSE_DELAY=-1\"\n"+
                "}";

        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/a", aConfig.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/b", bConfig.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/unknown", cConfig.getBytes());
        applicationContext = new ClassPathXmlApplicationContext("classpath:bonecp-routing-ds-context.xml");
    }

    @Test
    public void testRoutingDataSource() {
        DataSource dataSource = applicationContext.getBean("dataSource", DataSource.class);
        assertThat(dataSource, notNullValue());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        List<Map<String, Object>> result;
        result = jdbcTemplate.queryForList(SELECT_DB);
        assertThat((String)result.get(0).get("DATABASE()"), is("A"));
        result = jdbcTemplate.queryForList(SELECT_DB);
        assertThat((String)result.get(0).get("DATABASE()"), is("B"));
        result = jdbcTemplate.queryForList(SELECT_DB);
        assertThat((String)result.get(0).get("DATABASE()"), is("A"));
        result = jdbcTemplate.queryForList(SELECT_DB);
        assertThat((String)result.get(0).get("DATABASE()"), is("B"));
    }

    @Test
    public void testRemoveDataSourceAndFallbackDataSourceThenReAddNewDataSource() throws Exception {
        DataSource dataSource = applicationContext.getBean("dataSource", DataSource.class);
        assertThat(dataSource, notNullValue());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        List<Map<String, Object>> result;
        result = jdbcTemplate.queryForList(SELECT_DB);
        assertThat((String)result.get(0).get("DATABASE()"), is("A"));
        result = jdbcTemplate.queryForList(SELECT_DB);
        assertThat((String)result.get(0).get("DATABASE()"), is("B"));

        zkConfigClient.delete().deletingChildrenIfNeeded().forPath("/database/mydb/a");
        Thread.sleep(500);

        result = jdbcTemplate.queryForList(SELECT_DB);
        assertThat((String) result.get(0).get("DATABASE()"), is("C"));
        result = jdbcTemplate.queryForList(SELECT_DB);
        assertThat((String) result.get(0).get("DATABASE()"), is("B"));

        String dConfig = "{\n" +
                "    \"driverClassName\" : \"org.h2.Driver\",\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:d;MODE=MySQL;DB_CLOSE_DELAY=-1\"\n"+
                "}";
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/a", dConfig.getBytes());
        Thread.sleep(500);

        result = jdbcTemplate.queryForList(SELECT_DB);
        assertThat((String) result.get(0).get("DATABASE()"), is("D"));
        result = jdbcTemplate.queryForList(SELECT_DB);
        assertThat((String) result.get(0).get("DATABASE()"), is("B"));
    }
}
