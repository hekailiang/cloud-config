package org.squirrelframework.cloud.resource.security;

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
