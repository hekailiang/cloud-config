package org.squirrelframework.cloud.resource.codec;

/**
 * Created by kailianghe on 15/12/21.
 */
public interface Encoder {
    String encode(String value, String charset) throws Exception;
    String encode(String value) throws Exception;
}
