package org.squirrelframework.cloud;

import com.google.common.base.Optional;
import org.squirrelframework.cloud.routing.RoutingKeyResolver;

/**
 * Created by kailianghe on 15/12/8.
 */
public class TestRoutingKeyResolver implements RoutingKeyResolver {
    int invokeTimes=0;
    String[] routingKeys = {"a", "b"};

    @Override
    public Optional<String> get() {
        return Optional.of(routingKeys[invokeTimes++%2]);
    }
}
