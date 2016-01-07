package org.squirrelframework.cloud.resource;

import org.junit.Test;
import org.squirrelframework.cloud.BaseTestClass;
import org.squirrelframework.cloud.resource.tenant.TenantConfig;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by kailianghe on 11/9/15.
 */
public class RoutingAwareResourceConfigFactoryBeanTest extends BaseTestClass {

    @Override
    protected void prepare() throws Exception {
        String axatpConfig = "{\n" +
                "  \"id\":1,\n" +
                "  \"code\":\"axatp\",\n" +
                "  \"domain\":\"axatp.dev.ebaocloud.com.cn\",\n" +
                "  \"country\":\"cn\",\n" +
                "  \"searchEngineCollection\":\"axatp.dev\",\n" +
                "  \"company\":\"AXATP\",\n" +
                "  \"name\":\"AXATP\",\n" +
                "  \"localDomain\":\"axatp.localhost\"\n" +
                "}";
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/tenants/axatp", axatpConfig.getBytes());

        String ebaoConfig = "{\n" +
                "  \"id\":2,\n" +
                "  \"code\":\"ebao\",\n" +
                "  \"domain\":\"ebao.dev.ebaocloud.com.cn\",\n" +
                "  \"country\":\"cn\",\n" +
                "  \"searchEngineCollection\":\"axatp.dev\",\n" +
                "  \"company\":\"eBaotech\",\n" +
                "  \"name\":\"eBaotech\",\n" +
                "  \"localDomain\":\"ebao.localhost\"\n" +
                "}";
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/tenants/ebao", ebaoConfig.getBytes());
    }

    private RoutingAwareResourceConfig createBean(String path) throws Exception {
        RoutingAwareResourceConfigFactoryBean factoryBean = new RoutingAwareResourceConfigFactoryBean();
        factoryBean.setClient(zkRootClient);
        factoryBean.setPath(path);
        factoryBean.setResourceType(TenantConfig.class);
        factoryBean.setAutoReload(true);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        factoryBean.setValidator(validator);

        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    private RoutingAwareResourceConfig createBean() throws Exception {
        return createBean("/tenants");
    }

    @Test
    public void testCreateTenantAwareConfig() throws Exception {
        RoutingAwareResourceConfig result = createBean();
        assertThat(result.size(), is(2));
        assertThat(result.get("axatp"), notNullValue());
        assertThat(result.get("axatp"), is(TenantConfig.class));
        assertThat(((TenantConfig)result.get("axatp")).getDomain(), is("axatp.dev.ebaocloud.com.cn"));

        assertThat(result.get("ebao"), notNullValue());
        assertThat(result.get("ebao"), is(TenantConfig.class));
        assertThat(((TenantConfig) result.get("ebao")).getDomain(), is("ebao.dev.ebaocloud.com.cn"));
    }

    @Test(timeout = 10000L)
    public void testUpdateTenantAwareConfig() throws Exception {
        RoutingAwareResourceConfig result = createBean();
        final AtomicBoolean reloadInvoked = new AtomicBoolean(false);
        final CountDownLatch latch = new CountDownLatch(1);
        ((TenantConfig) result.get("ebao")).setReloadCallback(new ReloadCallback() {
            @Override
            public void reload() throws Exception {
                reloadInvoked.set(true);
                latch.countDown();
            }
        });

        String newEbaoConfig = "{\n" +
                "  \"id\":2,\n" +
                "  \"code\":\"ebao\",\n" +
                "  \"domain\":\"ebao2.ebaocloud.com.cn\",\n" +
                "  \"country\":\"cn\",\n" +
                "  \"searchEngineCollection\":\"axatp.dev\",\n" +
                "  \"company\":\"eBaotech\",\n" +
                "  \"name\":\"eBaotech\",\n" +
                "  \"localDomain\":\"ebao2.localhost\"\n" +
                "}";
        zkConfigClient.setData().forPath("/tenants/ebao", newEbaoConfig.getBytes());

        latch.await();

        assertThat(reloadInvoked.get(), is(Boolean.TRUE));
        assertThat(((TenantConfig) result.get("ebao")).getDomain(), is("ebao2.ebaocloud.com.cn"));
    }

    @Test
    public void testAddNewTenantAwareConfig() throws Exception {
        RoutingAwareResourceConfig result = createBean();
        assertThat(result.size(), is(2));

        String ciccConfig = "{\n" +
                "  \"id\":3,\n" +
                "  \"code\":\"cicc\",\n" +
                "  \"domain\":\"cicc.dev.ebaocloud.com.cn\",\n" +
                "  \"country\":\"cn\",\n" +
                "  \"searchEngineCollection\":\"cicc.dev\",\n" +
                "  \"company\":\"CICC\",\n" +
                "  \"name\":\"CICC\",\n" +
                "  \"localDomain\":\"cicc.localhost\"\n" +
                "}";
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/tenants/cicc", ciccConfig.getBytes());

        Thread.sleep(100);
        assertThat(result.size(), is(3));
        assertThat(result.get("cicc"), notNullValue());
        assertThat(result.get("cicc"), is(TenantConfig.class));
        assertThat(((TenantConfig)result.get("cicc")).getDomain(), is("cicc.dev.ebaocloud.com.cn"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidationFailure() throws Exception {
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/tenantsFailure", "".getBytes());
        createBean("tenantsFailure");
    }
}
