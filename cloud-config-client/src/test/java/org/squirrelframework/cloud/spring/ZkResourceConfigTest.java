package org.squirrelframework.cloud.spring;

import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.squirrelframework.cloud.BaseTestClass;
import org.squirrelframework.cloud.resource.CloudResourceConfig;
import org.squirrelframework.cloud.resource.TenantAwareResourceConfig;

import javax.validation.constraints.Min;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by kailianghe on 11/10/15.
 */
public class ZkResourceConfigTest extends BaseTestClass {

    ApplicationContext applicationContext;

    protected void prepareSimpleConfig() throws Exception {
        String sampleConfig = "{\"name\": \"hhe\", \"age\": 25, \"address\": \"shanghai\", \"attributes\":{\"id\":1}}";
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/resource/cnf", sampleConfig.getBytes());
        applicationContext = new ClassPathXmlApplicationContext("classpath:zkresource-context.xml");
    }

    protected void prepareErrorConfig() throws Exception {
        String sampleConfig = "{\"name\": \"little\", \"age\": 8, \"address\": \"shanghai\", \"attributes\":{\"id\":3}}";
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/resource/cnf", sampleConfig.getBytes());
        applicationContext = new ClassPathXmlApplicationContext("classpath:zkresource-context.xml");
    }

    protected void prepareTenantAwareConfig() throws Exception {
        String sampleConfigA = "{\"name\": \"hhe\", \"age\": 25, \"address\": \"shanghai\", \"attributes\":{\"id\":1}}";
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/resource/cnf/a", sampleConfigA.getBytes());

        String sampleConfigB = "{\"name\": \"cm\", \"age\": 20, \"address\": \"beijing\", \"attributes\":{\"id\":2}}";
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/resource/cnf/b", sampleConfigB.getBytes());
        applicationContext = new ClassPathXmlApplicationContext("classpath:zkresource2-context.xml");
    }

    @Test
    public void testSimpleResourceConfigInjection() throws Exception {
        prepareSimpleConfig();
        SampleResourceConfig sampleResourceConfig =
                applicationContext.getBean("sampleResourceConfig", SampleResourceConfig.class);
        assertThat(sampleResourceConfig, notNullValue());
        assertThat(sampleResourceConfig.name, is("hhe"));
        assertThat(sampleResourceConfig.address, is("shanghai"));
        assertThat(sampleResourceConfig.age, is(25));
        assertThat((Integer)sampleResourceConfig.getAttribute("id"), is(1));
    }

    @Test(expected = BeanCreationException.class)
    public void testSimpleResourceConfigInjectionWithError() throws Exception {
        prepareErrorConfig();
        applicationContext.getBean("sampleResourceConfig", SampleResourceConfig.class);
    }

    @Test
    public void testTenantAwareResourceConfigInjection() throws Exception {
        prepareTenantAwareConfig();
        TenantAwareResourceConfig tenantAwareResourceConfig =
                applicationContext.getBean("tenantAwareResourceConfig", TenantAwareResourceConfig.class);
        assertThat(tenantAwareResourceConfig, notNullValue());
        assertThat(tenantAwareResourceConfig.size(), is(2));

        SampleResourceConfig configA = (SampleResourceConfig)tenantAwareResourceConfig.get("a");
        assertThat(configA.name, is("hhe"));
        assertThat(configA.address, is("shanghai"));
        assertThat(configA.age, is(25));
        assertThat((Integer)configA.getAttribute("id"), is(1));

        SampleResourceConfig configB = (SampleResourceConfig)tenantAwareResourceConfig.get("b");
        assertThat(configB.name, is("cm"));
        assertThat(configB.address, is("beijing"));
        assertThat(configB.age, is(20));
        assertThat((Integer)configB.getAttribute("id"), is(2));
    }

    public static class SampleResourceConfig extends CloudResourceConfig {
        String name;
        String address;
        @Min(18)
        int age;
        public void setName(String name) {
            this.name = name;
        }
        public void setAddress(String address) {
            this.address = address;
        }
        public void setAge(int age) {
            this.age = age;
        }
    }
}
