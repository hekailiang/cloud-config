package org.squirrelframework.cloud.annotation;

/**
 * Created by kailianghe on 15/12/9.
 */

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface RoutingKey {
    String value();
}
