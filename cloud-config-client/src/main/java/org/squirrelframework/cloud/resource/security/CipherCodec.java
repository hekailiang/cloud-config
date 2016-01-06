package org.squirrelframework.cloud.resource.security;

/**
 * Created by kailianghe on 15/12/19.
 */
public class CipherCodec implements Codec {

    private static final int   MAX_ENCRYPT_BLOCK   = 117;

    private final Encoder encoder;

    private final Decoder decoder;

    public CipherCodec(Encoder encoder, Decoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    @Override
    public String decode(String value, String charset) throws Exception {
        return decoder.decode(value, charset);
    }

    @Override
    public String decode(String value) throws Exception {
        return decoder.decode(value);
    }

    @Override
    public String encode(String value, String charset) throws Exception {
        return encoder.encode(value, charset);
    }

    @Override
    public String encode(String value) throws Exception {
        return encoder.encode(value);
    }
}
