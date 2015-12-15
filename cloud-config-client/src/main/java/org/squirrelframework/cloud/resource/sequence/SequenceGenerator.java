package org.squirrelframework.cloud.resource.sequence;

/**
 * Created by kailianghe on 15/12/15.
 */
public interface SequenceGenerator {
    String next(String seqName) throws Exception;
}
