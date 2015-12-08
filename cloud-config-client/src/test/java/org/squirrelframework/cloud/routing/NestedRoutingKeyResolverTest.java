package org.squirrelframework.cloud.routing;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.squirrelframework.cloud.BaseTestClass;
import org.squirrelframework.cloud.CustomRoutingKeyResolver;
import org.squirrelframework.cloud.resource.database.BoneCPDataSourceConfig;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by kailianghe on 15/12/8.
 */
public class NestedRoutingKeyResolverTest extends BaseTestClass {

    ApplicationContext applicationContext;

    @Override
    protected void prepare() throws Exception {
        String common = "{\n" +
                "    \"driverClassName\" : \"org.h2.Driver\"\n" +
                "}";

        String a1c = "{\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:a1;MODE=MySQL;DB_CLOSE_DELAY=-1\"\n"+
                "}";

        String a2c = "{\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:a2;MODE=MySQL;DB_CLOSE_DELAY=-1\"\n"+
                "}";

        String b1c = "{\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:b1;MODE=MySQL;DB_CLOSE_DELAY=-1\"\n"+
                "}";

        String b2c = "{\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:b2;MODE=MySQL;DB_CLOSE_DELAY=-1\"\n"+
                "}";

        String cc = "{\n" +
                "    \"driverClassName\" : \"org.h2.Driver\",\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:c;MODE=MySQL;DB_CLOSE_DELAY=-1\"\n"+
                "}";

        String uc = "{\n" +
                "    \"driverClassName\" : \"org.h2.Driver\",\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:unknown;MODE=MySQL;DB_CLOSE_DELAY=-1\"\n"+
                "}";

        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/a", common.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/b", common.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/a/1", a1c.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/a/2", a2c.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/b/1", b1c.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/b/2", b2c.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/c",   cc.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/unknown", uc.getBytes());
        applicationContext = new ClassPathXmlApplicationContext("classpath:nested-routing-ds-context.xml");
    }

    @Test
    public void testNestedRoutingDataSource() throws Exception {
        CustomRoutingKeyResolver r1 = applicationContext.getBean("r1", CustomRoutingKeyResolver.class);
        CustomRoutingKeyResolver r2 = applicationContext.getBean("r2", CustomRoutingKeyResolver.class);
        DataSource dataSource = applicationContext.getBean("dataSource", DataSource.class);
        assertThat(dataSource, notNullValue());

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        List<Map<String, Object>> result;

        r1.key = "a"; r2.key = "1";
        result = jdbcTemplate.queryForList(SELECT_DB);
        assertThat((String)result.get(0).get("DATABASE()"), is("A1"));

        r1.key = "a"; r2.key = "2";
        result = jdbcTemplate.queryForList(SELECT_DB);
        assertThat((String)result.get(0).get("DATABASE()"), is("A2"));

        r1.key = "b"; r2.key = "1";
        result = jdbcTemplate.queryForList(SELECT_DB);
        assertThat((String)result.get(0).get("DATABASE()"), is("B1"));

        r1.key = "b"; r2.key = "2";
        result = jdbcTemplate.queryForList(SELECT_DB);
        assertThat((String)result.get(0).get("DATABASE()"), is("B2"));

        r1.key = "c"; r2.key = "43248923";
        result = jdbcTemplate.queryForList(SELECT_DB);
        assertThat((String)result.get(0).get("DATABASE()"), is("C"));

        r1.key = "dsadkj"; r2.key = "231312";
        result = jdbcTemplate.queryForList(SELECT_DB);
        assertThat((String)result.get(0).get("DATABASE()"), is("UNKNOWN"));

        Set<String> dsBeans = applicationContext.getBeansOfType(DataSource.class).keySet();
        Set<String> cnfBeans = applicationContext.getBeansOfType(BoneCPDataSourceConfig.class).keySet();
        zkConfigClient.delete().forPath("/database/mydb/a/1");
        Thread.sleep(500);

        zkConfigClient.delete().forPath("/database/mydb/a/2");
        Thread.sleep(500);

        zkConfigClient.delete().forPath("/database/mydb/a");
        Thread.sleep(500);

        Set<String> nowDsBeans = applicationContext.getBeansOfType(DataSource.class).keySet();
        Set<String> nowCnfBeans = applicationContext.getBeansOfType(BoneCPDataSourceConfig.class).keySet();
        dsBeans.removeAll(nowDsBeans);
        cnfBeans.removeAll(nowCnfBeans);
        assertThat(dsBeans, is((Set<String>) Sets.newHashSet("__database_mydb_aDS", "__database_mydb_a_1DS", "__database_mydb_a_2DS")));
        assertThat(cnfBeans, is((Set<String>) Sets.newHashSet("__database_mydb_a_1CNF", "__database_mydb_a_2CNF")));
    }

}
