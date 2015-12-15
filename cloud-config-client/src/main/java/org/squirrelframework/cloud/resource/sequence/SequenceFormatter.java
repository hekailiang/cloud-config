package org.squirrelframework.cloud.resource.sequence;

import java.util.Map;

/**
 * Created by kailianghe on 15/12/15.
 */
public interface SequenceFormatter {
    String format(Map<String, Object> parameters);
}
