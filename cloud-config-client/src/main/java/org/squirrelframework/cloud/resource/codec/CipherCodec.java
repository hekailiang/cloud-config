package org.squirrelframework.cloud.resource.codec;

import javax.crypto.Cipher;
import java.security.Key;

import org.apache.commons.codec.binary.Base64;
/**
 * Created by kailianghe on 15/12/19.
 */
public class CipherCodec implements Codec {

    private final Key key;

    public CipherCodec(Key key) {
        this.key = key;
    }

    @Override
    public String decode(String value) throws Exception {
        Cipher cipher = Cipher.getInstance(this.key.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, this.key);
        return new String(cipher.doFinal(Base64.decodeBase64(value.getBytes())));
    }

    @Override
    public String encode(String value) throws Exception {
        Cipher cipher = Cipher.getInstance(this.key.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, this.key);
        return new String(Base64.encodeBase64(cipher.doFinal(value.getBytes())));
    }
}
