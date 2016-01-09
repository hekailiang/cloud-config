package org.squirrelframework.cloud.resource.security;

import org.squirrelframework.cloud.resource.codec.Encoder;

import java.security.Key;

/**
 * Created by kailianghe on 15/12/21.
 */
public class CipherEncoderFactoryBean extends AbstractCoderFactoryBean<Encoder> {
    @Override
    protected Encoder createCoder(Key key) {
        return new CipherEncoder(key);
    }

    @Override
    public Class<?> getObjectType() {
        return CipherEncoder.class;
    }
}
