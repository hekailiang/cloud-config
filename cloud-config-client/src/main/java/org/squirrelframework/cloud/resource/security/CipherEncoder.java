package org.squirrelframework.cloud.resource.security;

import org.apache.commons.codec.binary.Base64;
import org.springframework.util.Base64Utils;
import org.squirrelframework.cloud.utils.CloudConfigCommon;

import javax.crypto.Cipher;
import java.security.Key;

/**
 * Created by kailianghe on 15/12/21.
 */
public class CipherEncoder extends AbstractEncoder {
    private static final int MAX_ENCODE_BLOCK   = 128;

    private final Key key;

    public CipherEncoder(Key key) {
        this.key = key;
    }

    @Override
    public String encode(String value, String charset) throws Exception {
        Cipher cipher = Cipher.getInstance(this.key.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, this.key);
        byte[] data = value.getBytes(charset);
        byte[] result = null;
        for (int i = 0; i < data.length; i += MAX_ENCODE_BLOCK) {
            byte[] doFinal = cipher.doFinal(CloudConfigCommon.subarray(data, i, i + MAX_ENCODE_BLOCK));
            result = CloudConfigCommon.addAll(result, doFinal);
        }
        return new String(Base64.encodeBase64(result), charset);
    }
}
