package org.squirrelframework.cloud.resource.sequence;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.squirrelframework.cloud.BaseTestClass;
import org.squirrelframework.cloud.utils.BeanIdGenerator;

import javax.sql.DataSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Created by kailianghe on 15/12/25.
 */
public class ConsistentHashRoutingTest extends BaseTestClass {

    ApplicationContext applicationContext;

    public static final String INIT_SQL =
            "CREATE TABLE IF NOT EXISTS PRODUCT ( id varchar(16) NOT NULL, customer_id INT NOT NULL, name varchar(20) DEFAULT '', PRIMARY KEY (id) );";

    public static final String SELECT_PRODUCT = "SELECT * FROM PRODUCT";

    protected void prepare() throws Exception {
        String config1 = "{\n" +
                "    \"driverClassName\" : \"org.h2.Driver\",\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:db1;MODE=MySQL;INIT=" + INIT_SQL + "\"\n" +
                "}";
        String config2 = "{\n" +
                "    \"driverClassName\" : \"org.h2.Driver\",\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:db2;MODE=MySQL;INIT=" + INIT_SQL + "\"\n" +
                "}";
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/a/01", config1.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/a/02", config2.getBytes());

        applicationContext = new ClassPathXmlApplicationContext("classpath:consistent-hash-routing.xml");
    }

    @Test
    public void testConsistentHashRouting() throws Exception {
        ContainerService2 productService = applicationContext.getBean(ContainerService2.class);
        // routing to db01
        Product p1 = new Product(1L, "P1");
        productService.saveProduct(p1);
        assertThat( p1.getId(), notNullValue() );

        Product p11 = productService.findProductById(p1.getId(), p1.getCustomerId());
        assertThat( p11, notNullValue() );

        DataSource ds01 = applicationContext.getBean(BeanIdGenerator.getDataSourceBeanId("/database/mydb/a/01"), DataSource.class);
        JdbcTemplate jDb01 = new JdbcTemplate(ds01);
        Object id1 = jDb01.queryForList(SELECT_PRODUCT).get(0).get("ID");
        assertThat( p1.getId(), is(id1) );

        // routing to db02
        Product p2 = new Product(112L, "P2");
        productService.saveProduct(p2);
        assertThat( p2.getId(), notNullValue() );

        Product p22 = productService.findProductById(p2.getId(), p2.getCustomerId());
        assertThat( p22, notNullValue() );

        p2.setName("P22");
        productService.saveProduct(p2);
        p22 = productService.findProductById(p2.getId(), p2.getCustomerId());
        assertThat( p22.getName(), is("P22") );

        DataSource ds02 = applicationContext.getBean(BeanIdGenerator.getDataSourceBeanId("/database/mydb/a/02"), DataSource.class);
        JdbcTemplate jDb02 = new JdbcTemplate(ds02);
        Object id2 = jDb02.queryForList(SELECT_PRODUCT).get(0).get("ID");
        assertThat( p2.getId(), is(id2) );

    }
}
