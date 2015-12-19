package org.squirrelframework.cloud.resource.codec;

/**
 * Created by kailianghe on 15/12/19.
 */
public interface Codec {
    String decode(String value) throws Exception;
    String encode(String value) throws Exception;
}
