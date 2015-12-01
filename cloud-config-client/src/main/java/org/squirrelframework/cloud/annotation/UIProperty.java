package org.squirrelframework.cloud.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface UIProperty {
    String label();
    String placeholder() default "";
    String defaultValue() default "";
    int order() default 0;
}
