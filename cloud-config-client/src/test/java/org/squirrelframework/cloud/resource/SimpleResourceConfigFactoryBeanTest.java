package org.squirrelframework.cloud.resource;

import org.junit.Test;
import org.squirrelframework.cloud.BaseTestClass;
import org.squirrelframework.cloud.resource.database.BoneCPDataSourceConfig;
import org.squirrelframework.cloud.utils.CloudConfigCommon;

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
public class SimpleResourceConfigFactoryBeanTest extends BaseTestClass {

    @Override
    protected void prepare() throws Exception {
        String generalDatabaseConfig = "{\n" +
                "    \"__type__\" : \"org.squirrelframework.cloud.resource.database.BoneCPDataSourceConfig\",\n" +
                "    \"driverClassName\" : \"com.mysql.jdbc.Driver\",\n" +
                "    \"idleMaxAgeInMinutes\" : 240,\n" +
                "    \"idleConnectionTestPeriodInMinutes\" : 60,\n" +
                "    \"maxConnectionsPerPartition\" : 10,\n" +
                "    \"minConnectionsPerPartition\" : 1,\n" +
                "    \"partitionCount\" : 2,\n" +
                "    \"acquireIncrement\" : 5,\n" +
                "    \"statementsCacheSize\" : 100\n" +
                "}";
        zkConfigClient.create().forPath("/database", generalDatabaseConfig.getBytes());

        String bcpBaseConfig = "{\n" +
                "    \"userName\" : \"root\",\n" +
                "    \"password\" : \"1111\"\n" +
                "}";
        zkConfigClient.create().forPath("/database/bcp", bcpBaseConfig.getBytes());

        String devConfig = "{\n" +
                "    \"jdbcUrl\" : \"jdbc:mysql://127.0.0.1:3306/dev_bcp01?useUnicode=true\"\n" +
                "}";
        zkConfigClient.create().forPath("/database/bcp/dev", devConfig.getBytes());

        String prodConfig = "{\n" +
                "    \"jdbcUrl\" : \"jdbc:mysql://127.0.0.1:3306/prod_bcp01?useUnicode=true\"\n" +
                "}";
        zkConfigClient.create().forPath("/database/bcp/prod", prodConfig.getBytes());

        String hheConfig = "{\n" +
                "    \"userName\" : \"hhe\",\n" +
                "    \"password\" : \"1234\",\n" +
                "    \"jdbcUrl\" : \"jdbc:mysql://127.0.0.1:3306/hhe_bcp01?useUnicode=true\"\n" +
                "}";
        zkConfigClient.create().forPath("/database/bcp/hhe", hheConfig.getBytes());

        String errPartitionCount = "{\"partitionCount\" : 0}";
        zkConfigClient.create().forPath("/database/bcp/errPartitionCount", errPartitionCount.getBytes());
    }

    private void basicVerifyResult(BoneCPDataSourceConfig result, boolean verifyUserAndPassword) {
        assertThat(result, notNullValue());
        assertThat(result.getDriverClassName(), is("com.mysql.jdbc.Driver"));
        assertThat(result.getIdleMaxAgeInMinutes(), is(240));
        assertThat(result.getIdleConnectionTestPeriodInMinutes(), is(60));
        assertThat(result.getMaxConnectionsPerPartition(), is(10));
        assertThat(result.getMinConnectionsPerPartition(), is(1));
        assertThat(result.getPartitionCount(), is(2));
        assertThat(result.getAcquireIncrement(), is(5));
        assertThat(result.getStatementsCacheSize(), is(100));

        if(verifyUserAndPassword) {
            assertThat(result.getUserName(), is("root"));
            assertThat(result.getPassword(), is("1111"));
        }
    }

    private BoneCPDataSourceConfig createBean() throws Exception {
        SimpleResourceConfigFactoryBean<BoneCPDataSourceConfig> factoryBean = new SimpleResourceConfigFactoryBean<>();
        factoryBean.setClient(zkRootClient);
        factoryBean.setPath("/database/bcp");
        factoryBean.setResourceType(BoneCPDataSourceConfig.class);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        factoryBean.setValidator(validator);

        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    @Test
    public void testCreateDataSourceConfigWithDefaultProfile() throws Exception {
        BoneCPDataSourceConfig result = createBean();
        basicVerifyResult(result, true);
        assertThat(result.getJdbcUrl(), is("jdbc:mysql://127.0.0.1:3306/dev_bcp01?useUnicode=true"));
    }

    @Test
    public void testCreateDataSourceConfigWithProdProfile() throws Exception {
        System.setProperty(CloudConfigCommon.CONFIG_PROFILE_KEY, "prod");

        BoneCPDataSourceConfig result = createBean();
        basicVerifyResult(result, true);
        assertThat(result.getJdbcUrl(), is("jdbc:mysql://127.0.0.1:3306/prod_bcp01?useUnicode=true"));
    }

    @Test
    public void testCreateDataSourceConfigWithMultipleProfiles() throws Exception {
        System.setProperty(CloudConfigCommon.CONFIG_PROFILE_KEY, "prod, hhe");

        BoneCPDataSourceConfig result = createBean();
        basicVerifyResult(result, false);
        assertThat(result.getUserName(), is("hhe"));
        assertThat(result.getPassword(), is("1234"));
        assertThat(result.getJdbcUrl(),  is("jdbc:mysql://127.0.0.1:3306/hhe_bcp01?useUnicode=true"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDataSourceConfigWithValidationFailure() throws Exception {
        System.setProperty(CloudConfigCommon.CONFIG_PROFILE_KEY, "errPartitionCount");
        createBean();
    }

    @Test(timeout = 10000L)
    public void testCreateDataSourceConfigWithReload() throws Exception {
        BoneCPDataSourceConfig result = createBean();
        final AtomicBoolean reloadInvoked = new AtomicBoolean(false);
        final CountDownLatch latch = new CountDownLatch(1);
        result.setReloadCallback(new ReloadCallback() {
            @Override
            public void reload() throws Exception {
                reloadInvoked.set(true);
                latch.countDown();
            }
        });
        basicVerifyResult(result, true);
        assertThat(result.getJdbcUrl(), is("jdbc:mysql://127.0.0.1:3306/dev_bcp01?useUnicode=true"));

        String newDevConfig = "{\n" +
                "    \"jdbcUrl\" : \"jdbc:mysql://127.0.0.1:3306/dev_bcp02?useUnicode=true\"\n" +
                "}";
        zkConfigClient.setData().forPath("/database/bcp/dev", newDevConfig.getBytes());

        latch.await();
        assertThat(reloadInvoked.get(), is(Boolean.TRUE));
        basicVerifyResult(result, true);
        assertThat(result.getJdbcUrl(), is("jdbc:mysql://127.0.0.1:3306/dev_bcp02?useUnicode=true"));
    }

    @Test(timeout = 10000L)
    public void testCreateDataSourceConfigWithNoReload() throws Exception {
        BoneCPDataSourceConfig result = createBean();
        result.setReloadCallback(new ReloadCallback() {
            @Override
            public void reload() throws Exception {
                throw new RuntimeException("Should not be invoked.");
            }
        });
        basicVerifyResult(result, true);
        assertThat(result.getJdbcUrl(), is("jdbc:mysql://127.0.0.1:3306/dev_bcp01?useUnicode=true"));

        // change prod profile should not affect dev profile
        String newDevConfig = "{\n" +
                "    \"jdbcUrl\" : \"jdbc:mysql://127.0.0.1:3306/prod_bcp02?useUnicode=true\"\n" +
                "}";
        zkConfigClient.setData().forPath("/database/bcp/prod", newDevConfig.getBytes());

        basicVerifyResult(result, true);
        assertThat(result.getJdbcUrl(), is("jdbc:mysql://127.0.0.1:3306/dev_bcp01?useUnicode=true"));
    }
}
