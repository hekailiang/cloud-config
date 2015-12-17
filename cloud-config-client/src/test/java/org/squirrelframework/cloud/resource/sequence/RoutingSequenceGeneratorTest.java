package org.squirrelframework.cloud.resource.sequence;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.squirrelframework.cloud.BaseTestClass;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by kailianghe on 15/12/17.
 */
public class RoutingSequenceGeneratorTest extends BaseTestClass {

    ApplicationContext applicationContext;

    protected void prepare() throws Exception {
        String config1 = "{\n" +
                "    \"driverClassName\" : \"org.h2.Driver\",\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:db1;MODE=MySQL;INIT=" + SequenceDaoTest.INIT_SQL + "\"\n" +
                "}";
        String config2 = "{\n" +
                "    \"driverClassName\" : \"org.h2.Driver\",\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:db2;MODE=MySQL;INIT=" + SequenceDaoTest.INIT_SQL + "\"\n" +
                "}";
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/01", config1.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/02", config2.getBytes());

        String props =  "{\n" +
                "    \"sequence.format.expression\" : \"T(java.lang.String).format('%s%02d%06d', #dbDateStr, T(java.lang.Integer).valueOf(#dbName.substring(2)), #sequenceValue)\"\n" +
                "}";
        zkPropsClient.create().creatingParentsIfNeeded().forPath("/sequence", props.getBytes());

        applicationContext = new ClassPathXmlApplicationContext("classpath:routing-sequence-gen-context.xml");
    }

    @Test
    public void testSimpleRoutingSequence() throws Exception {
        ProductService productService = applicationContext.getBean(ProductService.class);
        assertThat( productService.saveProduct(new Product(1L)).substring(8), is("02000001"));
        assertThat( productService.saveProduct(new Product(2L)).substring(8), is("01000001"));
        assertThat( productService.saveProduct(new Product(3L)).substring(8), is("02000002"));
        assertThat( productService.saveProduct(new Product(4L)).substring(8), is("01000002"));
    }

}