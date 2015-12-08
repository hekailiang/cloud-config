package org.squirrelframework.cloud.routing;

import com.google.common.base.Optional;

import java.util.Collections;
import java.util.List;

/**
 * Created by kailianghe on 12/4/15.
 */
public class FlattenRoutingKeyResolver implements RoutingKeyResolver {

    private List<RoutingKeyResolver> resolvers = Collections.emptyList();

    private String separator = "-";

    @Override
    public Optional<String> get() {
        StringBuilder builder = new StringBuilder();
        for(RoutingKeyResolver resolver : resolvers) {
            if(builder.length()>0) {
                builder.append(separator);
            }
            Optional<String> value = resolver.get();
            if(value.isPresent()) {
                builder.append(value.get());
            } else {
                break;
            }
        }
        return Optional.of(builder.toString());
    }

    public void setResolvers(List<RoutingKeyResolver> resolvers) {
        this.resolvers = resolvers;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }
}
