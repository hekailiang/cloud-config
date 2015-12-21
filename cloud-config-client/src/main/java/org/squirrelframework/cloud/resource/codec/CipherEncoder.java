package org.squirrelframework.cloud.resource.codec;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.security.Key;

/**
 * Created by kailianghe on 15/12/21.
 */
public class CipherEncoder implements Encoder {
    private final Key key;

    public CipherEncoder(Key key) {
        this.key = key;
    }

    @Override
    public String encode(String value) throws Exception {
        Cipher cipher = Cipher.getInstance(this.key.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, this.key);
        return new String(Base64.encodeBase64(cipher.doFinal(value.getBytes())));
    }
}
