package org.squirrelframework.cloud.resource.security;

import org.squirrelframework.cloud.resource.security.md5.Md5SignatureConfig;
import org.squirrelframework.cloud.resource.security.md5.Md5SignatureEncoder;
import org.squirrelframework.cloud.resource.security.rsa.RSAPrivateKeyConfig;
import org.squirrelframework.cloud.resource.security.rsa.RSASignatureEncoder;

import java.security.Key;

/**
 * Created by kailianghe on 16/1/6.
 */
public class SignatureEncoderFactoryBean extends AbstractCoderFactoryBean<SignatureEncoder> {
    @Override
    protected SignatureEncoder createCoder(Key key) {
        if(config instanceof RSAPrivateKeyConfig) {
            return new RSASignatureEncoder(key);
        } else if(config instanceof Md5SignatureConfig) {
            return new Md5SignatureEncoder( ((Md5SignatureConfig)config).getSecretKey() );
        }
        throw new UnsupportedOperationException("Unsupported config type, "+config.toString());
    }

    @Override
    public Class<?> getObjectType() {
        return SignatureEncoder.class;
    }
}
