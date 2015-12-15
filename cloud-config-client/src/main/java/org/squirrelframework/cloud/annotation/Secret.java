package org.squirrelframework.cloud.annotation;

import java.lang.annotation.*;

/**
 * Created by kailianghe on 15/12/14.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Secret {
}
