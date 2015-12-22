package org.squirrelframework.cloud.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.squirrelframework.cloud.BaseTestClass;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Created by kailianghe on 15/12/22.
 */
public abstract class AbstractSimpeDataSourceTest extends BaseTestClass {

    protected ApplicationContext applicationContext;

    protected abstract void prepareZk(String config) throws Exception;

    protected void testSimpleDataSource(String config) throws Exception {
        prepareZk(config);
        DataSource dataSource = applicationContext.getBean("dataSource", DataSource.class);
        assertThat(dataSource, notNullValue());

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> result = jdbcTemplate.queryForList(SELECT_1);
        assertThat(result.size(), is(1));
        assertThat((Integer) result.get(0).get("1"), is(1));
    }
}
