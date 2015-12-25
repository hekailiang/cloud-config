package org.squirrelframework.cloud.routing;

import com.google.common.cache.CacheLoader;

import java.util.Iterator;
import java.util.List;

/**
 * Created by kailianghe on 15/12/10.
 */
public class RoundRobinRoutingKeyResolver extends DispatchableRoutingResolver {

    @Override
    protected CacheLoader<String, Iterator<String>> getCacheLoader() {
        return new CacheLoader<String, Iterator<String>>() {
            @Override
            public Iterator<String> load(final String routingPath) throws Exception {
                List<String> candidates = client.getChildren().forPath(routingPath);
                logger.debug("Dispatchable routing candidates loaded - {}", candidates);
                return new RoundRobin<>(candidates).iterator();
            }
        };
    }

    static class RoundRobin<T> implements Iterable<T> {
        private List<T> coll;

        public RoundRobin(List<T> coll) {
            this.coll = coll;
        }

        public Iterator<T> iterator() {
            return new Iterator<T>() {
                private int index = 0;

                @Override
                public boolean hasNext() {
                    return true;
                }

                @Override
                public T next() {
                    T res = coll.get(index);
                    index = (index + 1) % coll.size();
                    return res;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
