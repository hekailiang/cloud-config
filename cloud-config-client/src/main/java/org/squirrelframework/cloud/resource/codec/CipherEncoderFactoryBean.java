package org.squirrelframework.cloud.resource.codec;

import java.security.Key;

/**
 * Created by kailianghe on 15/12/21.
 */
public class CipherEncoderFactoryBean extends AbstractCipherCoderFactoryBean<Encoder> {
    @Override
    protected Encoder createCoder(Key key) {
        return new CipherEncoder(key);
    }

    @Override
    public Class<?> getObjectType() {
        return Encoder.class;
    }
}
