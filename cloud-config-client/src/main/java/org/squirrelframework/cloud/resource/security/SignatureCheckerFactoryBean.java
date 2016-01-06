package org.squirrelframework.cloud.resource.security;

import java.security.Key;

/**
 * Created by kailianghe on 16/1/6.
 */
public class SignatureCheckerFactoryBean extends AbstractCoderFactoryBean<SignatureChecker> {
    @Override
    protected SignatureChecker createCoder(Key key) {
        if(config instanceof RSAPublicKeyConfig) {
            return new RSASignatureChecker(key);
        } else if(config instanceof Md5SignatureConfig) {
            return new Md5SignatureChecker( ((Md5SignatureConfig)config).getSecretKey() );
        }
        throw new UnsupportedOperationException("Unsupported config type, "+config.toString());
    }

    @Override
    public Class<?> getObjectType() {
        return SignatureChecker.class;
    }
}
