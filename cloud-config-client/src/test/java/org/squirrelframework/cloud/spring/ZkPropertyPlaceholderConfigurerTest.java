package org.squirrelframework.cloud.spring;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.squirrelframework.cloud.BaseTestClass;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by kailianghe on 11/9/15.
 */
public class ZkPropertyPlaceholderConfigurerTest extends BaseTestClass {

    ApplicationContext applicationContext;

    @Override
    protected void prepare() throws Exception {
        String mailConfig = "{\"mail.host\": \"smtp.sina.com\", \"mail.port\": 25}";
        zkPropsClient.create().creatingParentsIfNeeded().forPath("/mail", mailConfig.getBytes());

        String queryConfig = "{\"query.host\": \"query.yahoo.com\", \"query.port\": 2331}";
        zkPropsClient.create().creatingParentsIfNeeded().forPath("/query", queryConfig.getBytes());
    }

    @Test
    public void testPropertyValueInjection() {
        applicationContext = new ClassPathXmlApplicationContext("classpath:zkprops-context.xml");
        SampleBean mailBean = applicationContext.getBean("mailBean", SampleBean.class);
        assertThat(mailBean.getHost(), equalTo("smtp.sina.com"));
        assertThat(mailBean.getPort(), equalTo(25));
    }

    @Test
    public void testPropertyValueWithLocalProperties() {
        applicationContext = new ClassPathXmlApplicationContext("classpath:zkprops-context.xml");
        SampleBean queryBean = applicationContext.getBean("queryBean", SampleBean.class);
        assertThat(queryBean.getHost(), equalTo("query.yahoo.com"));
        assertThat(queryBean.getPort(), equalTo(4321));
    }

    @Test
    public void testMultipleRemotePropertyPath() {
        applicationContext = new ClassPathXmlApplicationContext("classpath:zkprops2-context.xml");
        SampleBean mailBean = applicationContext.getBean("mailBean", SampleBean.class);
        assertThat(mailBean.getHost(), equalTo("smtp.sina.com"));
        assertThat(mailBean.getPort(), equalTo(25));

        SampleBean queryBean = applicationContext.getBean("queryBean", SampleBean.class);
        assertThat(queryBean.getHost(), equalTo("query.yahoo.com"));
        assertThat(queryBean.getPort(), equalTo(4321));
    }

    public static class SampleBean {
        private String host;
        private int port;
        public String getHost() {
            return host;
        }
        public void setHost(String host) {
            this.host = host;
        }
        public int getPort() {
            return port;
        }
        public void setPort(int port) {
            this.port = port;
        }
    }
}
