package org.squirrelframework.cloud.resource.security;

import org.squirrelframework.cloud.resource.codec.Decoder;

/**
 * Created by kailianghe on 16/1/6.
 */
public abstract class AbstractDecoder implements Decoder {
    @Override
    public String decode(String value) throws Exception {
        return decode(value, "UTF-8");
    }
}
