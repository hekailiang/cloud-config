package org.squirrelframework.cloud.annotation;

import java.lang.annotation.*;

/**
 * Created by kailianghe on 15/12/12.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface RoutingVariable {
    String value();
}
