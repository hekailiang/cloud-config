package org.squirrelframework.cloud.resource.sequence;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.squirrelframework.cloud.BaseTestClass;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by kailianghe on 15/12/16.
 */
public class SimpleSequenceGeneratorTest extends BaseTestClass {

    ApplicationContext applicationContext;

    protected void prepare() throws Exception {
        String config = "{\n" +
                "    \"driverClassName\" : \"org.h2.Driver\",\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:dev;MODE=MySQL;INIT="+SequenceDaoTest.INIT_SQL+"\"\n"+
                "}";
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb", config.getBytes());
        applicationContext = new ClassPathXmlApplicationContext("classpath:sequence-gen-context.xml");
    }

    @Test
    public void testSequenceDao() throws Exception {
        SequenceGenerator sequenceGenerator = applicationContext.getBean("sequence", SequenceGenerator.class);
        assertThat(sequenceGenerator, notNullValue());
        assertThat(sequenceGenerator.next("test1"), is("1"));
        assertThat(sequenceGenerator.next("test1"), is("2"));
        assertThat(sequenceGenerator.next("test2"), is("1"));
    }

}
