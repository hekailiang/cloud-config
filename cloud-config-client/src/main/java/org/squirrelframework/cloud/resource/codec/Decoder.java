package org.squirrelframework.cloud.resource.codec;

/**
 * Created by kailianghe on 15/12/21.
 */
public interface Decoder {
    String decode(String value, String charset) throws Exception;
    String decode(String value) throws Exception;
}
