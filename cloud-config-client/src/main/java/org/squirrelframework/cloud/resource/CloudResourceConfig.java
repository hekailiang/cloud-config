package org.squirrelframework.cloud.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Map;

/**
 * Created by kailianghe on 9/6/15.
 */
@JsonIgnoreProperties({"__type__", "__name__", "__description__", "__order__", "__link__"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class CloudResourceConfig {

    private Map<String, Object> attributes;

    private transient ReloadCallback reloadCallback;

    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void setReloadCallback(ReloadCallback reloadCallback) {
        this.reloadCallback = reloadCallback;
    }

    public void reload() {
        if(reloadCallback!=null) {
            try {
                reloadCallback.reload();
            } catch (Exception e) {
                throw new RuntimeException("Reload "+getClass().getSimpleName()+" failed.", e);
            }
        }
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}