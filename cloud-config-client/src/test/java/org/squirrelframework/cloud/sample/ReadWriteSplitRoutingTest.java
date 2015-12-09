package org.squirrelframework.cloud.sample;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.squirrelframework.cloud.BaseTestClass;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by kailianghe on 15/12/8.
 */
public class ReadWriteSplitRoutingTest extends BaseTestClass {

    ApplicationContext applicationContext;

    static final String INIT = ";INIT=CREATE TABLE IF NOT EXISTS USER (" +
                "ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "USERNAME VARCHAR (32) NOT NULL," +
                "NAME VARCHAR (64) NOT NULL," +
                "UNIQUE (USERNAME)" +
            ");\"\n";

    @Override
    protected void prepare() throws Exception {
        String common = "{\n" +
                "    \"driverClassName\" : \"org.h2.Driver\"\n" +
                "}";

        String aDev = "{\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:a-dev;MODE=MySQL;DB_CLOSE_DELAY=-1"+INIT+
                "}";

        String aProdW = "{\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:a-prod-w;MODE=MySQL;DB_CLOSE_DELAY=-1"+INIT+
                "}";

        String aProdR1 = "{\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:a-prod-r-1;MODE=MySQL;DB_CLOSE_DELAY=-1"+INIT+
                "}";

        String aProdR2 = "{\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:a-prod-r-2;MODE=MySQL;DB_CLOSE_DELAY=-1"+INIT+
                "}";

        String aProdR3 = "{\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:a-prod-r-3;MODE=MySQL;DB_CLOSE_DELAY=-1"+INIT+
                "}";

        String cDev = "{\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:c-dev;MODE=MySQL;DB_CLOSE_DELAY=-1"+INIT+
                "}";

        String cProd = "{\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:c-prod;MODE=MySQL;DB_CLOSE_DELAY=-1"+INIT+
                "}";

        String uc = "{\n" +
                "    \"driverClassName\" : \"org.h2.Driver\",\n" +
                "    \"jdbcUrl\" : \"jdbc:h2:mem:unknown;MODE=MySQL;DB_CLOSE_DELAY=-1"+INIT+
                "}";

        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/a", common.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/a/dev", aDev.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/a/prod/write", aProdW.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/a/prod/read-1", aProdR1.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/a/prod/read-2", aProdR2.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/a/prod/read-3", aProdR3.getBytes());

        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/c", common.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/c/dev", cDev.getBytes());
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/c/prod", cProd.getBytes());

        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb/unknown", uc.getBytes());

        applicationContext = new ClassPathXmlApplicationContext("classpath:rw-split-routing-ds-context.xml");
    }

    @Test
    public void testReadWriteSplit() {
        UserService userService = applicationContext.getBean(UserService.class);
        List<User> users = userService.findAllUsers();
        assertThat(users.size(), is(0));

        User user = new User();
        user.setUsername("hekailiang");
        user.setName("Henry He");
        userService.insertUser(user);

        users = userService.findAllUsers();
        assertThat(users.size(), is(0));

        users = userService.findAllUsersAsWrite();
        assertThat(users.size(), is(1));
    }
}
