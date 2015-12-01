package org.squirrelframework.cloud.spring;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.squirrelframework.cloud.BaseTestClass;

/**
 * Created by kailianghe on 11/9/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
abstract public class SpringBaseTestClass extends BaseTestClass {
    @Autowired
    protected ApplicationContext applicationContext;
}
