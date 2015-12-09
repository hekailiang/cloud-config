package org.squirrelframework.cloud;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by kailianghe on 11/11/15.
 */
public class SwitchDataSourceTest extends BaseTestClass {

    private String server = "127.0.0.1:3306";

    private String user = "root";

    private String password = "1111";

    public void runTest(String configFile) throws Exception {
        String normalConfig = "{\n" +
                "    \"driverClassName\" : \"com.mysql.jdbc.Driver\",\n" +
                "    \"userName\" : \""+user+"\",\n" +
                "    \"password\" : \""+password+"\", \n"+
                "    \"jdbcUrl\" : \"jdbc:mysql://"+server+"/a?characterEncoding=utf8&createDatabaseIfNotExist=true\"\n"+
                "}";

        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb", normalConfig.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/dev", "".getBytes());
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:"+configFile);

        final DataSource dataSource = applicationContext.getBean(DataSource.class);
        final AtomicReference<List<Map<String, Object>>> resultHolder = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Map<String, Object>> result = jdbcTemplate.queryForList("SELECT sleep(10);");
                    resultHolder.set(result);
                } finally {
                    latch.countDown();
                }
            }
        }).start();

        String newConfig = "{\n" +
                "    \"jdbcUrl\" : \"jdbc:mysql://"+server+"/b?characterEncoding=utf8&createDatabaseIfNotExist=true\"\n"+
                "}";
        zkConfigClient.setData().forPath("/database/mydb/dev", newConfig.getBytes());
        int iter=0, maxIter=10;
        boolean renewed = false;
        while(iter++<maxIter && !renewed) {
            List<Map<String, Object>> newResult = jdbcTemplate.queryForList("SELECT DATABASE();");
            if("b".equals(newResult.get(0).get("DATABASE()"))) {
                renewed = true;
            }
            Thread.sleep(500);
        }

        latch.await();
        List<Map<String, Object>> orgResult = resultHolder.get();
        assertThat((Long)orgResult.get(0).get("sleep(10)"), is(0L));

        System.out.println("Success!");
    }

    public static void test(String configFile) throws Throwable {
        final SwitchDataSourceTest testcase = new SwitchDataSourceTest();
        try {
            testcase.init();
            testcase.runTest(configFile);
        } catch (Exception e) {
            System.out.println("Fail!");
        } finally {
            testcase.close();
        }
    }

    public static void main(String[] args) throws Throwable {
        test("bonecp-simple-ds-context.xml");
        // cannot safe close connection pool which means all connection point to old data source will fail
//        test("c3p0-simple-ds-context.xml");
//        test("druid-simple-ds-context.xml");
    }
}
