package org.squirrelframework.cloud.routing;

import com.google.common.base.Optional;

/**
 * Created by kailianghe on 15/12/9.
 */
public class DeclarativeRoutingKeyResolver implements RoutingKeyResolver {

    @Override
    public Optional<String> get() {
        return Optional.of(RoutingKeyHolder.getDeclarativeRoutingKey());
    }
}