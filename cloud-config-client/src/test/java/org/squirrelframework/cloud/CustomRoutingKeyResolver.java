package org.squirrelframework.cloud;

import com.google.common.base.Optional;
import org.squirrelframework.cloud.routing.RoutingKeyResolver;

/**
 * Created by kailianghe on 15/12/8.
 */
public class CustomRoutingKeyResolver implements RoutingKeyResolver {

    public String key;

    @Override
    public Optional<String> get() {
        return Optional.of(key);
    }

    public void setKey(String key) {
        this.key = key;
    }
}
