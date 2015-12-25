package org.squirrelframework.cloud.routing;

import com.google.common.cache.CacheLoader;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by kailianghe on 15/12/25.
 */
public class ConsistentHashRoutingKeyResolver extends DispatchableRoutingResolver {

    private DeclarativeRoutingKeyResolver declarativeRoutingKeyResolver;

    @Override
    protected CacheLoader<String, Iterator<String>> getCacheLoader() {
        return new CacheLoader<String, Iterator<String>>() {
            @Override
            public Iterator<String> load(String routingPath) throws Exception {
                List<String> nodes = client.getChildren().forPath(routingPath);
                final ConsistentHash<String> consistentHash = new ConsistentHash<>(nodes);
                return new Iterator<String>() {
                    @Override
                    public boolean hasNext() {
                        return true;
                    }

                    @Override
                    public String next() {
                        String routingParam = declarativeRoutingKeyResolver.get().get();
                        return consistentHash.get(routingParam);
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public void setDeclarativeRoutingKeyResolver(DeclarativeRoutingKeyResolver declarativeRoutingKeyResolver) {
        this.declarativeRoutingKeyResolver = declarativeRoutingKeyResolver;
    }

    static class ConsistentHash<T> {

        // virtual nodes circle
        private final TreeMap<Long, T> circle = new TreeMap<>();

        // virtual nodes number per real nodes
        private final Integer virtual;

        private final MessageDigest md5Algorithm;

        public ConsistentHash(List<T> nodes) {
            this(400, nodes);
        }

        public ConsistentHash(int virtual, List<T> nodes) {
            if (nodes == null || nodes.isEmpty()) {
                throw new IllegalArgumentException("nodes cannot be null or empty");
            }
            this.virtual = virtual;
            try {
                md5Algorithm = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("MD5 isn't available.");
            }
            for (T node : nodes) {
                addNode(node);
            }
        }

        protected void addNode(T node) {
            for (int i = 0; i < virtual / 4; i++) {
                byte[] digest = md5Hash(node.toString() + i);
                for (int h = 0; h < 4; h++) {
                    circle.put(hash(digest, h), node);
                }
            }
        }

        public T get(Object key) {
            if (circle.isEmpty()) {
                return null;
            }
            long hash = hash(key.toString());
            if (!circle.containsKey(hash)) {
                SortedMap<Long, T> tailMap = circle.tailMap(hash);
                hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
            }
            return circle.get(hash);
        }

        protected void removeNode(T node) {
            for (int i = 0; i < virtual / 4; i++) {
                byte[] digest = md5Hash(node.toString() + i);
                for (int h = 0; h < 4; h++) {
                    circle.remove(hash(digest, h));
                }
            }
        }

        private long hash(final String k) {
            byte[] digest = md5Hash(k);
            return hash(digest, 0) & 0xffffffffL;
        }

        private long hash(byte[] digest, int h) {
            return  ((long) (digest[3 + h * 4] & 0xFF) << 24) |
                    ((long) (digest[2 + h * 4] & 0xFF) << 16) |
                    ((long) (digest[1 + h * 4] & 0xFF) << 8) |
                    (digest[h * 4] & 0xFF);
        }

        private byte[] md5Hash(String text) {
            md5Algorithm.update(text.getBytes());
            return md5Algorithm.digest();
        }
    }

}
