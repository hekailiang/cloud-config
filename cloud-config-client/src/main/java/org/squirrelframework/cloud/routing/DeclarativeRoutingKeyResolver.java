package org.squirrelframework.cloud.routing;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * Created by kailianghe on 15/12/9.
 */
public class DeclarativeRoutingKeyResolver implements RoutingKeyResolver {

    private SelectRoutingKeyMethod selectRoutingKeyMethod = SelectRoutingKeyMethod.POLL;

    @Override
    public Optional<String> get() {
        final String routingKey;
        switch (selectRoutingKeyMethod) {
            case PEEK:
                routingKey = RoutingKeyHolder.peekDeclarativeRoutingKey();
                break;
            case POLL:
                routingKey = RoutingKeyHolder.pollDeclarativeRoutingKey();
                break;
            case ROLLING_POLL:
                routingKey = RoutingKeyHolder.rollingPollDeclarativeRoutingKey();
                break;
            default:
                throw new UnsupportedOperationException("Unsupported method "+selectRoutingKeyMethod.name());
        }
        Preconditions.checkNotNull(routingKey, "cannot find any routing key in current context");
        return Optional.of(routingKey);
    }

    public void setSelectRoutingKeyMethod(SelectRoutingKeyMethod selectRoutingKeyMethod) {
        this.selectRoutingKeyMethod = selectRoutingKeyMethod;
    }
}