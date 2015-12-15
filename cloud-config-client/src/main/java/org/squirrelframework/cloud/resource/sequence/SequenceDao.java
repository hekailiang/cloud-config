package org.squirrelframework.cloud.resource.sequence;

/**
 * Created by kailianghe on 15/12/14.
 */
public interface SequenceDao {
    SequenceRange applyNextRange(String seqName);
}
