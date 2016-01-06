package org.squirrelframework.cloud.resource.security;

import org.apache.commons.codec.binary.Base64;
import org.squirrelframework.cloud.utils.CloudConfigCommon;

import javax.crypto.Cipher;
import java.security.Key;

/**
 * Created by kailianghe on 15/12/21.
 */
public class CipherDecoder implements Decoder {
    private static final int MAX_DECODE_BLOCK = 128;

    private final Key key;

    public CipherDecoder(Key key) {
        this.key = key;
    }

    @Override
    public String decode(String value, String charset) throws Exception {
        Cipher cipher = Cipher.getInstance(this.key.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, this.key);
        byte[] data = Base64.decodeBase64( value.getBytes(charset) );
        byte[] result = null;
        for (int i = 0; i < data.length; i += MAX_DECODE_BLOCK) {
            byte[] doFinal = cipher.doFinal(CloudConfigCommon.subarray(data, i, i + MAX_DECODE_BLOCK));
            result = CloudConfigCommon.addAll(result, doFinal);
        }
        return new String(result, charset);
    }

    @Override
    public String decode(String value) throws Exception {
        return decode(value, "UTF-8");
    }
}
