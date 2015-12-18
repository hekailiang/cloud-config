package org.squirrelframework.cloud.routing;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * Created by kailianghe on 15/12/9.
 */
public class DeclarativeRoutingKeyResolver implements RoutingKeyResolver {

    private boolean rollingPoll = false;

    @Override
    public Optional<String> get() {
        String routingKey = rollingPoll ? RoutingKeyHolder.rollingPollDeclarativeRoutingKey() :
                RoutingKeyHolder.pollDeclarativeRoutingKey();
        Preconditions.checkNotNull(routingKey, "cannot find any routing key in current context");
        return Optional.of(routingKey);
    }

    public void setRollingPoll(boolean rollingPoll) {
        this.rollingPoll = rollingPoll;
    }
}