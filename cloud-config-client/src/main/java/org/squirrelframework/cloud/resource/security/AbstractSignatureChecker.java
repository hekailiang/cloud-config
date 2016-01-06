package org.squirrelframework.cloud.resource.security;

/**
 * Created by kailianghe on 16/1/6.
 */
public abstract class AbstractSignatureChecker implements SignatureChecker {

    @Override
    public boolean verify(String data, String sign) throws Exception {
        return verify(data, "UTF-8", sign);
    }
}
