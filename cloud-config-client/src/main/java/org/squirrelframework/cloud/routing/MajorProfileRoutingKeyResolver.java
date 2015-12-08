package org.squirrelframework.cloud.routing;

import com.google.common.base.Optional;
import org.squirrelframework.cloud.utils.CloudConfigCommon;

/**
 * Created by kailianghe on 15/12/8.
 */
public class MajorProfileRoutingKeyResolver implements RoutingKeyResolver {
    @Override
    public Optional<String> get() {
        return Optional.of( CloudConfigCommon.getProfiles()[0] );
    }
}
