package org.squirrelframework.cloud.resource;

import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by kailianghe on 11/9/15.
 */
public class CompositeRoutingKeyResolver implements RoutingKeyResolver {

    private static final Logger logger = LoggerFactory.getLogger(CompositeRoutingKeyResolver.class);

    private List<RoutingKeyResolver> resolvers;

    @Override
    public Optional<String> get() {
        Optional<String> result = Optional.absent();
        if(resolvers!=null && resolvers.size()>0) {
            for(RoutingKeyResolver resolver : resolvers) {
                result = resolver.get();
                if(result.isPresent()) break;
            }
        }
        if(!result.isPresent()) {
            logger.warn("Routing key is not resolved.");
        }
        return result;
    }

    public void setResolvers(List<RoutingKeyResolver> resolvers) {
        this.resolvers = resolvers;
    }
}