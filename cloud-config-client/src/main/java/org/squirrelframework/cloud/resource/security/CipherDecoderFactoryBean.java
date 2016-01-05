package org.squirrelframework.cloud.resource.security;

import java.security.Key;

/**
 * Created by kailianghe on 15/12/21.
 */
public class CipherDecoderFactoryBean extends AbstractCipherCoderFactoryBean<Decoder> {
    @Override
    protected Decoder createCoder(Key key) {
        return new CipherDecoder(key);
    }

    @Override
    public Class<?> getObjectType() {
        return Decoder.class;
    }
}
