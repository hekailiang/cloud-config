package org.squirrelframework.cloud.resource;

import org.hibernate.validator.constraints.NotEmpty;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Created by kailianghe on 9/10/15.
 */
public class RoutingAwareResourceConfig extends CloudResourceConfig implements Map<String, Object>, RoutingSupport<Object> {

    @NotEmpty
    private Map<String, Object> configHolder;

    public RoutingAwareResourceConfig(Map<String, Object> configHolder) {
        this.configHolder = Collections.unmodifiableMap(configHolder);
    }

    @Override
    public Object get(String tenantId) {
        return configHolder.get(tenantId);
    }

    public <T> T getResourceConfig(String tenantId) {
        return (T) get(tenantId);
    }

    @Override
    public int size() {
        return configHolder.size();
    }

    @Override
    public boolean isEmpty() {
        return configHolder.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return configHolder.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return configHolder.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return configHolder.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet() {
        return configHolder.keySet();
    }

    @Override
    public Collection<Object> values() {
        return configHolder.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return configHolder.entrySet();
    }

}
