package org.squirrelframework.cloud.routing;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.squirrelframework.cloud.annotation.RoutingKey;

/**
 * Created by kailianghe on 15/12/9.
 */
@Aspect
public class DeclarativeRoutingKeyAspect implements Ordered {
    /**
     * cut point: method annotated with @RoutingKey
     *
     * @param routingKey
     */
    @Around(value = "@annotation(routingKey)")
    public Object process(ProceedingJoinPoint jp, RoutingKey routingKey) throws Throwable {
        try {
            DeclarativeRoutingKeyHolder.putRoutingKey(routingKey.value());
            return jp.proceed();
        } finally {
            DeclarativeRoutingKeyHolder.removeRoutingKey();
        }

    }

    @Override
    public int getOrder() {
        return -1;
    }
}
