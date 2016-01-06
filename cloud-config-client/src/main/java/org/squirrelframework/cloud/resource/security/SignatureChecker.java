package org.squirrelframework.cloud.resource.security;

/**
 * Created by kailianghe on 16/1/6.
 */
public interface SignatureChecker {
    boolean verify(String data, String charset, String sign) throws Exception;
    boolean verify(String data, String sign) throws Exception;
}
