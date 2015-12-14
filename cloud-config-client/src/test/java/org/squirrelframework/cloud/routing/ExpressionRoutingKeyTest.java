package org.squirrelframework.cloud.routing;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.squirrelframework.cloud.annotation.RoutingKey;
import org.squirrelframework.cloud.annotation.RoutingVariable;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by kailianghe on 15/12/12.
 */
public class ExpressionRoutingKeyTest {

    public static final String TEST_ROUTING_KEY = " # { #args[0] + separator + #args[1] } ";

    public static final String PROD_ID_ROUTING_KEY = "#{ \"${name}\" + separator + #productId % 4 }";

    @Test
    public void testELExpressionRoutingKey() {
        ApplicationContext applicationContext =
                new ClassPathXmlApplicationContext("classpath:expression-routingkey-context.xml");
        TestRoutingKey testRoutingKey = applicationContext.getBean(TestRoutingKey.class);
        assertThat( testRoutingKey.sampleRoutingKey("hello", "world"), is("hello-world") );
        assertThat( testRoutingKey.prodRoutingKey("", 1L, ""), is("prod-1") );
        assertThat( testRoutingKey.prodRoutingKey("", 2L, ""), is("prod-2") );
        assertThat( testRoutingKey.prodRoutingKey("", 3L, ""), is("prod-3") );
        assertThat( testRoutingKey.prodRoutingKey("", 4L, ""), is("prod-0") );
    }

    public static class TestRoutingKey {

        private String separator;

        @RoutingKey(TEST_ROUTING_KEY)
        public String sampleRoutingKey(String a, String b) {
            return RoutingKeyHolder.getDeclarativeRoutingKey();
        }

        @RoutingKey(PROD_ID_ROUTING_KEY)
        public String prodRoutingKey(String a, @RoutingVariable("productId") long productId, String b) {
            return RoutingKeyHolder.getDeclarativeRoutingKey();
        }

        public String getSeparator() {
            return separator;
        }

        public void setSeparator(String separator) {
            this.separator = separator;
        }
    }
}
