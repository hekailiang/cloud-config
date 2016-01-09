package org.squirrelframework.cloud.resource.security.rsa;

import org.squirrelframework.cloud.resource.codec.CoderConfig;

/**
 * Created by kailianghe on 16/1/6.
 */
public class RSAPublicKeyConfig extends CoderConfig {

    private String publicKey;

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
