package org.squirrelframework.cloud.resource.security.md5;

import org.apache.commons.codec.binary.Hex;
import org.squirrelframework.cloud.resource.security.AbstractEncoder;
import org.squirrelframework.cloud.resource.security.SignatureEncoder;

import java.security.MessageDigest;

/**
 * Created by kailianghe on 16/1/6.
 */
public class Md5SignatureEncoder extends AbstractEncoder implements SignatureEncoder {

    private final String secretKey;

    public Md5SignatureEncoder(String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public String encode(String value, String charset) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update( (value+secretKey).getBytes(charset) );
        return new String( Hex.encodeHex(md5.digest()) );
    }
}
