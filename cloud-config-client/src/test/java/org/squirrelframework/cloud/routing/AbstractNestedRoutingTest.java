package org.squirrelframework.cloud.routing;

import org.springframework.context.ApplicationContext;
import org.squirrelframework.cloud.BaseTestClass;

/**
 * Created by kailianghe on 15/12/9.
 */
public abstract class AbstractNestedRoutingTest extends BaseTestClass {

    ApplicationContext applicationContext;

    @Override
    protected void prepare() throws Exception {
        String common = "{\n" +
                "    \"driverClassName\" : \"org.h2.Driver\"\n" +
                "}";

        String a1c = "{\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:a1;MODE=MySQL;DB_CLOSE_DELAY=-1\"\n"+
                "}";

        String a2c = "{\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:a2;MODE=MySQL;DB_CLOSE_DELAY=-1\"\n"+
                "}";

        String b1c = "{\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:b1;MODE=MySQL;DB_CLOSE_DELAY=-1\"\n"+
                "}";

        String b2c = "{\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:b2;MODE=MySQL;DB_CLOSE_DELAY=-1\"\n"+
                "}";

        String cc = "{\n" +
                "    \"driverClassName\" : \"org.h2.Driver\",\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:c;MODE=MySQL;DB_CLOSE_DELAY=-1\"\n"+
                "}";

        String uc = "{\n" +
                "    \"driverClassName\" : \"org.h2.Driver\",\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:unknown;MODE=MySQL;DB_CLOSE_DELAY=-1\"\n"+
                "}";

        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/a", common.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/b", common.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/a/1", a1c.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/a/2", a2c.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/b/1", b1c.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/b/2", b2c.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/c",   cc.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/unknown", uc.getBytes());
    }
}
