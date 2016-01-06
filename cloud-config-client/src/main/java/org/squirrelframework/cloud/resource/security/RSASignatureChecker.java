package org.squirrelframework.cloud.resource.security;

import com.google.common.base.Preconditions;
import org.apache.commons.codec.binary.Base64;
import org.squirrelframework.cloud.utils.CloudConfigCommon;

import java.security.Key;
import java.security.PublicKey;
import java.security.Signature;

/**
 * Created by kailianghe on 16/1/6.
 */
public class RSASignatureChecker extends AbstractSignatureChecker {

    private final PublicKey publicKey;

    public RSASignatureChecker(Key publicKey) {
        Preconditions.checkArgument(publicKey instanceof PublicKey, "must use public key to verify signature");
        this.publicKey = (PublicKey) publicKey;
    }

    public boolean verify(String data, String charset, String sign) throws Exception {
        Signature signature = Signature.getInstance(CloudConfigCommon.SIGNATURE_ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(data.getBytes(charset));
        return signature.verify(Base64.decodeBase64(sign));
    }
}
