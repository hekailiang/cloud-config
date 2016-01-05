package org.squirrelframework.cloud.resource.security;

/**
 * Created by kailianghe on 15/12/21.
 */
public interface Decoder {
    String decode(String value, String charset) throws Exception;
}
