package org.squirrelframework.cloud.resource.security.md5;

import org.squirrelframework.cloud.resource.codec.CoderConfig;

/**
 * Created by kailianghe on 16/1/6.
 */
public class Md5SignatureConfig extends CoderConfig {

    private String secretKey;

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
