package org.squirrelframework.cloud.routing;

import com.google.common.base.Optional;

/**
 * Created by kailianghe on 9/8/15.
 */
public interface RoutingKeyResolver {
    Optional<String> get();
}
