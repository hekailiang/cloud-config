package org.squirrelframework.cloud.resource.security;

import com.google.common.base.Preconditions;
import org.apache.commons.codec.binary.Base64;
import org.squirrelframework.cloud.utils.CloudConfigCommon;

import java.security.Key;
import java.security.PrivateKey;
import java.security.Signature;

/**
 * Created by kailianghe on 16/1/6.
 */
public class RSASignatureEncoder extends AbstractEncoder implements SignatureEncoder {

    private final PrivateKey privateKey;

    public RSASignatureEncoder(Key privateKey) {
        Preconditions.checkArgument(privateKey instanceof PrivateKey, "must use private key to generate signature");
        this.privateKey = (PrivateKey) privateKey;
    }

    @Override
    public String encode(String value, String charset) throws Exception {
        Signature signature = Signature.getInstance(CloudConfigCommon.SIGNATURE_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(value.getBytes(charset));
        return new String(Base64.encodeBase64(signature.sign()), charset);
    }

}
