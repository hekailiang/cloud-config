package org.squirrelframework.cloud.routing;

import com.google.common.base.Optional;

import java.util.List;

/**
 * Created by kailianghe on 15/12/8.
 */
public class NestedRoutingKeyResolver implements RoutingKeyResolver {

    private List<RoutingKeyResolver> resolvers;

    @Override
    public Optional<String> get() {
        RoutingKeyResolver resolver = new TraceableRoutingKeyResolver(resolvers.get(0));
        return resolver.get();
    }

    public RoutingKeyResolver next() {
        NestedRoutingKeyResolver subRoutingKeyResolver = new NestedRoutingKeyResolver();
        subRoutingKeyResolver.setResolvers(resolvers.subList(1, resolvers.size()));
        return subRoutingKeyResolver;
    }

    public boolean hasNext() {
        return resolvers!=null && resolvers.size()>1;
    }


    public void setResolvers(List<RoutingKeyResolver> resolvers) {
        this.resolvers = resolvers;
    }
}
