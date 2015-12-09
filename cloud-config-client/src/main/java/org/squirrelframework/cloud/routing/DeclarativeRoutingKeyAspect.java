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
            DeclarativeRoutingKeyHolder.setRoutingKey(routingKey.value());
            return jp.proceed();
        } finally {
            DeclarativeRoutingKeyHolder.resetRoutingKey();
        }

    }

    /**
     * 在 目标对象（例如 service 中的方法中）使用 @DataSource 和 @Transactional
     * 注解时，默认标注 @Transactional 的切面的通知方法会优先执行，切换数据源操作将失效，通过order将
     * DataSourceAspect 切面设置为-1 , 保证 @Transactional 切面类执行前先决定数据源。
     */
    @Override
    public int getOrder() {
        return -1;
    }
}
