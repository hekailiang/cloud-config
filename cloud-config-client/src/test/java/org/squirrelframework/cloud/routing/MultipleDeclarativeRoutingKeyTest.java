package org.squirrelframework.cloud.routing;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.squirrelframework.cloud.BaseTestClass;
import org.squirrelframework.cloud.annotation.RoutingKey;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by kailianghe on 15/12/9.
 */
public class MultipleDeclarativeRoutingKeyTest extends AbstractNestedRoutingTest {

    @Override
    public void prepare() throws Exception {
        super.prepare();
        applicationContext = new ClassPathXmlApplicationContext("classpath:nested-declarative-routing-context.xml");
    }

    @Test
    public void testMultipleDeclarativeRoutingKey() {
        Outer outer = applicationContext.getBean(Outer.class);
        assertThat(outer.toA1(), is("A1"));
        assertThat(outer.toA2(), is("A2"));
        assertThat(outer.toB1(), is("B1"));
        assertThat(outer.toB2(), is("B2"));
        assertThat(outer.toUnknown(), is("UNKNOWN"));
    }


    public static class Outer {

        Inner inner;

        public Outer(Inner inner) {
            this.inner = inner;
        }

        @RoutingKey("a")
        public String toA1() {
            return inner.one();
        }

        @RoutingKey("a")
        public String toA2() {
            return inner.two();
        }

        @RoutingKey("b")
        public String toB1() {
            return inner.one();
        }

        @RoutingKey("b")
        public String toB2() {
            return inner.two();
        }

        @RoutingKey("whatever")
        public String toUnknown() {
            return inner.one();
        }
    }

    public static class Inner {

        JdbcTemplate jdbcTemplate;

        public Inner(DataSource ds) {
            this.jdbcTemplate = new JdbcTemplate(ds);
        }

        @RoutingKey("1")
        public String one() {
            return getDatabase();
        }

        @RoutingKey("2")
        public String two() {
            return getDatabase();
        }

        private String getDatabase() {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(BaseTestClass.SELECT_DB);
            return (String)result.get(0).get("DATABASE()");
        }
    }

}


