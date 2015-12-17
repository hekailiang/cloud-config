package org.squirrelframework.cloud.routing;

import com.google.common.base.Optional;

/**
 * Created by kailianghe on 15/12/9.
 */
public class DeclarativeRoutingKeyResolver implements RoutingKeyResolver {

    private boolean rollingPoll = false;

    @Override
    public Optional<String> get() {
        String routingKey = rollingPoll ? RoutingKeyHolder.rollingPollDeclarativeRoutingKey() :
                RoutingKeyHolder.pollDeclarativeRoutingKey();
        return Optional.of(routingKey);
    }

    public void setRollingPoll(boolean rollingPoll) {
        this.rollingPoll = rollingPoll;
    }
}