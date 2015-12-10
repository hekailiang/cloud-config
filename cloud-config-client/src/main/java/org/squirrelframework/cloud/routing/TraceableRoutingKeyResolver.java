package org.squirrelframework.cloud.routing;

import com.google.common.base.Optional;

/**
 * Created by kailianghe on 15/12/10.
 */
public class TraceableRoutingKeyResolver implements RoutingKeyResolver {

    private final RoutingKeyResolver delegator;

    public TraceableRoutingKeyResolver(RoutingKeyResolver delegator) {
        this.delegator = delegator;
    }

    @Override
    public Optional<String> get() {
        Optional<String> result = delegator.get();
        if (result.isPresent() && RoutingKeyHolder.isRoutingKeyTraceEnabled()) {
            RoutingKeyHolder.putRoutingKey(result.get());
        }
        return result;
    }

}
