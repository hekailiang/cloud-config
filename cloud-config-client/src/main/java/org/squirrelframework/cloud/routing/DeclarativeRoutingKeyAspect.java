package org.squirrelframework.cloud.routing;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.squirrelframework.cloud.annotation.RoutingKey;

import static org.squirrelframework.cloud.routing.RoutingKeyHolder.*;

/**
 * Created by kailianghe on 15/12/9.
 */
@Aspect
public class DeclarativeRoutingKeyAspect implements Ordered {

    @Around(value = "@annotation(routingKey)")
    public Object process(ProceedingJoinPoint jp, RoutingKey routingKey) throws Throwable {
        boolean newEntryFlag = isNewEntry();
        try {
            if(newEntryFlag) {
                setNewEntry(false);
                if(routingKey.recordRoutingKeys()) {
                    setRoutingKeyTraceEnabled(true);
                }
            }
            putDeclarativeRoutingKey(routingKey.value());
            return jp.proceed();
        } finally {
            if(newEntryFlag) {
                removeNewEntry();
                if(routingKey.recordRoutingKeys()) {
                    removeRoutingKeyTraceEnabled();
                    removeRoutingKeys();
                }
                removeDeclarativeRoutingKeys();
            }
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
