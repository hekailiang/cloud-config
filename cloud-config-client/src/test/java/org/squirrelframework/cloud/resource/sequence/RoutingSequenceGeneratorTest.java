package org.squirrelframework.cloud.resource.sequence;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.squirrelframework.cloud.BaseTestClass;
import org.squirrelframework.cloud.utils.BeanIdGenerator;

import javax.sql.DataSource;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by kailianghe on 15/12/17.
 */
public class RoutingSequenceGeneratorTest extends BaseTestClass {

    ApplicationContext applicationContext;

    public static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS PRODUCT ( id varchar(16) NOT NULL, customer_id INT NOT NULL, name varchar(20) DEFAULT '', PRIMARY KEY (id) );";

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
                "    \"sequence.format.expression\" : \"T(java.lang.String).format('%s%02d%06d', #dbDateStr, T(java.lang.Integer).valueOf(#dbName.substring(2)), #sequenceValue)\",\n" +
                "    \"sequence.product.id.sharding.rule\": \"#id.substring(8, 10)\",\n" +
                "    \"sequence.product.sharding.rule\": \"#product?.id?.subString(8, 10) ?: T(java.lang.String).format('%02d', #product.customerId%2+1)\"\n" +
                "}";
        zkPropsClient.create().creatingParentsIfNeeded().forPath("/sequence", props.getBytes());

        applicationContext = new ClassPathXmlApplicationContext("classpath:routing-sequence-gen-context.xml");

        DataSource ds01 = applicationContext.getBean(BeanIdGenerator.getDataSourceBeanId("/database/mydb/01"), DataSource.class);
        DataSource ds02 = applicationContext.getBean(BeanIdGenerator.getDataSourceBeanId("/database/mydb/02"), DataSource.class);
        new JdbcTemplate(ds01).execute(CREATE_TABLE);
        new JdbcTemplate(ds02).execute(CREATE_TABLE);
    }

    @Test
    public void testSimpleRoutingSequence() throws Exception {
        ProductService productService = applicationContext.getBean(ProductService.class);
        Product p1 = new Product(1L, "P1");
        productService.saveProduct(p1);
        assertThat( p1.getId().substring(8), is("02000001") );

        Product p2 = new Product(2L, "P2");
        productService.saveProduct(p2);
        assertThat( p2.getId().substring(8), is("01000001") );

        Product p3 = new Product(3L, "P3");
        productService.saveProduct(p3);
        assertThat( p3.getId().substring(8), is("02000002") );

        Product p4 = new Product(4L, "P4");
        productService.saveProduct(p4);
        assertThat( p4.getId().substring(8), is("01000002") );

        Product p11 = productService.findProductById(p1.getId());
        assertThat(p1, equalTo(p11));

        Product p12 = productService.findProductById(p2.getId());
        assertThat(p2, equalTo(p12));

        Product p13 = productService.findProductById(p3.getId());
        assertThat(p3, equalTo(p13));

        Product p14 = productService.findProductById(p4.getId());
        assertThat(p4, equalTo(p14));
    }

}