package org.squirrelframework.cloud.resource.security;

/**
 * Created by kailianghe on 16/1/6.
 */
public abstract class AbstractDecoder implements Decoder {
    @Override
    public String decode(String value) throws Exception {
        return decode(value, "UTF-8");
    }
}
