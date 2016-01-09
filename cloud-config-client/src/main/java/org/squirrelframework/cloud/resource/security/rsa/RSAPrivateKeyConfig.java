package org.squirrelframework.cloud.resource.security.rsa;

import org.squirrelframework.cloud.resource.codec.CoderConfig;

/**
 * Created by kailianghe on 16/1/6.
 */
public class RSAPrivateKeyConfig extends CoderConfig {
    private String privateKey;

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
