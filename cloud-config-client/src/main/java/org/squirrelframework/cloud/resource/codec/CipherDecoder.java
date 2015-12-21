package org.squirrelframework.cloud.resource.codec;

import org.apache.commons.codec.binary.Base64;
import org.squirrelframework.cloud.utils.CloudConfigCommon;

import javax.crypto.Cipher;
import java.security.Key;

/**
 * Created by kailianghe on 15/12/21.
 */
public class CipherDecoder implements Decoder {
    private final Key key;

    public CipherDecoder(Key key) {
        this.key = key;
    }

    @Override
    public String decode(String value) throws Exception {
        Cipher cipher = Cipher.getInstance(this.key.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, this.key);
        return CloudConfigCommon.bytes2String( cipher.doFinal(Base64.decodeBase64(value.getBytes())) );
    }
}
