package org.squirrelframework.cloud.resource.security;

/**
 * Created by kailianghe on 16/1/6.
 */
public class SignatureCodec implements SignatureEncoder, SignatureChecker {

    private final SignatureEncoder encoder;

    private final SignatureChecker checker;

    public SignatureCodec(SignatureEncoder encoder, SignatureChecker checker) {
        this.encoder = encoder;
        this.checker = checker;
    }

    @Override
    public String encode(String value, String charset) throws Exception {
        return encoder.encode(value, charset);
    }

    @Override
    public String encode(String value) throws Exception {
        return encoder.encode(value);
    }

    @Override
    public boolean verify(String data, String charset, String sign) throws Exception {
        return checker.verify(data, charset, sign);
    }

    @Override
    public boolean verify(String data, String sign) throws Exception {
        return checker.verify(data, sign);
    }
}
