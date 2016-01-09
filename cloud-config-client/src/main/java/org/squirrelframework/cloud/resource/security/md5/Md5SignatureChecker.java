package org.squirrelframework.cloud.resource.security.md5;

import org.squirrelframework.cloud.resource.security.AbstractSignatureChecker;

/**
 * Created by kailianghe on 16/1/6.
 */
public class Md5SignatureChecker extends AbstractSignatureChecker {

    private final Md5SignatureEncoder encoder;

    public Md5SignatureChecker(String secretKey) {
        this.encoder = new Md5SignatureEncoder(secretKey);
    }

    @Override
    public boolean verify(String data, String charset, String sign) throws Exception {
        return encoder.encode(data, charset).equals(sign);
    }
}
